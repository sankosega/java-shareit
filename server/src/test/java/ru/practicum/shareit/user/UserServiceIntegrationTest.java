package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_shouldPersistAndReturn() {
        UserDto result = userService.createUser(new UserDto(null, "Alice", "alice@usit.com"));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getEmail()).isEqualTo("alice@usit.com");
    }

    @Test
    void createUser_duplicateEmail_shouldThrow() {
        userService.createUser(new UserDto(null, "Alice", "dup@usit.com"));

        assertThatThrownBy(() -> userService.createUser(new UserDto(null, "Bob", "dup@usit.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateUser_name_shouldUpdate() {
        UserDto saved = userService.createUser(new UserDto(null, "Alice", "alice@usit.com"));

        UserDto result = userService.updateUser(saved.getId(), new UserDto(null, "AliceNew", null));

        assertThat(result.getName()).isEqualTo("AliceNew");
        assertThat(result.getEmail()).isEqualTo("alice@usit.com");
    }

    @Test
    void updateUser_email_shouldUpdate() {
        UserDto saved = userService.createUser(new UserDto(null, "Alice", "alice@usit.com"));

        UserDto result = userService.updateUser(saved.getId(), new UserDto(null, null, "alice2@usit.com"));

        assertThat(result.getEmail()).isEqualTo("alice2@usit.com");
        assertThat(result.getName()).isEqualTo("Alice");
    }

    @Test
    void updateUser_sameEmail_shouldNotThrow() {
        UserDto saved = userService.createUser(new UserDto(null, "Alice", "alice@usit.com"));

        UserDto result = userService.updateUser(saved.getId(), new UserDto(null, "AliceNew", "alice@usit.com"));

        assertThat(result.getName()).isEqualTo("AliceNew");
        assertThat(result.getEmail()).isEqualTo("alice@usit.com");
    }

    @Test
    void updateUser_duplicateEmail_shouldThrow() {
        UserDto user1 = userService.createUser(new UserDto(null, "Alice", "alice@usit.com"));
        userService.createUser(new UserDto(null, "Bob", "bob@usit.com"));

        assertThatThrownBy(() -> userService.updateUser(user1.getId(), new UserDto(null, null, "bob@usit.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void updateUser_notFound_shouldThrow() {
        assertThatThrownBy(() -> userService.updateUser(999L, new UserDto(null, "X", null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getUserById_shouldReturnUser() {
        UserDto saved = userService.createUser(new UserDto(null, "Alice", "alice@usit.com"));

        UserDto result = userService.getUserById(saved.getId());

        assertThat(result.getName()).isEqualTo("Alice");
        assertThat(result.getEmail()).isEqualTo("alice@usit.com");
    }

    @Test
    void getUserById_notFound_shouldThrow() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllUsers_shouldReturnAll() {
        userService.createUser(new UserDto(null, "Alice", "alice@usit.com"));
        userService.createUser(new UserDto(null, "Bob", "bob@usit.com"));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void deleteUser_shouldRemove() {
        UserDto saved = userService.createUser(new UserDto(null, "Alice", "alice@usit.com"));
        userService.deleteUser(saved.getId());

        assertThatThrownBy(() -> userService.getUserById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteUser_notFound_shouldThrow() {
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
