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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.util.ShareItHeaders;

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

    private BookingResponseDto sampleResponse() {
        return new BookingResponseDto(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                new ItemDto(1L, "Bike", "Mountain bike", true, null),
                new UserDto(2L, "Booker", "booker@b.com"),
                BookingStatus.WAITING
        );
    }

    @Test
    void addBooking_returnsBooking() throws Exception {
        BookingDto request = new BookingDto(1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));
        when(bookingService.addBooking(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/bookings")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getBookingById_returnsBooking() throws Exception {
        when(bookingService.getBookingById(1L, 1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/bookings/1")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void approveBooking_returnsApprovedBooking() throws Exception {
        BookingResponseDto approved = sampleResponse();
        approved.setStatus(BookingStatus.APPROVED);
        when(bookingService.approveBooking(eq(1L), eq(1L), eq(true))).thenReturn(approved);

        mockMvc.perform(patch("/bookings/1")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getBookingsByBooker_returnsList() throws Exception {
        when(bookingService.getBookingsByBooker(1L, "ALL")).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/bookings")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getBookingsByOwner_returnsList() throws Exception {
        when(bookingService.getBookingsByOwner(1L, "ALL")).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/bookings/owner")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
