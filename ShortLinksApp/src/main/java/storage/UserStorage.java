package storage;

import model.User;

import java.util.Map;
import java.util.UUID;

public interface UserStorage {

    Map<UUID, User> getUsers();

    void saveUsers(Map<UUID, User> users);

}
