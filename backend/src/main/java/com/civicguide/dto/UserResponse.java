package com.civicguide.dto;

import com.civicguide.entity.Role;

public class UserResponse {

    private String name;
    private String email;
    private Role role;

    public UserResponse(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }
}