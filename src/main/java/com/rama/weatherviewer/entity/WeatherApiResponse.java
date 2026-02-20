package com.rama.weatherviewer.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherApiResponse {
    @JsonProperty("weather")
    List<Weather> weathers;
    Main main;
    Sys sys;

    @Data
    public static class Weather {
        String description;
        String icon;
    }

    @Data
    public static class Main {
        Double temp;
        @JsonProperty("feels_like")
        Double feelsLike;
        Integer humidity;
    }

    @Data
    public static class Sys {
        String country;
    }
}
