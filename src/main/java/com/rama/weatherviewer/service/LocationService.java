package com.rama.weatherviewer.service;

import com.rama.weatherviewer.dto.LocationDto;
import com.rama.weatherviewer.entity.Location;
import com.rama.weatherviewer.entity.User;
import com.rama.weatherviewer.exception.AccessDeniedException;
import com.rama.weatherviewer.exception.LocationAlreadyExistsException;
import com.rama.weatherviewer.exception.LocationNotFoundException;
import com.rama.weatherviewer.mapper.LocationMapper;
import com.rama.weatherviewer.repository.LocationRepository;
import com.rama.weatherviewer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final UserRepository userRepository;

    @Transactional
    public void save(LocationDto locationDto, Long userId) {
        try {
            User user = userRepository.getReference(userId);

            Location location = locationMapper.toLocation(locationDto, user);

            locationRepository.saveAndFlush(location);
        } catch (DataIntegrityViolationException e) {
            throw new LocationAlreadyExistsException("You have already added this location.");
        }
    }

    @Transactional
    public void delete(Long locationId, String username) {
        Location location = locationRepository.find(locationId)
                .orElseThrow(() -> new LocationNotFoundException("Location ID " + locationId + " not found."));

        if (!location.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("Access violation: User " + username + " tried to delete Location ID " + locationId + " owned by " + location.getUser().getUsername());
        }

        locationRepository.deleteById(location.getId());
    }
}
