package service.impl;

import model.User;
import repository.UserRepository;
import service.UserService;

import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с пользователями
 */
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Создает нового пользователя со случайным UUID
     *
     * @return новый пользователь
     */
    @Override
    public User createNewUser() {
        UUID newUuid = UUID.randomUUID();
        User user = new User(newUuid);
        userRepository.save(user);
        return user;
    }

    /**
     * Поиск пользователя по uuid
     *
     * @param uuid идентификатор пользователя
     * @return Optional с пользователем, если он найден, иначе пустой Optional
     */
    @Override
    public Optional<User> findByUuid(UUID uuid) {
        return userRepository.findById(uuid);
    }
}
