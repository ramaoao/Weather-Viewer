package com.rama.weatherviewer.service;

import com.rama.weatherviewer.dto.AuthResult;
import com.rama.weatherviewer.entity.Session;
import com.rama.weatherviewer.entity.User;
import com.rama.weatherviewer.exception.UserAlreadyExistsException;
import com.rama.weatherviewer.mapper.UserMapper;
import com.rama.weatherviewer.repository.SessionRepository;
import com.rama.weatherviewer.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Spy
    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @InjectMocks
    private AuthService authService;

    private final String TEST_USERNAME = "testUsername";
    private final String TEST_PASSWORD = "testPassword";
    private final UUID SESSION_ID = UUID.randomUUID();

    @Test
    @DisplayName("register: должен успешно создать пользователя и сессию")
    void shouldRegisterUser() {
        String password = "testPassword";

        UUID sessionId = authService.register(TEST_USERNAME, password);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(BCrypt.checkpw(password, savedUser.getPassword())).isTrue();

        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());

        Session savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getUuid()).isEqualTo(sessionId);
        assertThat(savedSession.getUser()).isEqualTo(savedUser);
    }

    @Test
    @DisplayName("register: должен вернуть AuthException, если пользователь уже зарегестрирован")
    void shouldReturnUserAlreadyExistsExceptionWhenUserAlreadyExists() {
        DataIntegrityViolationException dbError = new DataIntegrityViolationException("Duplicate entry");

        doThrow(dbError).when(userRepository).saveAndFlush(any(User.class));

        assertThatThrownBy(() ->
                authService.register(TEST_USERNAME, TEST_PASSWORD))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("This username is already taken.");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: должен успешно логинить пользователя и создавать сессию")
    void shouldLoginUser() {
        String hashedPassword = BCrypt.hashpw(TEST_PASSWORD, BCrypt.gensalt());

        User existingUser = User.builder()
                .username(TEST_USERNAME)
                .password(hashedPassword)
                .build();

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.ofNullable(existingUser));

        UUID sessionId = authService.login(TEST_USERNAME, TEST_PASSWORD);

        verify(userRepository).findByUsername(TEST_USERNAME);

        ArgumentCaptor<Session> sessionCaptor = ArgumentCaptor.forClass(Session.class);
        verify(sessionRepository).save(sessionCaptor.capture());

        Session savedSession = sessionCaptor.getValue();
        assertThat(savedSession.getUuid()).isEqualTo(sessionId);
        assertThat(savedSession.getUser().getUsername()).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("logout: должен удалять сессию пользователя")
    void shouldLogoutUser() {
        UUID expectedId = UUID.fromString(SESSION_ID.toString());

        Optional<UUID> result = authService.logout(SESSION_ID.toString());

        assertThat(result).isPresent().contains(expectedId);

        ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(sessionRepository).deleteById(uuidCaptor.capture());

        assertThat(uuidCaptor.getValue()).isEqualTo(expectedId);
    }

    @Test
    @DisplayName("logout: не должен падать, если передан кривой ID")
    void shouldNotFailWhenSessionUuidInvalid() {
        Optional<UUID> result = authService.logout("невалидный-id");

        assertThat(result).isEmpty();

        verify(sessionRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("authenticate: должен праверить валидность UUID, наличие сесси в базе и проверка срока годности сессии")
    void shouldAuthenticateUser() {
        User user = User.builder()
                .username("Rama")
                .build();

        Session session = Session.builder()
                .uuid(SESSION_ID)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofHours(1)))
                .build();

        when(sessionRepository.find(SESSION_ID)).thenReturn(Optional.of(session));

        AuthResult result = authService.authenticate(SESSION_ID.toString());

        assertThat(result.isAuthenticated()).isTrue();
        assertThat(result.getUser().getUsername()).isEqualTo("Rama");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"    ", "invalid-id", "123-abc"})
    @DisplayName("authenticate: должен возвращать guest для любого некоректного ввода")
    void shouldReturnGuestForInvalidInput(String invalidInput) {
        AuthResult result = authService.authenticate(invalidInput);

        assertThat(result.isAuthenticated()).isFalse();
        assertThat(result.getUser()).isNull();

        verify(sessionRepository, never()).find(any());
    }

    @Test
    @DisplayName("authenticate: должен вернуть guest, если сессия не найден в бд")
    void shouldReturnGuestWhenSessionNotFound() {
        when(sessionRepository.find(SESSION_ID)).thenReturn(Optional.empty());

        AuthResult result = authService.authenticate(SESSION_ID.toString());

        assertThat(result.isAuthenticated()).isFalse();
        verify(sessionRepository).find(SESSION_ID);
    }

    @Test
    @DisplayName("authenticate: должен вернут guest, если сессия истекал")
    void shouldReturnGuestWhenSessionExpired() {
        User user = User.builder()
                .username("Rama")
                .build();
        Session expiredSession = Session.builder()
                .uuid(SESSION_ID)
                .user(user)
                .expiresAt(Instant.now().minusSeconds(1))
                .build();

        when(sessionRepository.find(SESSION_ID)).thenReturn(Optional.of(expiredSession));

        AuthResult result = authService.authenticate(SESSION_ID.toString());

        assertThat(result.isAuthenticated()).isFalse();
    }
}
