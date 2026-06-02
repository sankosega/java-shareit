package ru.practicum.shareit.booking.strategy.booker;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.strategy.BookerBookingSearchStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookerAllBookingStrategy implements BookerBookingSearchStrategy {

    private final BookingRepository bookingRepository;

    @Override
    public BookingState getState() {
        return BookingState.ALL;
    }

    @Override
    public List<Booking> findBookings(Long userId, LocalDateTime now, Sort sort) {
        return bookingRepository.findByBooker_Id(userId, sort);
    }
}
