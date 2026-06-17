package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void createRequest_shouldReturnCreated() throws Exception {
        ItemRequestDto input = new ItemRequestDto(null, "Need a drill", null);
        ItemRequestResponseDto output = new ItemRequestResponseDto(1L, "Need a drill",
                LocalDateTime.now(), List.of());
        when(itemRequestService.createRequest(eq(1L), any())).thenReturn(output);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void getOwnRequests_shouldReturnList() throws Exception {
        when(itemRequestService.getOwnRequests(1L)).thenReturn(List.of(
                new ItemRequestResponseDto(1L, "Need a drill", LocalDateTime.now(), List.of())));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllRequests_shouldReturnOthersRequests() throws Exception {
        when(itemRequestService.getAllRequests(1L)).thenReturn(List.of(
                new ItemRequestResponseDto(2L, "Need screwdriver", LocalDateTime.now(), List.of())));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRequestById_shouldReturnRequest() throws Exception {
        when(itemRequestService.getRequestById(eq(1L), eq(2L))).thenReturn(
                new ItemRequestResponseDto(2L, "Need something", LocalDateTime.now(), List.of()));

        mockMvc.perform(get("/requests/2")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L));
    }
}
