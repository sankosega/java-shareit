package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    @NotBlank(message = "Name must not be blank", groups = Create.class)
    private String name;

    @NotBlank(message = "Email must not be blank", groups = Create.class)
    @Email(message = "Email must be valid")
    private String email;

    public interface Create {
    }
}
