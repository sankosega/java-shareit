package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.util.ShareItHeaders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void addItem_returnsItem() throws Exception {
        ItemDto request = new ItemDto(null, "Drill", "Electric", true, null);
        ItemDto response = new ItemDto(1L, "Drill", "Electric", true, null);
        when(itemService.addItem(eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/items")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getItemById_returnsItemResponse() throws Exception {
        ItemResponseDto response = new ItemResponseDto(1L, "Drill", "Electric", true, null, null, null, List.of());
        when(itemService.getItemById(eq(1L), eq(1L))).thenReturn(response);

        mockMvc.perform(get("/items/1")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getAllItemsByOwner_returnsList() throws Exception {
        when(itemService.getAllItemsByOwner(1L)).thenReturn(List.of(
                new ItemResponseDto(1L, "Item1", "Desc1", true, null, null, null, List.of())
        ));

        mockMvc.perform(get("/items")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchItems_returnsMatchingItems() throws Exception {
        when(itemService.searchItems("drill")).thenReturn(List.of(
                new ItemDto(1L, "Drill", "Electric drill", true, null)
        ));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void updateItem_returnsUpdatedItem() throws Exception {
        ItemDto update = new ItemDto(null, "Updated Drill", null, null, null);
        when(itemService.updateItem(eq(1L), eq(1L), any())).thenReturn(
                new ItemDto(1L, "Updated Drill", "Electric", true, null));

        mockMvc.perform(patch("/items/1")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Drill"));
    }

    @Test
    void addComment_returnsComment() throws Exception {
        CommentDto request = new CommentDto(null, "Great item!", null, null);
        CommentDto response = new CommentDto(1L, "Great item!", "Alice", null);
        when(itemService.addComment(eq(1L), eq(1L), any())).thenReturn(response);

        mockMvc.perform(post("/items/1/comment")
                        .header(ShareItHeaders.X_SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Great item!"));
    }
}
