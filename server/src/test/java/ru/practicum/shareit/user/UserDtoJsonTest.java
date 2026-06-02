package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void serialize_containsAllFields() throws Exception {
        UserDto dto = new UserDto(1L, "Alice", "alice@example.com");

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id", 1);
        assertThat(result).hasJsonPathStringValue("$.name", "Alice");
        assertThat(result).hasJsonPathStringValue("$.email", "alice@example.com");
    }

    @Test
    void deserialize_parsesCorrectly() throws Exception {
        String content = """
                {
                  "id": 2,
                  "name": "Bob",
                  "email": "bob@example.com"
                }
                """;

        UserDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getName()).isEqualTo("Bob");
        assertThat(dto.getEmail()).isEqualTo("bob@example.com");
    }

    @Test
    void deserialize_nullId_parsesWithoutId() throws Exception {
        String content = """
                {
                  "name": "Charlie",
                  "email": "charlie@example.com"
                }
                """;

        UserDto dto = json.parseObject(content);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isEqualTo("Charlie");
    }
}
