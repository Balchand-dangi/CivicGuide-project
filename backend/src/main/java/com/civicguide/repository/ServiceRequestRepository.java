package com.civicguide.repository;

import com.civicguide.entity.RequestStatus;
import com.civicguide.entity.ServiceRequest;
import com.civicguide.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long> {

    List<ServiceRequest> findByUserOrderByCreatedAtDesc(User user);

    List<ServiceRequest> findByMentorOrderByCreatedAtDesc(User mentor);

    List<ServiceRequest> findByStatusOrderByCreatedAtAsc(RequestStatus status);

    /** Efficient count by status — used by AdminController stats. */
    long countByStatus(RequestStatus status);
}
