package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "Item name must not be blank", groups = Create.class)
    private String name;

    @NotBlank(message = "Item description must not be blank", groups = Create.class)
    private String description;

    @NotNull(message = "Available flag must not be null", groups = Create.class)
    private Boolean available;

    private Long requestId;

    public interface Create {
    }
}
