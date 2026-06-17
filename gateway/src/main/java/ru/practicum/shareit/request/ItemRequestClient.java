package ru.practicum.shareit.request;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Service
public class ItemRequestClient extends BaseClient {

    private static final String API_PREFIX = "/requests";

    public ItemRequestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> createRequest(long userId, ItemRequestDto dto) {
        return post(API_PREFIX, userId, dto);
    }

    public ResponseEntity<Object> getOwnRequests(long userId) {
        return get(API_PREFIX, userId);
    }

    public ResponseEntity<Object> getAllRequests(long userId) {
        return get(API_PREFIX + "/all", userId);
    }

    public ResponseEntity<Object> getRequestById(long userId, long requestId) {
        return get(API_PREFIX + "/" + requestId, userId);
    }
}
