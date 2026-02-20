package com.rama.weatherviewer.controller;

import com.rama.weatherviewer.dto.AuthResult;
import com.rama.weatherviewer.dto.WeatherDto;
import com.rama.weatherviewer.entity.Location;
import com.rama.weatherviewer.repository.LocationRepository;
import com.rama.weatherviewer.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final WeatherService weatherService;
    private final LocationRepository locationRepository;

    @GetMapping("/")
    public String redirectToMainPage() {
        return "redirect:/weather";
    }

    @GetMapping("/weather")
    public String getMainPage(@RequestAttribute("auth") AuthResult authResult, Model model) {
        List<WeatherDto> weatherList = List.of();

        if (authResult.isAuthenticated()) {
            List<Location> locations = locationRepository.findByUsername(authResult.getUser().getId());
            weatherList = weatherService.getWeatherForLocations(locations);
        }

        model.addAttribute("auth", authResult);
        model.addAttribute("weatherList", weatherList);

        return "index";
    }
}
