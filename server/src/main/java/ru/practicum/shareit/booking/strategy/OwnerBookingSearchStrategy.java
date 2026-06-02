package ru.practicum.shareit.booking.strategy;

import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;

import java.time.LocalDateTime;
import java.util.List;

public interface OwnerBookingSearchStrategy {
    BookingState getState();

    List<Booking> findBookings(Long userId, LocalDateTime now, Sort sort);
}
