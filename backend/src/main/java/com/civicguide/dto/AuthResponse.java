package com.civicguide.dto;

public class AuthResponse {

    private String role;
    private String name;

    public AuthResponse(String role, String name) {
        this.role = role;
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }
}
