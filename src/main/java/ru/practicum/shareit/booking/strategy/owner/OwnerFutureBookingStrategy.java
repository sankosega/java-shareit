package ru.practicum.shareit.booking.strategy.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.strategy.OwnerBookingSearchStrategy;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OwnerFutureBookingStrategy implements OwnerBookingSearchStrategy {

    private final BookingRepository bookingRepository;

    @Override
    public BookingState getState() {
        return BookingState.FUTURE;
    }

    @Override
    public List<Booking> findBookings(Long userId, LocalDateTime now, Sort sort) {
        return bookingRepository.findByItem_Owner_IdAndStartIsAfter(userId, now, sort);
    }
}
