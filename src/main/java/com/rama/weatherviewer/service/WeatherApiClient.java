package com.rama.weatherviewer.service;

import com.rama.weatherviewer.dto.WeatherDto;
import com.rama.weatherviewer.entity.Location;
import com.rama.weatherviewer.entity.WeatherApiResponse;
import com.rama.weatherviewer.mapper.WeatherMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@Slf4j
public class WeatherApiClient {
    private final RestClient restClient;
    private final WeatherMapper weatherMapper;

    public WeatherApiClient(RestClient restClient, WeatherMapper weatherMapper) {
        this.restClient = restClient;
        this.weatherMapper = weatherMapper;
    }

    @Cacheable(value = "weather", key = "#root.args[0].id")
    public WeatherDto fetchWeather(Location location) {
        log.info("Fetching fresh weather data for [Lat: {}, Lon: {}]", location.getLatitude(), location.getLongitude());

        WeatherApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("lat", location.getLatitude())
                        .queryParam("lon", location.getLongitude())
                        .queryParam("units", "metric")
                        .queryParam("appid", "{appid}")
                        .build())
                .retrieve()
                .body(WeatherApiResponse.class);

        return weatherMapper.toWeatherDto(response, location);
    }
}