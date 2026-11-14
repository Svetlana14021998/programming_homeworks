package helpers;

import config.AppConfig;
import model.Link;

import java.time.Instant;
import java.util.List;

/**
 * Вспомогательный класс для вывода информации о ссылках пользователя
 */
public class LinksHelper {

    public static void printUserLinksInfo(List<Link> links) {
        if (links.isEmpty()) {
            System.out.println("У вас пока нет созданных ссылок.");
            return;
        }
        System.out.println("Ваши ссылки:");
        for (Link link : links) {
            print(link);
        }
    }

    public static void printUserLinkInfo(Link link) {
        if (link == null) {
            System.out.println("Информация не найдена");
            return;
        }
        print(link);
    }

    private static void print(Link link) {
        String prefix = AppConfig.getInstance().getLinkPrefix();
        System.out.println("----------------------------------------");
        System.out.println("Короткая ссылка: " + prefix + link.getCode());
        System.out.println("URL: " + link.getOriginalUrl());

        if (link.getCurrentLimit() <= 0) {
            System.out.println("Статус: Лимит переходов истек");
        } else if (Instant.now().isAfter(link.getExpiresAt())) {
            System.out.println("Статус: Ссылка недоступна для использования и будет удалена");
        } else {
            System.out.println("Статус: Активна (осталось переходов: " + link.getCurrentLimit() + ")");
        }
        System.out.println("----------------------------------------");
    }
}
