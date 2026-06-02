package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserService userService;

    private Long requestorId;
    private Long otherUserId;

    @BeforeEach
    void setUp() {
        UserDto requestor = userService.createUser(new UserDto(null, "Requestor", "req@req.com"));
        requestorId = requestor.getId();
        UserDto other = userService.createUser(new UserDto(null, "Other", "other@req.com"));
        otherUserId = other.getId();
    }

    @Test
    void createRequest_savesAndReturnsRequest() {
        ItemRequestDto dto = new ItemRequestDto("Need a drill");

        ItemRequestResponseDto result = itemRequestService.createRequest(requestorId, dto);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void createRequest_unknownUser_throwsNotFound() {
        assertThatThrownBy(() -> itemRequestService.createRequest(999L, new ItemRequestDto("desc")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwnRequests_returnsOnlyRequestorRequests() {
        itemRequestService.createRequest(requestorId, new ItemRequestDto("Need A"));
        itemRequestService.createRequest(requestorId, new ItemRequestDto("Need B"));
        itemRequestService.createRequest(otherUserId, new ItemRequestDto("Need C"));

        List<ItemRequestResponseDto> own = itemRequestService.getOwnRequests(requestorId);

        assertThat(own).hasSize(2);
    }

    @Test
    void getAllRequests_excludesOwnRequests() {
        itemRequestService.createRequest(requestorId, new ItemRequestDto("Mine"));
        itemRequestService.createRequest(otherUserId, new ItemRequestDto("Others"));

        List<ItemRequestResponseDto> all = itemRequestService.getAllRequests(requestorId);

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getDescription()).isEqualTo("Others");
    }

    @Test
    void getRequestById_returnsCorrectRequest() {
        ItemRequestResponseDto created = itemRequestService.createRequest(requestorId,
                new ItemRequestDto("Specific request"));

        ItemRequestResponseDto found = itemRequestService.getRequestById(otherUserId, created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getDescription()).isEqualTo("Specific request");
    }

    @Test
    void getRequestById_notFound_throwsNotFound() {
        assertThatThrownBy(() -> itemRequestService.getRequestById(requestorId, 999L))
                .isInstanceOf(NotFoundException.class);
    }
}
