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
@Transactional
@ActiveProfiles("test")
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void createUser_savesAndReturnsUser() {
        UserDto dto = new UserDto(null, "Alice", "alice@example.com");

        UserDto saved = userService.createUser(dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Alice");
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
    }

    @Test
    void createUser_duplicateEmail_throwsConflict() {
        userService.createUser(new UserDto(null, "Alice", "dup@example.com"));

        assertThatThrownBy(() -> userService.createUser(new UserDto(null, "Bob", "dup@example.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getUserById_notFound_throwsNotFoundException() {
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateUser_updatesNameAndEmail() {
        UserDto saved = userService.createUser(new UserDto(null, "Old", "old@example.com"));

        UserDto updated = userService.updateUser(saved.getId(), new UserDto(null, "New", "new@example.com"));

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void getAllUsers_returnsAllSaved() {
        userService.createUser(new UserDto(null, "U1", "u1@example.com"));
        userService.createUser(new UserDto(null, "U2", "u2@example.com"));

        List<UserDto> users = userService.getAllUsers();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void deleteUser_removesUser() {
        UserDto saved = userService.createUser(new UserDto(null, "ToDelete", "del@example.com"));
        userService.deleteUser(saved.getId());

        assertThatThrownBy(() -> userService.getUserById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}
