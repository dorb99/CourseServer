package com.courseServer.filters;

import com.courseServer.services.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthFilter implements Filter {
    private final AuthService authService;

    public AuthFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Skip authentication for login and public endpoints
        String path = httpRequest.getRequestURI();
        if (path.equals("/api/v1/auth/login") || path.startsWith("/public/")) {
            chain.doFilter(request, response);
            return;
        }

        // Get token from Authorization header
        String token = httpRequest.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || !authService.validateToken(token)) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.getWriter().write("Unauthorized: Invalid or missing token");
            return;
        }

        chain.doFilter(request, response);
    }
} 