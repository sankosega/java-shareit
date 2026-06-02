package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryItemRepository {

    private final Map<Long, Item> items = new ConcurrentHashMap<>();
    private long idCounter = 1;

    public Item save(Item item) {
        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(i -> i.getOwner() != null && i.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    public List<Item> searchByText(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String lowerText = text.toLowerCase();
        return new ArrayList<>(items.values()).stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .filter(i -> (i.getName() != null && i.getName().toLowerCase().contains(lowerText))
                        || (i.getDescription() != null && i.getDescription().toLowerCase().contains(lowerText)))
                .collect(Collectors.toList());
    }
}
