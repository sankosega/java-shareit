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
    void serialize_containsAllFields() throws Exception {
        BookingDto dto = new BookingDto(
                1L,
                LocalDateTime.of(2025, 6, 1, 10, 0),
                LocalDateTime.of(2025, 6, 2, 10, 0)
        );

        var result = json.write(dto);

        assertThat(result).hasJsonPathNumberValue("$.itemId", 1);
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).hasJsonPath("$.end");
    }

    @Test
    void deserialize_parsesCorrectly() throws Exception {
        String content = """
                {
                  "itemId": 5,
                  "start": "2025-06-01T10:00:00",
                  "end": "2025-06-02T10:00:00"
                }
                """;

        BookingDto dto = json.parseObject(content);

        assertThat(dto.getItemId()).isEqualTo(5L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2025, 6, 1, 10, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2025, 6, 2, 10, 0));
    }
}
