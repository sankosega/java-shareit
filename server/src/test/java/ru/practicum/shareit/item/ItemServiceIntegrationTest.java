package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = new User(null, "Test User", "test@test.com");
        userId = userRepository.save(user).getId();
    }

    @Test
    void addItem_shouldPersistAndReturnItem() {
        ItemDto dto = new ItemDto(null, "Drill", "Power drill", true, null);
        ItemDto result = itemService.addItem(userId, dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Drill");
        assertThat(result.getDescription()).isEqualTo("Power drill");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void getAllItemsByOwner_shouldReturnOwnerItems() {
        itemService.addItem(userId, new ItemDto(null, "Item1", "Desc1", true, null));
        itemService.addItem(userId, new ItemDto(null, "Item2", "Desc2", true, null));

        List<ItemResponseDto> items = itemService.getAllItemsByOwner(userId);

        assertThat(items).hasSize(2);
    }

    @Test
    void searchItems_withBlankText_shouldReturnEmpty() {
        itemService.addItem(userId, new ItemDto(null, "Searchable", "findme", true, null));
        assertThat(itemService.searchItems("")).isEmpty();
        assertThat(itemService.searchItems("   ")).isEmpty();
    }

    @Test
    void searchItems_withText_shouldReturnMatching() {
        itemService.addItem(userId, new ItemDto(null, "Hammer", "Heavy tool", true, null));
        itemService.addItem(userId, new ItemDto(null, "Screwdriver", "Small tool", true, null));

        List<ItemDto> result = itemService.searchItems("hammer");
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Hammer");
    }

    @Test
    void addItem_withRequestId_shouldLinkRequest() {
        User requestor = userRepository.save(new User(null, "Req", "req@isit.com"));
        ItemRequest request = new ItemRequest();
        request.setDescription("Need drill");
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        Long requestId = itemRequestRepository.save(request).getId();

        ItemDto result = itemService.addItem(userId,
                new ItemDto(null, "Drill", "Heavy", true, requestId));

        assertThat(result.getRequestId()).isEqualTo(requestId);
    }

    @Test
    void getItemById_asOwner_shouldReturnResponseDto() {
        ItemDto added = itemService.addItem(userId, new ItemDto(null, "Saw", "Sharp", true, null));

        ItemResponseDto result = itemService.getItemById(added.getId(), userId);

        assertThat(result.getId()).isEqualTo(added.getId());
        assertThat(result.getName()).isEqualTo("Saw");
    }

    @Test
    void getItemById_asNonOwner_shouldReturnWithoutBookings() {
        ItemDto added = itemService.addItem(userId, new ItemDto(null, "Saw", "Sharp", true, null));
        User other = userRepository.save(new User(null, "Other", "other@isit.com"));

        ItemResponseDto result = itemService.getItemById(added.getId(), other.getId());

        assertThat(result.getId()).isEqualTo(added.getId());
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    void getItemById_notFound_shouldThrow() {
        assertThatThrownBy(() -> itemService.getItemById(999L, userId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateItem_shouldModifyFields() {
        ItemDto added = itemService.addItem(userId, new ItemDto(null, "Old", "OldDesc", true, null));

        ItemDto updated = itemService.updateItem(userId, added.getId(),
                new ItemDto(null, "New", "NewDesc", false, null));

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("NewDesc");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void updateItem_byNonOwner_shouldThrow() {
        ItemDto added = itemService.addItem(userId, new ItemDto(null, "Drill", "Heavy", true, null));
        User other = userRepository.save(new User(null, "Other", "other2@isit.com"));

        assertThatThrownBy(() -> itemService.updateItem(other.getId(), added.getId(),
                new ItemDto(null, "Hacked", null, null, null)))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void addComment_shouldSave() {
        User booker = userRepository.save(new User(null, "Booker", "booker@isit.com"));
        ItemDto added = itemService.addItem(userId, new ItemDto(null, "Drill", "Heavy", true, null));
        createPastBooking(booker.getId(), added.getId());

        CommentDto result = itemService.addComment(booker.getId(), added.getId(),
                new CommentDto(null, "Great item!", null, null));

        assertThat(result.getText()).isEqualTo("Great item!");
        assertThat(result.getAuthorName()).isEqualTo("Booker");
    }

    @Test
    void addComment_withoutCompletedBooking_shouldThrow() {
        ItemDto added = itemService.addItem(userId, new ItemDto(null, "Drill", "Heavy", true, null));
        User other = userRepository.save(new User(null, "Other", "other3@isit.com"));

        assertThatThrownBy(() -> itemService.addComment(other.getId(), added.getId(),
                new CommentDto(null, "No booking", null, null)))
                .isInstanceOf(ValidationException.class);
    }

    private void createPastBooking(Long bookerId, Long bookedItemId) {
        User booker = userRepository.findById(bookerId).orElseThrow();
        Item item = itemRepository.findById(bookedItemId).orElseThrow();
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusMinutes(1));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(booking);
    }
}
