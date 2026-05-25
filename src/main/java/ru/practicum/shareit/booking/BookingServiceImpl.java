package ru.practicum.shareit.booking;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private Map<BookingState, BookingFetchStrategy> bookerStrategies;
    private Map<BookingState, BookingFetchStrategy> ownerStrategies;

    @PostConstruct
    private void initStrategies() {
        bookerStrategies = new EnumMap<>(BookingState.class);
        bookerStrategies.put(BookingState.ALL,
                (id, now) -> bookingRepository.findByBooker_Id(id, SORT_BY_START_DESC));
        bookerStrategies.put(BookingState.CURRENT,
                (id, now) -> bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfter(
                        id, now, now, SORT_BY_START_DESC));
        bookerStrategies.put(BookingState.PAST,
                (id, now) -> bookingRepository.findByBooker_IdAndEndIsBefore(id, now, SORT_BY_START_DESC));
        bookerStrategies.put(BookingState.FUTURE,
                (id, now) -> bookingRepository.findByBooker_IdAndStartIsAfter(id, now, SORT_BY_START_DESC));
        bookerStrategies.put(BookingState.WAITING,
                (id, now) -> bookingRepository.findByBooker_IdAndStatus(id, BookingStatus.WAITING, SORT_BY_START_DESC));
        bookerStrategies.put(BookingState.REJECTED,
                (id, now) -> bookingRepository.findByBooker_IdAndStatus(id, BookingStatus.REJECTED, SORT_BY_START_DESC));

        ownerStrategies = new EnumMap<>(BookingState.class);
        ownerStrategies.put(BookingState.ALL,
                (id, now) -> bookingRepository.findByItem_Owner_Id(id, SORT_BY_START_DESC));
        ownerStrategies.put(BookingState.CURRENT,
                (id, now) -> bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(
                        id, now, now, SORT_BY_START_DESC));
        ownerStrategies.put(BookingState.PAST,
                (id, now) -> bookingRepository.findByItem_Owner_IdAndEndIsBefore(id, now, SORT_BY_START_DESC));
        ownerStrategies.put(BookingState.FUTURE,
                (id, now) -> bookingRepository.findByItem_Owner_IdAndStartIsAfter(id, now, SORT_BY_START_DESC));
        ownerStrategies.put(BookingState.WAITING,
                (id, now) -> bookingRepository.findByItem_Owner_IdAndStatus(id, BookingStatus.WAITING, SORT_BY_START_DESC));
        ownerStrategies.put(BookingState.REJECTED,
                (id, now) -> bookingRepository.findByItem_Owner_IdAndStatus(id, BookingStatus.REJECTED, SORT_BY_START_DESC));
    }

    @Override
    @Transactional
    public BookingResponseDto addBooking(Long userId, BookingDto bookingDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found: " + bookingDto.getItemId()));
        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Item is not available for booking");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Owner cannot book their own item");
        }
        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new ValidationException("Booking start must be before end");
        }
        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the item owner can approve bookings");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking is already processed");
        }
        booking.setStatus(Boolean.TRUE.equals(approved) ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toBookingResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found: " + bookingId));
        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);
        if (!isBooker && !isOwner) {
            throw new NotFoundException("Access denied for user: " + userId);
        }
        return BookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingsByBooker(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        BookingState bookingState = parseState(state);
        List<Booking> bookings = bookerStrategies.get(bookingState).fetch(userId, LocalDateTime.now());
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        BookingState bookingState = parseState(state);
        List<Booking> bookings = ownerStrategies.get(bookingState).fetch(userId, LocalDateTime.now());
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }
}
