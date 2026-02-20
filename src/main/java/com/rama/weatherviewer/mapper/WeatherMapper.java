package com.rama.weatherviewer.mapper;

import com.rama.weatherviewer.dto.WeatherDto;
import com.rama.weatherviewer.entity.Location;
import com.rama.weatherviewer.entity.WeatherApiResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WeatherMapper {
    @Mapping(target = "locationId", source = "location.id")
    @Mapping(target = "name", source = "location.name")
    @Mapping(target = "country", source = "response.sys.country")
    @Mapping(target = "temperature", source = "response.main.temp")
    @Mapping(target = "feelsLike", source = "response.main.feelsLike")
    @Mapping(target = "humidity", source = "response.main.humidity")
    @Mapping(target = "description", expression = "java(getDescription(response))")
    @Mapping(target = "icon", expression = "java(getIcon(response))")
    WeatherDto toWeatherDto(WeatherApiResponse response, Location location);

    default String getDescription(WeatherApiResponse response) {
        if (response.getWeathers() != null && !response.getWeathers().isEmpty()) {
            return response.getWeathers().getFirst().getDescription();
        }
        return null;
    }

    default String getIcon(WeatherApiResponse response) {
        if (response.getWeathers() != null && !response.getWeathers().isEmpty()) {
            return response.getWeathers().getFirst().getIcon();
        }
        return null;
    }
}
