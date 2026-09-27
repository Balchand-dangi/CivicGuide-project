package com.civicguide.controller;

import com.civicguide.dto.UserResponse;
import com.civicguide.entity.User;
import com.civicguide.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // The role returned to the frontend comes from the authenticated
        // SecurityContext, which was built from the signed JWT claim.
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Authenticated user has no role"));

        UserResponse response = new UserResponse(
                user.getName(),
                user.getEmail(),
                com.civicguide.entity.Role.valueOf(role));

        return ResponseEntity.ok(response);
    }
}
