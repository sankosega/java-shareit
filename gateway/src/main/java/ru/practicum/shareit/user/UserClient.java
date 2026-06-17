package ru.practicum.shareit.user;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserDto;

@Service
public class UserClient extends BaseClient {

    private static final String API_PREFIX = "/users";

    public UserClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> createUser(UserDto userDto) {
        return post(API_PREFIX, 0L, userDto);
    }

    public ResponseEntity<Object> updateUser(long userId, UserDto userDto) {
        return patch(API_PREFIX + "/" + userId, userId, userDto);
    }

    public ResponseEntity<Object> getUserById(long userId) {
        return get(API_PREFIX + "/" + userId, userId);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get(API_PREFIX, 0L);
    }

    public ResponseEntity<Object> deleteUser(long userId) {
        return delete(API_PREFIX + "/" + userId, userId);
    }
}
