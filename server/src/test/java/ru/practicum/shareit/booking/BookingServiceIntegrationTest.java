package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Long ownerId;
    private Long bookerId;
    private Long itemId;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(new User(null, "Owner", "owner@bsit.com"));
        User booker = userRepository.save(new User(null, "Booker", "booker@bsit.com"));
        ownerId = owner.getId();
        bookerId = booker.getId();

        Item item = new Item();
        item.setName("Drill");
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwner(owner);
        itemId = itemRepository.save(item).getId();
    }

    @Test
    void addBooking_shouldCreateWithWaitingStatus() {
        BookingResponseDto result = bookingService.addBooking(bookerId,
                new BookingDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));

        assertThat(result.getId()).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(itemId);
        assertThat(result.getBooker().getId()).isEqualTo(bookerId);
    }

    @Test
    void addBooking_unavailableItem_shouldThrow() {
        Item locked = new Item();
        locked.setName("Locked");
        locked.setDescription("desc");
        locked.setAvailable(false);
        locked.setOwner(userRepository.findById(ownerId).orElseThrow());
        Long lockedId = itemRepository.save(locked).getId();

        assertThatThrownBy(() -> bookingService.addBooking(bookerId,
                new BookingDto(lockedId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addBooking_ownerBookingOwnItem_shouldThrow() {
        assertThatThrownBy(() -> bookingService.addBooking(ownerId,
                new BookingDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addBooking_startEqualsEnd_shouldThrow() {
        LocalDateTime t = LocalDateTime.now().plusDays(1);
        assertThatThrownBy(() -> bookingService.addBooking(bookerId, new BookingDto(itemId, t, t)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void addBooking_unknownUser_shouldThrow() {
        assertThatThrownBy(() -> bookingService.addBooking(999L,
                new BookingDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void addBooking_unknownItem_shouldThrow() {
        assertThatThrownBy(() -> bookingService.addBooking(bookerId,
                new BookingDto(999L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void approveBooking_approved_shouldSetApproved() {
        BookingResponseDto booking = createFutureBooking();
        BookingResponseDto result = bookingService.approveBooking(ownerId, booking.getId(), true);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBooking_rejected_shouldSetRejected() {
        BookingResponseDto booking = createFutureBooking();
        BookingResponseDto result = bookingService.approveBooking(ownerId, booking.getId(), false);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveBooking_byNonOwner_shouldThrow() {
        BookingResponseDto booking = createFutureBooking();
        assertThatThrownBy(() -> bookingService.approveBooking(bookerId, booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void approveBooking_alreadyProcessed_shouldThrow() {
        BookingResponseDto booking = createFutureBooking();
        bookingService.approveBooking(ownerId, booking.getId(), true);
        assertThatThrownBy(() -> bookingService.approveBooking(ownerId, booking.getId(), true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveBooking_notFound_shouldThrow() {
        assertThatThrownBy(() -> bookingService.approveBooking(ownerId, 999L, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getBookingById_asBooker_shouldReturn() {
        BookingResponseDto booking = createFutureBooking();
        BookingResponseDto result = bookingService.getBookingById(bookerId, booking.getId());
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingById_asOwner_shouldReturn() {
        BookingResponseDto booking = createFutureBooking();
        BookingResponseDto result = bookingService.getBookingById(ownerId, booking.getId());
        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getBookingById_asStranger_shouldThrow() {
        BookingResponseDto booking = createFutureBooking();
        User stranger = userRepository.save(new User(null, "Stranger", "stranger@bsit.com"));
        assertThatThrownBy(() -> bookingService.getBookingById(stranger.getId(), booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getBookingById_notFound_shouldThrow() {
        assertThatThrownBy(() -> bookingService.getBookingById(bookerId, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getBookingsByBooker_allState_shouldReturnAll() {
        createFutureBooking();
        List<BookingResponseDto> result = bookingService.getBookingsByBooker(bookerId, "ALL");
        assertThat(result).hasSize(1);
    }

    @Test
    void getBookingsByBooker_waitingState() {
        createFutureBooking();
        assertThat(bookingService.getBookingsByBooker(bookerId, "WAITING")).hasSize(1);
    }

    @Test
    void getBookingsByBooker_futureState() {
        createFutureBooking();
        assertThat(bookingService.getBookingsByBooker(bookerId, "FUTURE")).hasSize(1);
    }

    @Test
    void getBookingsByBooker_pastState() {
        createPastBooking();
        assertThat(bookingService.getBookingsByBooker(bookerId, "PAST")).hasSize(1);
    }

    @Test
    void getBookingsByBooker_currentState() {
        createCurrentBooking();
        assertThat(bookingService.getBookingsByBooker(bookerId, "CURRENT")).hasSize(1);
    }

    @Test
    void getBookingsByBooker_rejectedState() {
        BookingResponseDto booking = createFutureBooking();
        bookingService.approveBooking(ownerId, booking.getId(), false);
        assertThat(bookingService.getBookingsByBooker(bookerId, "REJECTED")).hasSize(1);
    }

    @Test
    void getBookingsByBooker_unknownUser_shouldThrow() {
        assertThatThrownBy(() -> bookingService.getBookingsByBooker(999L, "ALL"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getBookingsByBooker_invalidState_shouldThrow() {
        assertThatThrownBy(() -> bookingService.getBookingsByBooker(bookerId, "UNKNOWN"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getBookingsByOwner_allState_shouldReturnAll() {
        createFutureBooking();
        assertThat(bookingService.getBookingsByOwner(ownerId, "ALL")).hasSize(1);
    }

    @Test
    void getBookingsByOwner_waitingState() {
        createFutureBooking();
        assertThat(bookingService.getBookingsByOwner(ownerId, "WAITING")).hasSize(1);
    }

    @Test
    void getBookingsByOwner_futureState() {
        createFutureBooking();
        assertThat(bookingService.getBookingsByOwner(ownerId, "FUTURE")).hasSize(1);
    }

    @Test
    void getBookingsByOwner_pastState() {
        createPastBooking();
        assertThat(bookingService.getBookingsByOwner(ownerId, "PAST")).hasSize(1);
    }

    @Test
    void getBookingsByOwner_currentState() {
        createCurrentBooking();
        assertThat(bookingService.getBookingsByOwner(ownerId, "CURRENT")).hasSize(1);
    }

    @Test
    void getBookingsByOwner_rejectedState() {
        BookingResponseDto booking = createFutureBooking();
        bookingService.approveBooking(ownerId, booking.getId(), false);
        assertThat(bookingService.getBookingsByOwner(ownerId, "REJECTED")).hasSize(1);
    }

    @Test
    void getBookingsByOwner_unknownUser_shouldThrow() {
        assertThatThrownBy(() -> bookingService.getBookingsByOwner(999L, "ALL"))
                .isInstanceOf(NotFoundException.class);
    }

    private BookingResponseDto createFutureBooking() {
        return bookingService.addBooking(bookerId,
                new BookingDto(itemId, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2)));
    }

    private void createPastBooking() {
        bookingService.addBooking(bookerId,
                new BookingDto(itemId, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusMinutes(1)));
    }

    private void createCurrentBooking() {
        bookingService.addBooking(bookerId,
                new BookingDto(itemId, LocalDateTime.now().minusHours(1), LocalDateTime.now().plusHours(1)));
    }
}
