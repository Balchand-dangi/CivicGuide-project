package com.civicguide.controller;

import com.civicguide.dto.AdminStatsResponse;
import com.civicguide.dto.MentorResponse;
import com.civicguide.entity.Role;
import com.civicguide.entity.RequestStatus;
import com.civicguide.entity.VerificationStatus;
import com.civicguide.repository.MentorProfileRepository;
import com.civicguide.repository.ServiceRequestRepository;
import com.civicguide.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository users;
    private final MentorProfileRepository mentors;
    private final ServiceRequestRepository requests;

    public AdminController(UserRepository users, MentorProfileRepository mentors, ServiceRequestRepository requests) {
        this.users = users;
        this.mentors = mentors;
        this.requests = requests;
    }

    @GetMapping("/stats")
    public AdminStatsResponse stats() {
        long userCount = users.countByRole(Role.USER);
        long mentorCount = users.countByRole(Role.MENTOR);
        long pendingMentors = mentors.countByVerificationStatus(VerificationStatus.PENDING);
        long totalRequests = requests.count();
        long pendingRequests = requests.countByStatus(RequestStatus.PENDING);
        return new AdminStatsResponse(userCount, mentorCount, pendingMentors, totalRequests, pendingRequests);
    }

    @GetMapping("/mentors/pending")
    public List<MentorResponse> pendingMentors() {
        return mentors.findByVerificationStatus(VerificationStatus.PENDING)
                .stream()
                .map(MentorResponse::from)
                .toList();
    }

    @PatchMapping("/mentors/{id}/verification")
    public MentorResponse verifyMentor(@PathVariable Long id, @RequestParam VerificationStatus status) {
        var mentor = mentors.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Mentor profile not found: " + id));
        mentor.setVerificationStatus(status);
        return MentorResponse.from(mentors.save(mentor));
    }
}
