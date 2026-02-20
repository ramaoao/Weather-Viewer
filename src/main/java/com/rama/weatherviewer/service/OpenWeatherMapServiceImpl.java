package com.rama.weatherviewer.service;

import com.rama.weatherviewer.dto.LocationDto;
import com.rama.weatherviewer.dto.WeatherDto;
import com.rama.weatherviewer.entity.Location;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class OpenWeatherMapServiceImpl implements WeatherService {
    private final RestClient restClient;
    private final WeatherApiClient weatherApiClient;
    private final Executor weatherExecutor;

    public OpenWeatherMapServiceImpl(RestClient restClient, WeatherApiClient weatherApiClient, Executor weatherExecutor) {
        this.weatherApiClient = weatherApiClient;
        this.restClient = restClient;
        this.weatherExecutor = weatherExecutor;
    }

    @Override
    public List<LocationDto> findLocationsByName(String cityName) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geo/1.0/direct")
                        .queryParam("q", cityName)
                        .queryParam("limit", 8)
                        .queryParam("appid", "{appid}")
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Override
    public List<WeatherDto> getWeatherForLocations(List<Location> locations) {
        List<CompletableFuture<WeatherDto>> futures = locations.stream()
                .map(location -> CompletableFuture.supplyAsync(() -> weatherApiClient.fetchWeather(location), weatherExecutor))
                .toList();

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }
}
