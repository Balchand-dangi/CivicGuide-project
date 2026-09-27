package com.civicguide.controller;

import com.civicguide.dto.AuthResponse;
import com.civicguide.dto.LoginRequest;
import com.civicguide.dto.RegisterRequest;
import com.civicguide.service.AuthService;
import com.civicguide.service.AuthService.LoginResult;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

/**
 * Public authentication endpoints.
 * CORS is configured globally in SecurityConfig — no @CrossOrigin needed here.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResult result = authService.login(request);

        // Use ResponseCookie to get SameSite support (standard Servlet API Cookie lacks
        // it)
        ResponseCookie cookie = ResponseCookie.from("access_token", result.accessToken())
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofSeconds(900)) // 15 minutes — matches jwt.access-token-expiration
                .sameSite("Lax")
                // .secure(true) // Uncomment when deployed on HTTPS
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(result.authResponse());
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("access_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok("Logged out successfully");
    }
}
