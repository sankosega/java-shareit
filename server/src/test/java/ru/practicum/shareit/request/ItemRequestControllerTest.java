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
import ru.practicum.shareit.util.ShareItHeaders;

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

    private ItemRequestResponseDto sample() {
        return new ItemRequestResponseDto(1L, "Need a drill", LocalDateTime.now(), List.of());
    }

    @Test
    void createRequest_returnsCreatedRequest() throws Exception {
        ItemRequestDto dto = new ItemRequestDto("Need a drill");
        when(itemRequestService.createRequest(eq(1L), any())).thenReturn(sample());

        mockMvc.perform(post("/requests")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }

    @Test
    void getOwnRequests_returnsList() throws Exception {
        when(itemRequestService.getOwnRequests(1L)).thenReturn(List.of(sample()));

        mockMvc.perform(get("/requests")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllRequests_returnsList() throws Exception {
        when(itemRequestService.getAllRequests(1L)).thenReturn(List.of(sample()));

        mockMvc.perform(get("/requests/all")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getRequestById_returnsRequest() throws Exception {
        when(itemRequestService.getRequestById(1L, 1L)).thenReturn(sample());

        mockMvc.perform(get("/requests/1")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Need a drill"));
    }
}
