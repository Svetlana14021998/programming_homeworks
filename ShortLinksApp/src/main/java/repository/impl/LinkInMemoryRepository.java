package repository.impl;

import model.Link;
import repository.LinkRepository;
import storage.LinkStorage;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkInMemoryRepository implements LinkRepository {

    private final Map<String, Link> cache;
    private final LinkStorage storage;

    public LinkInMemoryRepository(LinkStorage storage) {
        this.storage = storage;
        this.cache = new ConcurrentHashMap<>(storage.loadAllLinks());
    }

    /**
     * Поиск ссылки по коду короткой ссылки
     *
     * @param code код короткой ссылки
     * @return Optional с найденной ссылкой, иначе пустой Optional
     */
    @Override
    public Optional<Link> findByCode(String code) {
        return Optional.ofNullable(cache.get(code));
    }

    /**
     * Возвращает все ссылки пользователя
     *
     * @param ownerUuid идентификатор пользователя
     * @return список ссылок пользователя
     */
    @Override
    public List<Link> findByOwner(UUID ownerUuid) {
        return cache.values().stream()
                .filter(link -> ownerUuid.equals(link.getOwnerUuid()))
                .toList();
    }

    /**
     * Проверка существования ссылки по коду короткой ссылки
     *
     * @param code код короткой ссылки
     * @return true, если ссылка есть, false, если нет
     */
    @Override
    public boolean existsByCode(String code) {
        return cache.containsKey(code);
    }

    /**
     * Сохранение ссылки
     *
     * @param link ссылка
     */
    @Override
    public void save(Link link) {
        cache.put(link.getCode(), link);
        storage.saveLinks(cache);
    }

    /**
     * Удаление ссылки по её короткому коду
     *
     * @param code код короткой ссылки
     */
    @Override
    public void deleteByCode(String code) {
        if (cache.remove(code) != null) {
            storage.saveLinks(cache);
        }
    }

    /**
     * Поиск ссылки по url и идентификатору пользователя
     *
     * @param ownerUuid   идентификатор пользователя
     * @param originalUrl оригинальный url
     * @return Optional с найденной ссылкой, иначе пустой Optional
     */
    @Override
    public Optional<Link> findByOwnerAndUrl(UUID ownerUuid, String originalUrl) {
        return cache.values().stream()
                .filter(link -> ownerUuid.equals(link.getOwnerUuid())
                        && originalUrl.equals(link.getOriginalUrl()))
                .findFirst();
    }

    /**
     * Получение всех ссылок
     *
     * @return список всех ссылок
     */
    @Override
    public List<Link> findAll() {
        return List.copyOf(cache.values());
    }
}
