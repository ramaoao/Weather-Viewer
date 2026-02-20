package com.rama.weatherviewer.handler;

import com.rama.weatherviewer.exception.AccessDeniedException;
import com.rama.weatherviewer.exception.LocationNotFoundException;
import com.rama.weatherviewer.exception.UserNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(LocationNotFoundException.class)
    public String handleLocationNotFoundException(LocationNotFoundException e, Model model, HttpServletRequest request) {
        log.warn("Resources access issue: message='{}', IP='{}', URL='{}'", e.getMessage(), request.getRemoteAddr(), request.getRequestURI());

        model.addAttribute("errorMessage", "Location not found or unavailable.");

        return "index";
    }

    @ExceptionHandler(UserNotFoundException.class)
    public String handleUserNotFoundException(UserNotFoundException e, HttpServletRequest request) {
        log.error("Security/Consistency error: authenticate user not found in database. Message='{}', IP='{}'", e.getMessage(), request.getRemoteAddr());

        return "redirect:/auth/sign-in";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDeniedException(AccessDeniedException e, RedirectAttributes redirectAttributes) {
        log.warn("Access violation attempt: {}", e.getMessage());

        redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to do that.");

        return "redirect:/weather";
    }

    @ExceptionHandler(Exception.class)
    public String handleAllUncaughtException(Exception e, Model model, HttpServletRequest request) {
        log.error("Uncaught exception occurred. Message='{}', IP='{}', URL='{}'", e.getMessage(), request.getRemoteAddr(), request.getRequestURI());

        model.addAttribute("errorMessage", "Something went wrong on uor side. We are already looking into it.");

        return "error";
    }
}
