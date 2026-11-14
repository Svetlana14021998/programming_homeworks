package storage.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import config.AppConfig;
import model.Link;
import storage.LinkStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Работа с файловым хранилищем ссылок (чтение и запись)
 */
public class LinkFileStorageImpl implements LinkStorage {

    private static final String LINKS_FILE = AppConfig.getInstance().getLinksFile();
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Instant.class,
                    (JsonSerializer<Instant>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(Instant.class,
                    (JsonDeserializer<Instant>) (json, type, ctx) -> Instant.parse(json.getAsString()))
            .create();
    private static final LinkFileStorageImpl INSTANCE = new LinkFileStorageImpl();

    private LinkFileStorageImpl() {
    }

    public static LinkFileStorageImpl getInstance() {
        return INSTANCE;
    }

    /**
     * Загружает все ссылки из файла. Если файла нет — создает пустой файл
     * и возвращает пустую Map.
     *
     * @return все ссылки (ключ — код)
     */
    @Override
    public Map<String, Link> loadAllLinks() {
        Path path = Path.of(LINKS_FILE);

        if (Files.notExists(path)) {
            System.out.println("Файл ссылок не найден. Создаем новый");
            try {
                Files.writeString(path, "{}", StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException("Файл ссылок недоступен", e);
            }
            return new HashMap<>();
        }

        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (content.isBlank()) {
                return new HashMap<>();
            }

            Type type = new TypeToken<Map<String, Link>>() {
            }.getType();
            Map<String, Link> links = GSON.fromJson(content, type);
            return links != null ? links : new HashMap<>();

        } catch (IOException e) {
            throw new UncheckedIOException("Файл ссылок недоступен", e);
        } catch (JsonParseException e) {
            throw new IllegalStateException("Не удалось разобрать файл %s".formatted(LINKS_FILE), e);
        }
    }

    /**
     * Сохраняет все ссылки в файл (перезаписывает).
     *
     * @param links все ссылки (ключ - код ссылки, значение - ссылка)
     */
    @Override
    public void saveLinks(Map<String, Link> links) {
        try {
            String json = GSON.toJson(links);
            Files.writeString(Path.of(LINKS_FILE), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при сохранении ссылок", e);
        }
    }
}