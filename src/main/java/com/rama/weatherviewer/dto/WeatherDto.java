package com.rama.weatherviewer.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WeatherDto {
    Long locationId;
    String name;
    String country;
    Integer temperature;
    Integer feelsLike;
    String description;
    Integer humidity;
    String icon;
}
