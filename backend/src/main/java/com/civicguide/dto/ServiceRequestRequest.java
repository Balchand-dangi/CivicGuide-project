package com.civicguide.dto;

import jakarta.validation.constraints.NotBlank;

public class ServiceRequestRequest {
    @NotBlank private String category;
    @NotBlank private String serviceName;
    private String description;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
