package com.civicguide.controller;

import com.civicguide.dto.ServiceRequestRequest;
import com.civicguide.dto.ServiceRequestResponse;
import com.civicguide.entity.RequestStatus;
import com.civicguide.entity.ServiceRequest;
import com.civicguide.entity.User;
import com.civicguide.entity.VerificationStatus;
import com.civicguide.repository.MentorProfileRepository;
import com.civicguide.repository.ServiceRequestRepository;
import com.civicguide.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/requests")
public class ServiceRequestController {

    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final MentorProfileRepository mentorProfileRepository;

    public ServiceRequestController(ServiceRequestRepository requestRepository,
            UserRepository userRepository,
            MentorProfileRepository mentorProfileRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
        this.mentorProfileRepository = mentorProfileRepository;
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping
    public ResponseEntity<ServiceRequestResponse> create(@Valid @RequestBody ServiceRequestRequest request,
            Authentication authentication) {
        User user = currentUser(authentication);
        ServiceRequest entity = new ServiceRequest();
        entity.setUser(user);
        entity.setCategory(request.getCategory());
        entity.setServiceName(request.getServiceName());
        entity.setDescription(request.getDescription());
        return ResponseEntity.ok(ServiceRequestResponse.from(requestRepository.save(entity)));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/mine")
    public List<ServiceRequestResponse> mine(Authentication authentication) {
        return requestRepository.findByUserOrderByCreatedAtDesc(currentUser(authentication))
                .stream().map(ServiceRequestResponse::from).toList();
    }

    @PreAuthorize("hasRole('MENTOR')")
    @GetMapping("/mentor")
    public List<ServiceRequestResponse> mentorRequests(Authentication authentication) {
        return requestRepository.findByMentorOrderByCreatedAtDesc(currentUser(authentication))
                .stream().map(ServiceRequestResponse::from).toList();
    }

    @PreAuthorize("hasRole('MENTOR')")
    @GetMapping("/available")
    public List<ServiceRequestResponse> available(Authentication authentication) {
        User mentor = currentUser(authentication);
        var profile = mentorProfileRepository.findByUser(mentor)
                .orElseThrow(() -> new IllegalStateException("Mentor profile not found for authenticated user."));
        if (profile.getVerificationStatus() != VerificationStatus.APPROVED) {
            return List.of();
        }
        return requestRepository.findByStatusOrderByCreatedAtAsc(RequestStatus.PENDING)
                .stream().map(ServiceRequestResponse::from).toList();
    }

    @PreAuthorize("hasRole('MENTOR')")
    @PostMapping("/{id}/accept")
    public ResponseEntity<ServiceRequestResponse> accept(@PathVariable Long id, Authentication authentication) {
        ServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Service request not found: " + id));
        if (request.getStatus() != RequestStatus.PENDING) {
            return ResponseEntity.badRequest().build();
        }
        User mentor = currentUser(authentication);
        var profile = mentorProfileRepository.findByUser(mentor)
                .orElseThrow(() -> new IllegalStateException("Mentor profile not found for authenticated user."));
        if (profile.getVerificationStatus() != VerificationStatus.APPROVED) {
            return ResponseEntity.status(403).build();
        }
        request.setMentor(mentor);
        request.setStatus(RequestStatus.ACCEPTED);
        return ResponseEntity.ok(ServiceRequestResponse.from(requestRepository.save(request)));
    }

    /**
     * Status-transition rules:
     * - MENTOR: ACCEPTED → IN_PROGRESS, IN_PROGRESS → COMPLETED
     * - USER: PENDING → CANCELLED (own requests only)
     */
    @PreAuthorize("hasAnyRole('USER', 'MENTOR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceRequestResponse> updateStatus(@PathVariable Long id,
            @RequestParam RequestStatus status,
            Authentication authentication) {
        ServiceRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Service request not found: " + id));
        User actor = currentUser(authentication);

        boolean isOwner = request.getUser().getId().equals(actor.getId());
        boolean isAssignedMentor = request.getMentor() != null
                && request.getMentor().getId().equals(actor.getId());

        if (!isOwner && !isAssignedMentor) {
            return ResponseEntity.status(403).build();
        }

        // State-machine guard
        RequestStatus current = request.getStatus();
        boolean allowed = false;

        if (isAssignedMentor) {
            // Mentor can advance: ACCEPTED → IN_PROGRESS → COMPLETED
            allowed = (current == RequestStatus.ACCEPTED && status == RequestStatus.IN_PROGRESS)
                    || (current == RequestStatus.IN_PROGRESS && status == RequestStatus.COMPLETED);
        }
        if (isOwner) {
            // Citizen can mark COMPLETED once mentor has started, or CANCEL while PENDING
            allowed = allowed
                    || ((current == RequestStatus.ACCEPTED || current == RequestStatus.IN_PROGRESS)
                            && status == RequestStatus.COMPLETED)
                    || (current == RequestStatus.PENDING && status == RequestStatus.CANCELLED);
        }

        if (!allowed) {
            return ResponseEntity.badRequest().build();
        }

        request.setStatus(status);
        return ResponseEntity.ok(ServiceRequestResponse.from(requestRepository.save(request)));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database."));
    }
}
