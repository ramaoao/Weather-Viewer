package com.rama.weatherviewer.controller;

import com.rama.weatherviewer.dto.UserLoginDto;
import com.rama.weatherviewer.dto.UserRegistrationDto;
import com.rama.weatherviewer.exception.AuthException;
import com.rama.weatherviewer.exception.UserAlreadyExistsException;
import com.rama.weatherviewer.service.AuthService;
import com.rama.weatherviewer.validator.UserValidator;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.util.Optional;
import java.util.UUID;

import static com.rama.weatherviewer.util.CookieUtils.COOKIE_NAME;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private AuthController authController;

    private final String TEST_USERNAME = "testUsername";
    private final String TEST_PASSWORD = "testPassword";
    private final UUID EXPECTED_SESSION_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("WEB-INF/views/");
        viewResolver.setSuffix(".html");

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setViewResolvers(viewResolver)
                .build();
    }

    @Test
    @DisplayName("GET signUp: должен успешно вернуть пользователю страницу sign-up")
    void shouldReturnSignUpPage() throws Exception {
        mockMvc.perform(get("/auth/sign-up"))
                .andExpect(status().isOk())
                .andExpect(view().name("sign-up"))
                .andExpect(model().attributeExists("userRegistrationDto"))
                .andExpect(model().attribute("userRegistrationDto", any(UserRegistrationDto.class)));
    }

    @Test
    @DisplayName("POST signUp: должен успешно зарегистрировать пользователя, установить куку и сделать редирект")
    void shouldRegisterUserAndRedirect() throws Exception {
        when(authService.register(TEST_USERNAME, TEST_PASSWORD)).thenReturn(EXPECTED_SESSION_ID);

        mockMvc.perform(post("/auth/sign-up")
                .param("username", TEST_USERNAME)
                .param("password", TEST_PASSWORD)
                .param("confirmPassword",TEST_PASSWORD))

                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/weather"))
                .andExpect(cookie().value(COOKIE_NAME, EXPECTED_SESSION_ID.toString()));

        verify(authService).register(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    @DisplayName("POST signUp: должен вернуть страницу sign-up с ошибкой, если логин занят")
    void shouldReturnSignUpPageWhenLoginUsernameIsToken() throws Exception {
        when(authService.register(TEST_USERNAME, TEST_PASSWORD))
                .thenThrow(new UserAlreadyExistsException("User with this username already exists."));

        mockMvc.perform(post("/auth/sign-up")
                .param("username", TEST_USERNAME)
                .param("password", TEST_PASSWORD)
                .param("confirmPassword", TEST_PASSWORD))

                .andExpect(status().isOk())
                .andExpect(view().name("sign-up"))
                .andExpect(model().attributeHasFieldErrorCode("userRegistrationDto", "username", "duplicate"))
                .andExpect(model().attribute("userRegistrationDto", hasProperty("password", is(""))));

        verify(authService).register(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    @DisplayName("GET signIn: должен успешно вернуть пользователю страницу sign-in")
    void shouldReturnSignInPage() throws Exception {
        mockMvc.perform(get("/auth/sign-in"))
                .andExpect(status().isOk())
                .andExpect(view().name("sign-in"))
                .andExpect(model().attributeExists("userLoginDto"))
                .andExpect(model().attribute("userLoginDto", any(UserLoginDto.class)));
    }

    @Test
    @DisplayName("POST signIn: должен успешно залогинить пользователя, установить куку и сделать редирект")
    void shouldLoginUserAndRedirect() throws Exception {
        when(authService.login(TEST_USERNAME, TEST_PASSWORD)).thenReturn(EXPECTED_SESSION_ID);

        mockMvc.perform(post("/auth/sign-in")
                .param("username", TEST_USERNAME)
                .param("password", TEST_PASSWORD))

                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/weather"))
                .andExpect(cookie().value(COOKIE_NAME, EXPECTED_SESSION_ID.toString()));

        verify(authService).login(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    @DisplayName("POST signIn: должен вернуть страницу sign-in с ошибкой, если пользователя не существует")
    void shouldReturnSignInWhenLoginUserNotExists() throws Exception {
        when(authService.login(TEST_USERNAME, TEST_PASSWORD))
                .thenThrow(new AuthException("Invalid username or password."));

        mockMvc.perform(post("/auth/sign-in")
                .param("username", TEST_USERNAME)
                .param("password", TEST_PASSWORD))

                .andExpect(status().isOk())
                .andExpect(view().name("sign-in"))
                .andExpect(model().attributeHasErrors("userLoginDto"))
                .andExpect(model().attribute("userLoginDto", hasProperty("password", is(""))));

        verify(authService).login(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    @DisplayName("POST logout: должен успешно удалить сессию и сделать редирект")
    void shouldLogoutUserDeleteSessionAndRedirect() throws Exception {
        Cookie sessionCookie = new Cookie(COOKIE_NAME, EXPECTED_SESSION_ID.toString());

        when(authService.logout(EXPECTED_SESSION_ID.toString())).thenReturn(Optional.of(EXPECTED_SESSION_ID));

        mockMvc.perform(post("/auth/logout")
                .cookie(sessionCookie))

                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/weather"))
                .andExpect(cookie().maxAge(COOKIE_NAME, 0));

        verify(authService).logout(EXPECTED_SESSION_ID.toString());
    }
}
