package com.rama.weatherviewer.service;

import com.rama.weatherviewer.dto.AuthResult;
import com.rama.weatherviewer.entity.Session;
import com.rama.weatherviewer.entity.User;
import com.rama.weatherviewer.exception.AuthException;
import com.rama.weatherviewer.exception.UserAlreadyExistsException;
import com.rama.weatherviewer.mapper.UserMapper;
import com.rama.weatherviewer.repository.SessionRepository;
import com.rama.weatherviewer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.rama.weatherviewer.util.CookieUtils.SESSION_DURATION_SECONDS;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final UserMapper userMapper;

    @Transactional
    public UUID register(String username, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = userMapper.toUser(username, hashedPassword);

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
                throw new UserAlreadyExistsException("This username is already taken.");
        }

        return createSession(user);
    }

    @Transactional
    public UUID login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .filter(u -> BCrypt.checkpw(password, u.getPassword()))
                .orElseThrow(() -> new AuthException("Invalid username or password."));

        return createSession(user);
    }

    @Transactional
    public Optional<UUID> logout(String sessionId) {
        return parseUuid(sessionId).map(uuid -> {
            sessionRepository.deleteById(uuid);
            return uuid;
        });
    }

    @Transactional(readOnly = true)
    public AuthResult authenticate(String sessionId) {
        return parseUuid(sessionId)
                .flatMap(sessionRepository::find)
                .filter(session -> session.getExpiresAt().isAfter(Instant.now()))
                .map(session -> AuthResult.authorized(userMapper.toResponseDto(session.getUser())))
                .orElse(AuthResult.guest());
    }

    private UUID createSession(User user) {
        UUID sessionId = UUID.randomUUID();

        Session session = Session.builder()
                .uuid(sessionId)
                .user(user)
                .expiresAt(Instant.now().plusSeconds(SESSION_DURATION_SECONDS))
                .build();

        sessionRepository.save(session);
        log.info("Session created for user: {}", user.getUsername());

        return sessionId;
    }

    private Optional<UUID> parseUuid(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(UUID.fromString(sessionId));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
