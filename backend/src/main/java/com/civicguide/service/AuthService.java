package com.civicguide.service;

import com.civicguide.dto.AuthResponse;
import com.civicguide.dto.LoginRequest;
import com.civicguide.dto.RegisterRequest;
import com.civicguide.entity.MentorProfile;
import com.civicguide.entity.Role;
import com.civicguide.entity.User;
import com.civicguide.exception.DuplicateResourceException;
import com.civicguide.exception.InvalidCredentialsException;
import com.civicguide.repository.MentorProfileRepository;
import com.civicguide.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

        private final UserRepository userRepository;
        private final MentorProfileRepository mentorProfileRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;

        public AuthService(
                        UserRepository userRepository,
                        MentorProfileRepository mentorProfileRepository,
                        PasswordEncoder passwordEncoder,
                        JwtService jwtService) {

                this.userRepository = userRepository;
                this.mentorProfileRepository = mentorProfileRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtService = jwtService;
        }

        @Transactional
        public void register(RegisterRequest request) {

                // 1. ADMIN registration is never allowed via public endpoint
                if (request.getRole() == Role.ADMIN) {
                        throw new IllegalArgumentException("Admin registration is not allowed.");
                }

                // 2. Email uniqueness check
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new DuplicateResourceException("Email already registered.");
                }

                // 3. Password confirmation check
                if (!request.getPassword().equals(request.getConfirmPassword())) {
                        throw new IllegalArgumentException("Passwords do not match.");
                }

                // 4. Create User
                User user = new User();
                user.setName(request.getFullName());
                user.setEmail(request.getEmail());
                user.setPassword(passwordEncoder.encode(request.getPassword()));
                user.setPhone(request.getPhone());
                user.setRole(request.getRole());

                User savedUser = userRepository.save(user);

                // 5. If Mentor → create MentorProfile (starts PENDING)
                if (request.getRole() == Role.MENTOR) {
                        MentorProfile mentor = new MentorProfile();
                        mentor.setUser(savedUser);
                        mentor.setQualification(request.getQualification());
                        mentor.setSpecialization(request.getSpecialization());
                        mentor.setExperienceYears(request.getExperienceYears());
                        mentor.setLanguages(request.getLanguages());
                        mentor.setBio(request.getBio());
                        mentorProfileRepository.save(mentor);
                }
        }

        @Transactional
        public LoginResult login(LoginRequest request) {

                User user = userRepository
                                .findByEmail(request.getEmail())
                                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        throw new InvalidCredentialsException("Invalid email or password.");
                }

                String accessToken = jwtService.generateAccessToken(user);

                AuthResponse authResponse = new AuthResponse(
                                user.getRole().name(),
                                user.getName());

                return new LoginResult(accessToken, authResponse);
        }

        /** Simple container to return both token + response body. */
        public record LoginResult(String accessToken, AuthResponse authResponse) {
        }
}
