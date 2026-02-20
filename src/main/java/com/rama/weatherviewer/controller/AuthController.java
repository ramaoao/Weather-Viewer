package com.rama.weatherviewer.controller;

import com.rama.weatherviewer.dto.UserLoginDto;
import com.rama.weatherviewer.dto.UserRegistrationDto;
import com.rama.weatherviewer.exception.AuthException;
import com.rama.weatherviewer.exception.UserAlreadyExistsException;
import com.rama.weatherviewer.service.AuthService;
import com.rama.weatherviewer.util.CookieUtils;
import com.rama.weatherviewer.validator.UserValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserValidator userValidator;

    @GetMapping("/sign-up")
    public String getSignUpPage(Model model) {
        model.addAttribute("userRegistrationDto", new UserRegistrationDto());

        return "sign-up";
    }

    @PostMapping("/sign-up")
    public String signUp(@Valid @ModelAttribute("userRegistrationDto") UserRegistrationDto userRegistrationDto, BindingResult bindingResult, HttpServletResponse response) {
        userValidator.validate(userRegistrationDto, bindingResult);

        if (bindingResult.hasErrors()) {
            clearPassword(userRegistrationDto);

            return "sign-up";
        }

        try {
            UUID sessionId = authService.register(userRegistrationDto.getUsername(), userRegistrationDto.getPassword());
            log.info("User {} registered in successfully.", userRegistrationDto.getUsername());

            CookieUtils.setSessionCookie(response, sessionId.toString());

            return "redirect:/weather";
        } catch (UserAlreadyExistsException e) {
            log.warn("Race condition: Username {} already token.", userRegistrationDto.getUsername());

            bindingResult.rejectValue("username", "duplicate", e.getMessage());
            clearPassword(userRegistrationDto);

            return "sign-up";
        }
    }

    @GetMapping("/sign-in")
    public String getSignInPage(Model model) {
        model.addAttribute("userLoginDto", new UserLoginDto());

        return "sign-in";
    }

    @PostMapping("/sign-in")
    public String signIn(@Valid @ModelAttribute("userLoginDto") UserLoginDto userLoginDto, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            userLoginDto.setPassword("");
            bindingResult.reject("auth.error", "Invalid username or password");

            return "sign-in";
        }

        try {
            UUID sessionId = authService.login(userLoginDto.getUsername(), userLoginDto.getPassword());
            log.info("User {} logged in successfully.", userLoginDto.getUsername());

            CookieUtils.setSessionCookie(response, sessionId.toString());

            return "redirect:/weather";
        } catch (AuthException e) {
            log.warn("Failed login attempt for user: {}", userLoginDto.getUsername());

            bindingResult.reject("auth.error", e.getMessage());
            userLoginDto.setPassword("");

            return "sign-in";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.findSessionCookie(request)
                .ifPresent(cookie -> {
                    authService.logout(cookie.getValue()).ifPresent(uuid -> {
                        log.info("Session {} successfully deleted", uuid);
                    });

                    CookieUtils.invalidateCookie(cookie, response);
                });

        return "redirect:/weather";
    }

    private void clearPassword(UserRegistrationDto userRegistrationDto) {
        userRegistrationDto.setPassword("");
        userRegistrationDto.setConfirmPassword("");
    }
}
