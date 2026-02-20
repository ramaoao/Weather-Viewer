package com.rama.weatherviewer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRegistrationDto {
    @NotBlank(message = "Username cannot be empty.")
    @Size(min = 2, max = 20, message = "Username must be between 2 and 20 characters.")
    String username;

    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 12, max = 72, message = "Password must be between 12 and 72 characters.")
    String password;

    @Size(max = 72)
    String confirmPassword;
}
