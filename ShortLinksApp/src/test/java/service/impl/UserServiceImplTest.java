package service.impl;

import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Проверка сервиса работы с пользователями")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    @DisplayName("Создание нового пользователя")
    void checkCreateNewUserTest() {
        //given
        //when
        User user = userService.createNewUser();

        //then
        assertNotNull(user);
        assertNotNull(user.getUuid());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    @DisplayName("Поиск пользователя по UUID")
    void checkFindUserByUuidTest() {
        //given
        UUID uuid = UUID.randomUUID();
        User expected = new User(uuid);
        when(userRepository.findById(uuid)).thenReturn(Optional.of(expected));

        //when
        Optional<User> result = userService.findByUuid(uuid);

        //then
        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }
}