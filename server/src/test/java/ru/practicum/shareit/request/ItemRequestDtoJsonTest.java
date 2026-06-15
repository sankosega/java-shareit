package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.dto.ItemShortResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestResponseDto> json;

    @Test
    void serialize_shouldContainAllFields() throws Exception {
        ItemShortResponseDto item = new ItemShortResponseDto(1L, "Drill", 2L, 3L);
        ItemRequestResponseDto dto = new ItemRequestResponseDto(
                1L, "Need a drill",
                LocalDateTime.of(2025, 6, 1, 12, 0),
                List.of(item));

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(result).hasJsonPathArrayValue("$.items");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
    }

    @Test
    void deserialize_shouldParseDescription() throws Exception {
        String content = "{\"id\":1,\"description\":\"Need a drill\",\"created\":\"2025-06-01T12:00:00\",\"items\":[]}";

        ItemRequestResponseDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Need a drill");
        assertThat(dto.getItems()).isEmpty();
    }
}
