package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.util.ShareItHeaders;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto createRequest(
            @RequestHeader(ShareItHeaders.X_SHARER_USER_ID) Long userId,
            @RequestBody ItemRequestDto dto) {
        return itemRequestService.createRequest(userId, dto);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getOwnRequests(
            @RequestHeader(ShareItHeaders.X_SHARER_USER_ID) Long userId) {
        return itemRequestService.getOwnRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAllRequests(
            @RequestHeader(ShareItHeaders.X_SHARER_USER_ID) Long userId) {
        return itemRequestService.getAllRequests(userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getRequestById(
            @RequestHeader(ShareItHeaders.X_SHARER_USER_ID) Long userId,
            @PathVariable Long requestId) {
        return itemRequestService.getRequestById(userId, requestId);
    }
}
