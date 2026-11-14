package repository.impl;

import model.User;
import repository.UserRepository;
import storage.UserStorage;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserInMemoryRepository implements UserRepository {

    private final Map<UUID, User> cache;
    private final UserStorage storage;

    public UserInMemoryRepository(UserStorage storage) {
        this.storage = storage;
        this.cache = new ConcurrentHashMap<>(storage.getUsers());
    }

    @Override
    public void save(User user) {
        cache.put(user.getUuid(), user);
        storage.saveUsers(cache);
    }

    @Override
    public Optional<User> findById(UUID uuid) {
        return Optional.ofNullable(cache.get(uuid));
    }
}
