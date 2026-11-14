package service.impl;

import config.AppConfig;
import model.Link;
import repository.LinkRepository;
import service.LinkService;

import java.awt.*;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

/**
 * Сервис для работы со ссылками
 */
public class LinkServiceImpl implements LinkService {

    private final LinkRepository linkRepository;
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = AppConfig.getInstance().getCodeLength();

    public LinkServiceImpl(LinkRepository linkRepository) {
        this.linkRepository = linkRepository;
    }

    /**
     * Создание короткой ссылки для пользователя
     * Если у пользователя уже есть "живая" ссылка, то новая не создается и возвращается старая
     * Для генерации кода короткой ссылки используется метод {@link #generateCode}, вызов которого в цикле do-while исключает коллизии
     *
     * @param ownerUuid   пользователь - владелец ссылки
     * @param originalUrl исходный url
     * @param limit       количество переходов по ссылке
     * @return ссылка
     */
    @Override
    public Link createLink(UUID ownerUuid, String originalUrl, int limit) {
        if (originalUrl == null || !originalUrl.startsWith("http")) {
            throw new IllegalArgumentException("URL должен начинаться с http:// или https://");
        }

        Optional<Link> existingLink = linkRepository.findByOwnerAndUrl(ownerUuid, originalUrl);

        if (existingLink.isPresent()) {
            Link link = existingLink.get();
            boolean isAlive = Instant.now().isBefore(link.getExpiresAt())
                    && link.getCurrentLimit() > 0;

            if (isAlive) {
                System.out.println("Для этого URL у вас уже есть активная ссылка.");
                return link;
            }
        }

        String code;
        do {
            code = generateCode();
        } while (linkRepository.existsByCode(code));

        Link link = new Link(originalUrl, code, Instant.now().plus(AppConfig.getInstance().getLinkTtlDays(), ChronoUnit.DAYS), limit, limit, ownerUuid);
        linkRepository.save(link);
        System.out.println("Сгенерирована новая короткая ссылка");
        return link;
    }

    /**
     * Метод для перехода по короткой ссылке
     * Переход не осуществляется, если:
     * 1.Если ссылка не найдена или у нее другой владелец
     * 2.Если срок действия ссылки истек, но она пока не удалена
     * 3.Если исчерпан лимит переходов по ссылке
     * При успешном переходе по ссылке ее текущий лимит переходов уменьшается
     *
     * @param ownerUuid uuid текущего пользователя
     * @param shortLink короткая ссылка
     */
    @Override
    public void openLink(UUID ownerUuid, String shortLink) {
        String prefix = AppConfig.getInstance().getLinkPrefix();
        String code = shortLink.replace(prefix, "");
        Link link = linkRepository.findByCode(code).orElse(null);
        if (link == null) {
            System.out.println("Ссылка не найдена");
            return;
        }
        if (!link.getOwnerUuid().equals(ownerUuid)) {
            System.out.println("Ссылка не найдена");
            return;
        }
        if (Instant.now().isAfter(link.getExpiresAt())) {
            System.out.println("Срок действия ссылки истек");
            return;
        }
        if (link.getCurrentLimit() == 0) {
            System.out.println("Исчерпан лимит переходов по ссылке");
            return;
        }
        int newLimit = link.getCurrentLimit() - 1;
        link.setCurrentLimit(newLimit);
        linkRepository.save(link);
        if (newLimit == 0) {
            System.out.println("Внимание: это был последний доступный переход. Лимит по ссылке исчерпан");
        }else {
            System.out.printf("Осталось доступных переходов:%d%n",newLimit);
        }
        try {
            Desktop.getDesktop().browse(new URI(link.getOriginalUrl()));
        } catch (Exception e) {
            System.out.println("Не удалось открыть браузер. Перейдите вручную: " + link.getOriginalUrl());
        }
    }

    /**
     * Возвращает все ссылки текущего пользователя
     *
     * @param ownerUuid идентификатор пользователя
     * @return список ссылок текущего пользователя
     */
    @Override
    public List<Link> getUserLinks(UUID ownerUuid) {
        return linkRepository.findByOwner(ownerUuid);
    }

    /**
     * Возвращает информацию о ссылке по короткой ссылке
     *
     * @param shortLink       короткая ссылка
     * @param currentUserUuid идентификатор пользователя
     * @return ссылка или null, если она не найдена или не принадлежит текущему пользователю
     */
    @Override
    public Link getLink(String shortLink, UUID currentUserUuid) {
        String prefix = AppConfig.getInstance().getLinkPrefix();
        String code = shortLink.replace(prefix, "");
        Link link = linkRepository.findByCode(code).orElse(null);
        if (link != null && !link.getOwnerUuid().equals(currentUserUuid)) {
            return null;
        }
        return link;
    }

    /**
     * Удаление ссылки
     *
     * @param shortLink       короткая ссылка
     * @param currentUserUuid текущий пользователь
     */
    @Override
    public void deleteLink(String shortLink, UUID currentUserUuid) {
        Link link = getLink(shortLink, currentUserUuid);
        if (link == null) {
            System.out.println("Ссылка не найдена");
            return;
        }
        linkRepository.deleteByCode(link.getCode());
        System.out.println("Ссылка удалена");
    }

    /**
     * Метод для обновления количества лимитов для ссылки
     * Если ссылка не найдена или принадлежит другому пользователю, то действие невозможно
     * Если срок действия ссылки истек, но она пока не удалена, то изменение лимита недоступно
     *
     * @param shortLink       короткая ссылка
     * @param newLimit        новый лимит
     * @param currentUserUuid идентификатор пользователя
     */
    @Override
    public void updateLinkLimit(String shortLink, int newLimit, UUID currentUserUuid) {
        Link link = getLink(shortLink, currentUserUuid);
        if (link == null) {
            System.out.println("Ссылка не найдена");
            return;
        }
        if (Instant.now().isAfter(link.getExpiresAt())) {
            System.out.println("Ссылка не активна и будет удалена. Операции над ней невозможны");
            return;
        }
        link.setLimit(newLimit);
        link.setCurrentLimit(newLimit);
        linkRepository.save(link);
        System.out.printf("Лимит ссылки обновлен. Установлено %d доступных переходов.%n", newLimit);
    }

    /**
     * Генерирует последовательность из 6 символов латинского алфавита и цифр
     *
     * @return код короткой ссылки
     */
    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
