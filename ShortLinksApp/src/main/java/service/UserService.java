package service;

import model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserService {

    User createNewUser();

    Optional<User> findByUuid(UUID uuid);
}
