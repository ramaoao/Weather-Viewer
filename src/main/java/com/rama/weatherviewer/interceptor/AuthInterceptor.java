package com.rama.weatherviewer.interceptor;

import com.rama.weatherviewer.dto.AuthResult;
import com.rama.weatherviewer.service.AuthService;
import com.rama.weatherviewer.util.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String sessionId = CookieUtils.findSessionCookie(request)
                .map(Cookie::getValue)
                .orElse(null);

        AuthResult authResult = authService.authenticate(sessionId);

        request.setAttribute("auth", authResult);

        return true;
    }
}
