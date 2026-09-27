package com.civicguide.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Central exception → HTTP status mapping.
 *
 * Handler precedence (most specific first):
 * DuplicateResourceException → 409 Conflict
 * InvalidCredentialsException → 401 Unauthorized
 * MethodArgumentNotValidException → 400 Bad Request (bean-validation)
 * IllegalArgumentException → 400 Bad Request (password mismatch, admin reg)
 * NoSuchElementException → 404 Not Found
 * IllegalStateException → 500 Internal Server Error (data-integrity issues)
 * Exception (catch-all) → 500 Internal Server Error
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<Map<String, String>> handleDuplicateResource(DuplicateResourceException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(Map.of("message", ex.getMessage()));
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentialsException ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(Map.of("message", ex.getMessage()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
                String errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                                .collect(Collectors.joining(", "));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("message", errors));
        }

        /**
         * Business-rule violations: password mismatch, admin registration attempt, etc.
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(Map.of("message", ex.getMessage()));
        }

        /** Resource not found (repository .orElseThrow()). */
        @ExceptionHandler(NoSuchElementException.class)
        public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("message", ex.getMessage()));
        }

        /** Data integrity / internal state issues. */
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("message", "Internal error: " + ex.getMessage()));
        }

        /** Safety net — never expose raw stack traces. */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("message", "An unexpected error occurred. Please try again later."));
        }
}
