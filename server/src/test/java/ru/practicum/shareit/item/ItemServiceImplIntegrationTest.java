package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private Long ownerId;

    @BeforeEach
    void setUp() {
        UserDto owner = userService.createUser(new UserDto(null, "Owner", "owner@item.com"));
        ownerId = owner.getId();
    }

    @Test
    void addItem_savesAndReturnsItem() {
        ItemDto dto = new ItemDto(null, "Drill", "Electric drill", true, null);

        ItemDto saved = itemService.addItem(ownerId, dto);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Drill");
    }

    @Test
    void addItem_unknownOwner_throwsNotFound() {
        ItemDto dto = new ItemDto(null, "Item", "Desc", true, null);

        assertThatThrownBy(() -> itemService.addItem(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllItemsByOwner_returnsOwnerItems() {
        itemService.addItem(ownerId, new ItemDto(null, "Item1", "Desc1", true, null));
        itemService.addItem(ownerId, new ItemDto(null, "Item2", "Desc2", true, null));

        List<ItemResponseDto> items = itemService.getAllItemsByOwner(ownerId);

        assertThat(items).hasSize(2);
    }

    @Test
    void searchItems_returnsMatchingItems() {
        itemService.addItem(ownerId, new ItemDto(null, "Hammer", "Heavy hammer", true, null));
        itemService.addItem(ownerId, new ItemDto(null, "Screwdriver", "Small screwdriver", true, null));

        List<ItemDto> result = itemService.searchItems("hammer");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Hammer");
    }

    @Test
    void searchItems_emptyText_returnsEmptyList() {
        itemService.addItem(ownerId, new ItemDto(null, "Drill", "Drill desc", true, null));

        List<ItemDto> result = itemService.searchItems("");

        assertThat(result).isEmpty();
    }

    @Test
    void updateItem_updatesFields() {
        ItemDto saved = itemService.addItem(ownerId, new ItemDto(null, "Old", "Old desc", true, null));

        ItemDto updated = itemService.updateItem(ownerId, saved.getId(),
                new ItemDto(null, "New", null, null, null));

        assertThat(updated.getName()).isEqualTo("New");
        assertThat(updated.getDescription()).isEqualTo("Old desc");
    }
}
