package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestResponseDto createRequest(Long userId, ItemRequestDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ItemRequest request = new ItemRequest();
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());
        return toResponseDto(itemRequestRepository.save(request), List.of());
    }

    @Override
    public List<ItemRequestResponseDto> getOwnRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_Id(userId, SORT_BY_CREATED_DESC);
        return attachItems(requests);
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestor_IdNot(userId, SORT_BY_CREATED_DESC);
        return attachItems(requests);
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        List<Item> items = itemRepository.findAllByRequest_Id(requestId);
        return toResponseDto(request, items);
    }

    private List<ItemRequestResponseDto> attachItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequest = itemRepository.findAllByRequest_IdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                .map(r -> toResponseDto(r, itemsByRequest.getOrDefault(r.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private ItemRequestResponseDto toResponseDto(ItemRequest request, List<Item> items) {
        List<ItemRequestResponseDto.ItemShortDto> itemDtos = items.stream()
                .map(i -> new ItemRequestResponseDto.ItemShortDto(i.getId(), i.getName(), i.getOwner().getId()))
                .collect(Collectors.toList());
        return new ItemRequestResponseDto(request.getId(), request.getDescription(),
                request.getCreated(), itemDtos);
    }
}
