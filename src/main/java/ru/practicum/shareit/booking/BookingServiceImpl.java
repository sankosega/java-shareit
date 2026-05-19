package ru.practicum.shareit.booking;

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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

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
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findByBooker_IdAndStartIsBeforeAndEndIsAfter(
                        userId, now, now, SORT_BY_START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findByBooker_IdAndEndIsBefore(userId, now, SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBooker_IdAndStartIsAfter(userId, now, SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findByBooker_IdAndStatus(
                        userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBooker_IdAndStatus(
                        userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                bookings = bookingRepository.findByBooker_Id(userId, SORT_BY_START_DESC);
        }
        return bookings.stream().map(BookingMapper::toBookingResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long userId, String state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        BookingState bookingState = parseState(state);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;
        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(
                        userId, now, now, SORT_BY_START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findByItem_Owner_IdAndEndIsBefore(userId, now, SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findByItem_Owner_IdAndStartIsAfter(userId, now, SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(
                        userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findByItem_Owner_IdAndStatus(
                        userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                bookings = bookingRepository.findByItem_Owner_Id(userId, SORT_BY_START_DESC);
        }
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
