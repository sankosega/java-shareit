package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @Test
    void addBooking_shouldReturnCreatedBooking() throws Exception {
        BookingDto input = new BookingDto(1L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        BookingResponseDto output = new BookingResponseDto(1L, input.getStart(), input.getEnd(),
                null, null, BookingStatus.WAITING);
        when(bookingService.addBooking(eq(1L), any())).thenReturn(output);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void approveBooking_shouldReturnApproved() throws Exception {
        BookingResponseDto output = new BookingResponseDto(1L, LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2), null, null, BookingStatus.APPROVED);
        when(bookingService.approveBooking(eq(1L), eq(1L), eq(true))).thenReturn(output);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingsByBooker_shouldReturnList() throws Exception {
        when(bookingService.getBookingsByBooker(eq(1L), eq("ALL"))).thenReturn(List.of(
                new BookingResponseDto(1L, LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2), null, null, BookingStatus.WAITING)));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBookingsByOwner_shouldReturnList() throws Exception {
        when(bookingService.getBookingsByOwner(eq(1L), eq("ALL"))).thenReturn(List.of(
                new BookingResponseDto(2L, LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(2), null, null, BookingStatus.WAITING)));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
