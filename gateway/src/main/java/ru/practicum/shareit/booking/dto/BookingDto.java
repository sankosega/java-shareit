package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    @NotNull(message = "Item id must not be null")
    private Long itemId;

    @NotNull(message = "Booking start must not be null")
    private LocalDateTime start;

    @NotNull(message = "Booking end must not be null")
    private LocalDateTime end;
}
