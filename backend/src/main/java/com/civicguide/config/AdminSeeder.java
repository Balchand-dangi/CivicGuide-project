package com.civicguide.config;

import com.civicguide.entity.Role;
import com.civicguide.entity.User;
import com.civicguide.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {
    @Bean
    CommandLineRunner seedAdmin(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${civicguide.admin.email:}") String email,
            @Value("${civicguide.admin.password:}") String password) {
        return args -> {
            if (email == null || email.isBlank() || password == null || password.isBlank()) return;
            if (userRepository.existsByEmail(email)) return;
            User admin = new User();
            admin.setName("CivicGuide Administrator");
            admin.setEmail(email.trim());
            admin.setPassword(passwordEncoder.encode(password));
            admin.setPhone("0000000000");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        };
    }
}
