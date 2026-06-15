package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GatewayControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer mockServer;

    private static final String USER_HEADER = "X-Sharer-User-Id";
    private static final String JSON_BODY = "{}";

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    // ===== USER =====

    @Test
    void createUser_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/users")))
                .andExpect(method(POST))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDto(null, "Alice", "alice@test.com"))))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void createUser_invalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"bad\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/users/1")))
                .andExpect(method(PATCH))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserDto(null, "Bob", "bob@test.com"))))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getUserById_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/users/1")))
                .andExpect(method(GET))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getAllUsers_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/users")))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void deleteUser_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/users/1")))
                .andExpect(method(DELETE))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getUserById_serverError_shouldReturn4xx() throws Exception {
        mockServer.expect(requestTo(containsString("/users/999")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());

        mockServer.verify();
    }

    // ===== ITEM =====

    @Test
    void addItem_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/items")))
                .andExpect(method(POST))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ItemDto(null, "Drill", "Heavy", true, null))))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void addItem_invalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/items")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"available\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/items/1")))
                .andExpect(method(PATCH))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(patch("/items/1")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ItemDto(null, "Updated", null, null, null))))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getItemById_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/items/1")))
                .andExpect(method(GET))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/items/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getAllItemsByOwner_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/items")))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/items")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void searchItems_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/items/search")))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/items/search")
                        .header(USER_HEADER, 1L)
                        .param("text", "drill"))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void addComment_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/items/1/comment")))
                .andExpect(method(POST))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CommentDto("Great!"))))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    // ===== BOOKING =====

    @Test
    void addBooking_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/bookings")))
                .andExpect(method(POST))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        BookingDto dto = new BookingDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void addBooking_invalidDates_shouldReturn400() throws Exception {
        BookingDto dto = new BookingDto(1L,
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addBooking_invalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/bookings")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":null,\"start\":null,\"end\":null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/bookings/1")))
                .andExpect(method(PATCH))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(patch("/bookings/1")
                        .header(USER_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getBookingById_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/bookings/1")))
                .andExpect(method(GET))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/bookings/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getBookingsByBooker_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/bookings")))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getBookingsByBooker_invalidState_shouldReturn400() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(USER_HEADER, 1L)
                        .param("state", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsByOwner_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/bookings/owner")))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "FUTURE"))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getBookingsByOwner_invalidState_shouldReturn400() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(USER_HEADER, 1L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());
    }

    // ===== ITEM REQUEST =====

    @Test
    void createRequest_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/requests")))
                .andExpect(method(POST))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ItemRequestDto("Need a drill"))))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void createRequest_invalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/requests")
                        .header(USER_HEADER, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwnRequests_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/requests")))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/requests")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getAllRequests_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/requests/all")))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/requests/all")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        mockServer.verify();
    }

    @Test
    void getRequestById_shouldDelegate() throws Exception {
        mockServer.expect(requestTo(containsString("/requests/1")))
                .andExpect(method(GET))
                .andRespond(withSuccess(JSON_BODY, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/requests/1")
                        .header(USER_HEADER, 1L))
                .andExpect(status().isOk());

        mockServer.verify();
    }
}
