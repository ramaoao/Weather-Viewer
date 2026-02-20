package com.rama.weatherviewer.dto;

import lombok.Value;

@Value
public class AuthResult {
    UserResponseDto user;

    public static AuthResult guest() {
        return new AuthResult(null);
    }

    public static AuthResult authorized(UserResponseDto user) {
        return new AuthResult(user);
    }

    public boolean isAuthenticated() {
        return user != null;
    }
}
