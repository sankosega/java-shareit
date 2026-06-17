package ru.practicum.shareit.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.client.BaseClient;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {

    private static final String API_PREFIX = "/bookings";

    public BookingClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> addBooking(long userId, BookingDto bookingDto) {
        return post(API_PREFIX, userId, bookingDto);
    }

    public ResponseEntity<Object> approveBooking(long userId, long bookingId, boolean approved) {
        return patch(API_PREFIX + "/" + bookingId + "?approved={approved}", userId,
                Map.of("approved", approved), null);
    }

    public ResponseEntity<Object> getBookingById(long userId, long bookingId) {
        return get(API_PREFIX + "/" + bookingId, userId);
    }

    public ResponseEntity<Object> getBookingsByBooker(long userId, String state) {
        return get(API_PREFIX + "?state={state}", userId, Map.of("state", state));
    }

    public ResponseEntity<Object> getBookingsByOwner(long userId, String state) {
        return get(API_PREFIX + "/owner?state={state}", userId, Map.of("state", state));
    }
}
