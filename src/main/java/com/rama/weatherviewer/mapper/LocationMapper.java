package com.rama.weatherviewer.mapper;

import com.rama.weatherviewer.dto.LocationDto;
import com.rama.weatherviewer.entity.Location;
import com.rama.weatherviewer.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LocationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    Location toLocation(LocationDto locationDto, User user);
}
