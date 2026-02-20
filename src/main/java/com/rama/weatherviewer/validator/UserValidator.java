package com.rama.weatherviewer.validator;

import com.rama.weatherviewer.dto.UserRegistrationDto;
import com.rama.weatherviewer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserValidator implements Validator {
    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserRegistrationDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserRegistrationDto userRequest = (UserRegistrationDto) target;

        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            errors.rejectValue("username", "duplicate", "This username is already taken.");
        }

        if (userRequest.getPassword().contains(userRequest.getUsername())) {
            errors.rejectValue("password", "insecure", "Password must not contain your username.");
        }

        if (!Objects.equals(userRequest.getPassword(), userRequest.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "mismatch", "Password do not match.");
        }
    }
}
