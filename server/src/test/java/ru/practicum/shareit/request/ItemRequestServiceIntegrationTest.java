package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    private Long userId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        userId = userRepository.save(new User(null, "User One", "user1@test.com")).getId();
        otherUserId = userRepository.save(new User(null, "User Two", "user2@test.com")).getId();
    }

    @Test
    void createRequest_shouldPersistAndReturn() {
        ItemRequestDto dto = new ItemRequestDto(null, "Need a drill", null);
        ItemRequestResponseDto result = itemRequestService.createRequest(userId, dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getOwnRequests_shouldReturnOnlyOwnRequests() {
        itemRequestService.createRequest(userId, new ItemRequestDto(null, "Request 1", null));
        itemRequestService.createRequest(userId, new ItemRequestDto(null, "Request 2", null));
        itemRequestService.createRequest(otherUserId, new ItemRequestDto(null, "Other request", null));

        List<ItemRequestResponseDto> result = itemRequestService.getOwnRequests(userId);
        assertThat(result).hasSize(2);
    }

    @Test
    void getAllRequests_shouldReturnOtherUsersRequests() {
        itemRequestService.createRequest(userId, new ItemRequestDto(null, "My request", null));
        itemRequestService.createRequest(otherUserId, new ItemRequestDto(null, "Other request", null));

        List<ItemRequestResponseDto> result = itemRequestService.getAllRequests(userId);
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Other request");
    }

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        ItemRequestResponseDto created = itemRequestService.createRequest(userId,
                new ItemRequestDto(null, "Need something", null));
        ItemRequestResponseDto result = itemRequestService.getRequestById(otherUserId, created.getId());

        assertThat(result.getId()).isEqualTo(created.getId());
        assertThat(result.getDescription()).isEqualTo("Need something");
    }
}
