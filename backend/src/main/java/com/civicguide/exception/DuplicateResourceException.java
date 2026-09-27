package com.civicguide.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException() {
        super("Resource already exists");
    }

    public DuplicateResourceException(String message) {
        super(message);
    }
}
