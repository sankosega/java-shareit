package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.util.ShareItHeaders;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader(ShareItHeaders.X_SHARER_USER_ID) long userId,
                                             @Valid @RequestBody BookingDto bookingDto) {
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new IllegalArgumentException("Booking start must be before end");
        }
        return bookingClient.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader(ShareItHeaders.X_SHARER_USER_ID) long userId,
                                                 @PathVariable long bookingId,
                                                 @RequestParam boolean approved) {
        return bookingClient.approveBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader(ShareItHeaders.X_SHARER_USER_ID) long userId,
                                                 @PathVariable long bookingId) {
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBooker(@RequestHeader(ShareItHeaders.X_SHARER_USER_ID) long userId,
                                                      @RequestParam(defaultValue = "ALL") String state) {
        validateState(state);
        return bookingClient.getBookingsByBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getBookingsByOwner(@RequestHeader(ShareItHeaders.X_SHARER_USER_ID) long userId,
                                                     @RequestParam(defaultValue = "ALL") String state) {
        validateState(state);
        return bookingClient.getBookingsByOwner(userId, state);
    }

    private void validateState(String state) {
        try {
            BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}
