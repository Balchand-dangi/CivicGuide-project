package com.civicguide.controller;

import com.civicguide.dto.MentorResponse;
import com.civicguide.entity.VerificationStatus;
import com.civicguide.repository.MentorProfileRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/mentor")
@PreAuthorize("hasRole('MENTOR')")
public class MentorController {

    private final MentorProfileRepository repository;

    public MentorController(MentorProfileRepository repository) {
        this.repository = repository;
    }

    /** GET /api/mentor/profile/{id} — fetch a mentor profile by its primary key. */
    @GetMapping("/profile/{id}")
    public MentorResponse profile(@PathVariable Long id) {
        return repository.findById(id)
                .map(MentorResponse::from)
                .orElseThrow(() -> new NoSuchElementException("Mentor profile not found: " + id));
    }

    @GetMapping("/approved")
    public List<MentorResponse> approved() {
        return repository.findByVerificationStatus(VerificationStatus.APPROVED)
                .stream()
                .map(MentorResponse::from)
                .toList();
    }
}
