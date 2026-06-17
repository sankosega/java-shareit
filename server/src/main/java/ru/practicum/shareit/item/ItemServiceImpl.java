package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    @Transactional
    public ItemDto addItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found: " + itemDto.getRequestId()));
            item.setRequest(request);
        }
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        if (!existing.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the owner can edit the item");
        }
        ItemMapper.updateItemFromDto(existing, itemDto);
        return ItemMapper.toItemDto(itemRepository.save(existing));
    }

    @Override
    public ItemResponseDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        List<CommentDto> comments = commentRepository.findAllByItem_Id(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        boolean isOwner = item.getOwner().getId().equals(userId);
        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;
        if (isOwner) {
            List<Booking> bookings = bookingRepository.findByItem_IdAndStatusOrderByStartAsc(
                    itemId, BookingStatus.APPROVED);
            LocalDateTime now = LocalDateTime.now();
            lastBooking = bookings.stream()
                    .filter(b -> b.getEnd().isBefore(now))
                    .reduce((first, second) -> second)
                    .map(b -> new BookingShortDto(b.getId(), b.getBooker().getId()))
                    .orElse(null);
            nextBooking = bookings.stream()
                    .filter(b -> b.getStart().isAfter(now))
                    .findFirst()
                    .map(b -> new BookingShortDto(b.getId(), b.getBooker().getId()))
                    .orElse(null);
        }
        return ItemMapper.toItemResponseDto(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemResponseDto> getAllItemsByOwner(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        LocalDateTime now = LocalDateTime.now();
        return itemRepository.findAllByOwner_Id(userId).stream()
                .map(item -> {
                    List<CommentDto> comments = commentRepository.findAllByItem_Id(item.getId()).stream()
                            .map(CommentMapper::toCommentDto)
                            .collect(Collectors.toList());
                    List<Booking> bookings = bookingRepository.findByItem_IdAndStatusOrderByStartAsc(
                            item.getId(), BookingStatus.APPROVED);
                    BookingShortDto last = bookings.stream()
                            .filter(b -> b.getEnd().isBefore(now))
                            .reduce((first, second) -> second)
                            .map(b -> new BookingShortDto(b.getId(), b.getBooker().getId()))
                            .orElse(null);
                    BookingShortDto next = bookings.stream()
                            .filter(b -> b.getStart().isAfter(now))
                            .findFirst()
                            .map(b -> new BookingShortDto(b.getId(), b.getBooker().getId()))
                            .orElse(null);
                    return ItemMapper.toItemResponseDto(item, last, next, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
        if (!bookingRepository.existsByBooker_IdAndItem_IdAndEndIsBefore(userId, itemId, LocalDateTime.now())) {
            throw new ValidationException("User has no completed booking for this item");
        }
        Comment comment = CommentMapper.toComment(commentDto, item, author);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }
}
