package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void serialize_shouldContainAllFields() throws Exception {
        LocalDateTime start = LocalDateTime.of(2030, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2030, 1, 2, 10, 0);
        BookingDto dto = new BookingDto(1L, start, end);

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
    }

    @Test
    void deserialize_shouldParseAllFields() throws Exception {
        String content = "{\"itemId\":2,\"start\":\"2030-06-01T10:00:00\",\"end\":\"2030-06-02T10:00:00\"}";

        BookingDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(2L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2030, 6, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2030, 6, 2, 10, 0));
    }
}
