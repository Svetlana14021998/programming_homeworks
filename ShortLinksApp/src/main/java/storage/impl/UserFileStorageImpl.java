package storage.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import config.AppConfig;
import model.User;
import storage.UserStorage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Работа с файловым хранилищем пользователей (чтение и запись)
 */
public class UserFileStorageImpl implements UserStorage {

    private static final String USERS_FILE = AppConfig.getInstance().getUsersFile();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final UserFileStorageImpl INSTANCE = new UserFileStorageImpl();

    private UserFileStorageImpl() {
    }

    public static UserFileStorageImpl getInstance() {
        return INSTANCE;
    }


    /**
     * Загружает всех пользователей из файла. Если файла нет — создает пустой файл и возвращает
     * пустую Map.
     *
     * @return все пользователи
     */
    @Override
    public Map<UUID, User> getUsers() {
        Path path = Path.of(USERS_FILE);

        if (Files.notExists(path)) {
            System.out.println("Файл пользователей не найден. Создаем новый");
            try {
                Files.writeString(path, "{}", StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException("Файл пользователей недоступен", e);
            }
            return new HashMap<>();
        }
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            if (content.isBlank()) {
                return new HashMap<>();
            }

            Type type = new TypeToken<Map<UUID, User>>() {
            }.getType();
            Map<UUID, User> users = GSON.fromJson(content, type);
            return users != null ? users : new HashMap<>();

        } catch (IOException e) {
            throw new UncheckedIOException("Файл пользователей недоступен", e);
        } catch (JsonParseException e) {
            throw new IllegalStateException("Не удалось разобрать %s".formatted(USERS_FILE), e);
        }
    }

    /**
     * Сохраняет всех пользователей в файл (перезаписывает).
     *
     * @param users мапа пользователей (ключ - идентификатор, значение - пользователь)
     */
    @Override
    public void saveUsers(Map<UUID, User> users) {
        try {
            String json = GSON.toJson(users);
            Files.writeString(Path.of(USERS_FILE), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка при сохранении пользователей", e);
        }
    }
}
