package com.videoplatform.common.api;

import com.videoplatform.common.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {
    private final JwtService jwtService;
    private final long expirationSeconds;

    public TokenController(
            JwtService jwtService,
            @Value("${security.jwt.expiration-seconds:900}") long expirationSeconds) {
        this.jwtService = jwtService;
        this.expirationSeconds = expirationSeconds;
    }

    @PostMapping
    public TokenResponse issue(@Valid @RequestBody IssueTokenRequest request) {
        return new TokenResponse(
                jwtService.issue(request.userId(), request.email()),
                expirationSeconds);
    }

    @PostMapping("/validate")
    public TokenValidationResponse validate(@Valid @RequestBody ValidateTokenRequest request) {
        if (!jwtService.isValid(request.token())) {
            return new TokenValidationResponse(false, null, null);
        }
        return new TokenValidationResponse(
                true,
                jwtService.subject(request.token()),
                jwtService.email(request.token()));
    }

    public record IssueTokenRequest(
            @NotNull UUID userId,
            @NotBlank @Email String email) {
    }

    public record TokenResponse(String token, long expiresInSeconds) {
    }

    public record ValidateTokenRequest(@NotBlank String token) {
    }

    public record TokenValidationResponse(boolean valid, UUID userId, String email) {
    }
}
