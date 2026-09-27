package com.civicguide.dto;

import com.civicguide.entity.RequestStatus;
import com.civicguide.entity.ServiceRequest;
import java.time.LocalDateTime;

public record ServiceRequestResponse(
        Long id,
        String category,
        String serviceName,
        String description,
        RequestStatus status,
        String userName,
        String userEmail,
        String mentorName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ServiceRequestResponse from(ServiceRequest r) {
        return new ServiceRequestResponse(
                r.getId(), r.getCategory(), r.getServiceName(), r.getDescription(),
                r.getStatus(), r.getUser().getName(), r.getUser().getEmail(),
                r.getMentor() == null ? null : r.getMentor().getName(),
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
