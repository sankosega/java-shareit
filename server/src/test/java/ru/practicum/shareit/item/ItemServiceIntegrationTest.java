package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

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
}
