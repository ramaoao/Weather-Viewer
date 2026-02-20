package com.rama.weatherviewer.controller;

import com.rama.weatherviewer.dto.AuthResult;
import com.rama.weatherviewer.dto.LocationDto;
import com.rama.weatherviewer.exception.LocationAlreadyExistsException;
import com.rama.weatherviewer.service.LocationService;
import com.rama.weatherviewer.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@Slf4j
@RequestMapping("/weather")
@RequiredArgsConstructor
public class WeatherController {
    private final WeatherService weatherService;
    private final LocationService locationService;

    @GetMapping("/search-results")
    public String getSearchResult(@RequestParam("city") String city, Model model) {
        List<LocationDto> locations = weatherService.findLocationsByName(city);

        model.addAttribute("locations", locations);

        return "search-results";
    }

    @PostMapping("/add-location")
    public String addLocation(@ModelAttribute LocationDto locationDto, RedirectAttributes redirectAttributes, @RequestAttribute("auth") AuthResult authResult) {
        if (!authResult.isAuthenticated()) {
            return "redirect:/auth/sign-in";
        }

        try {
            locationService.save(locationDto, authResult.getUser().getId());
            log.info("User {} added location: {}", authResult.getUser().getUsername(), locationDto.getName());

            return "redirect:/weather";
        } catch (LocationAlreadyExistsException e) {
            log.warn("User {} tried to add duplicate location: {}", authResult.getUser().getUsername(), locationDto.getName());

            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());

            return "redirect:/weather";
        }

    }

    @PostMapping("/delete-location")
    public String deleteLocation(@RequestParam("locationId") Long locationId, @RequestAttribute("auth") AuthResult authResult) {
        locationService.delete(locationId, authResult.getUser().getUsername());
        log.info("User {} delete location ID: {}", authResult.getUser().getUsername(), locationId);

        return "redirect:/weather";
    }
}
