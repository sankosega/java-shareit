package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.createUser(new UserDto(null, "Owner", "owner@book.com"));
        ownerId = owner.getId();
        UserDto booker = userService.createUser(new UserDto(null, "Booker", "booker@book.com"));
        bookerId = booker.getId();
        ItemDto item = itemService.addItem(ownerId, new ItemDto(null, "Bike", "Mountain bike", true, null));
        itemId = item.getId();
    }

    @Test
    void addBooking_savesAndReturnsBooking() {
        BookingDto dto = new BookingDto(itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        BookingResponseDto result = bookingService.addBooking(bookerId, dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(itemId);
    }

    @Test
    void addBooking_unavailableItem_throwsValidation() {
        itemService.updateItem(ownerId, itemId, new ItemDto(null, null, null, false, null));
        BookingDto dto = new BookingDto(itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.addBooking(bookerId, dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveBooking_changesStatusToApproved() {
        BookingDto dto = new BookingDto(itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        BookingResponseDto booking = bookingService.addBooking(bookerId, dto);

        BookingResponseDto approved = bookingService.approveBooking(ownerId, booking.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getBookingsByBooker_allState_returnsAllBookings() {
        bookingService.addBooking(bookerId, new BookingDto(itemId,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        List<BookingResponseDto> bookings = bookingService.getBookingsByBooker(bookerId, "ALL");

        assertThat(bookings).hasSize(1);
    }

    @Test
    void getBookingsByOwner_allState_returnsAllBookings() {
        bookingService.addBooking(bookerId, new BookingDto(itemId,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        List<BookingResponseDto> bookings = bookingService.getBookingsByOwner(ownerId, "ALL");

        assertThat(bookings).hasSize(1);
    }

    @Test
    void getBookingsByBooker_unknownState_throwsValidation() {
        assertThatThrownBy(() -> bookingService.getBookingsByBooker(bookerId, "UNKNOWN"))
                .isInstanceOf(ValidationException.class);
    }
}
