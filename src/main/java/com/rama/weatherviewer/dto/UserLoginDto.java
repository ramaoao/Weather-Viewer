package com.rama.weatherviewer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserLoginDto {
    @NotBlank(message = "Username cannot be empty.")
    @Size(max = 20)
    String username;

    @NotBlank(message = "Password cannot be empty.")
    @Size(max = 72)
    String password;
}
