package com.example.hits.application.filter;

import com.example.hits.application.service.LoggedOutTokenService;
import com.example.hits.application.util.JwtUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtFilter implements Filter {
    private final JwtUtil jwtUtil;
    private final LoggedOutTokenService loggedOutTokenService;

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (isPublicEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        var token = authHeader.substring(7);
        try {
            if (loggedOutTokenService.isTokenLoggedOut(token)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            var claims = jwtUtil.parseAccessClaims(token);
            request.setAttribute("userId", claims.get("user_id", String.class));
            request.setAttribute("role", claims.get("role", String.class));
            request.setAttribute("accessToken", token);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private Boolean isPublicEndpoint(String uri) {
        return uri.equals("/api/v1/user/login") || uri.equals("/api/v1/user/register")
                || uri.equals("/api/v1/user/refresh-tokens");
    }
}
