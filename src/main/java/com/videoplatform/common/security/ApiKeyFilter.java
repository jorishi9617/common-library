package com.videoplatform.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {
    private final byte[] configuredApiKey;

    public ApiKeyFilter(@Value("${security.api-key}") String apiKey) {
        byte[] apiKeyBytes = apiKey.getBytes(StandardCharsets.UTF_8);
        if (apiKey.isBlank() || apiKeyBytes.length < 32) {
            throw new IllegalArgumentException("security.api-key must be at least 32 bytes");
        }
        this.configuredApiKey = apiKeyBytes;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals("/actuator/health") || path.startsWith("/actuator/health/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String suppliedApiKey = request.getHeader("X-API-Key");
        if (suppliedApiKey == null || !MessageDigest.isEqual(
                configuredApiKey, suppliedApiKey.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing API key");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
