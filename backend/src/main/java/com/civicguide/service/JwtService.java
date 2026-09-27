package com.civicguide.service;

import com.civicguide.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

        private final SecretKey secretKey;
        private final long accessTokenExpiration;

        public JwtService(
                        @Value("${jwt.secret}") String secret,
                        @Value("${jwt.access-token-expiration}") long accessTokenExpiration) {

                this.secretKey = Keys.hmacShaKeyFor(
                                secret.getBytes(StandardCharsets.UTF_8));
                this.accessTokenExpiration = accessTokenExpiration;
        }

        public String generateAccessToken(User user) {

                Date now = new Date();
                Date expiry = new Date(now.getTime() + accessTokenExpiration);

                return Jwts.builder()
                                .subject(user.getEmail())
                                .claim("role", user.getRole().name())
                                .claim("userId", user.getId())
                                .issuedAt(now)
                                .expiration(expiry)
                                .signWith(secretKey)
                                .compact();
        }

        public boolean isTokenValid(String token) {
                try {
                        getClaims(token);
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        public String extractEmail(String token) {
                return getClaims(token).getSubject();
        }

        public String extractRole(String token) {
                return getClaims(token).get("role", String.class);
        }

        private Claims getClaims(String token) {
                return Jwts.parser()
                                .verifyWith(secretKey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();
        }
}
