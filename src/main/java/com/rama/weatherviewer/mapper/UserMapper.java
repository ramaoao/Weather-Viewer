package com.rama.weatherviewer.mapper;

import com.rama.weatherviewer.dto.UserResponseDto;
import com.rama.weatherviewer.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "password", source = "hashedPassword")
    User toUser(String username, String hashedPassword);

    UserResponseDto toResponseDto(User user);
}
