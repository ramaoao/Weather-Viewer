package com.rama.weatherviewer.service;

import com.rama.weatherviewer.dto.LocationDto;
import com.rama.weatherviewer.dto.WeatherDto;
import com.rama.weatherviewer.entity.Location;

import java.util.List;

public interface WeatherService {
    List<LocationDto> findLocationsByName(String cityName);

    List<WeatherDto> getWeatherForLocations(List<Location> locations);
}
