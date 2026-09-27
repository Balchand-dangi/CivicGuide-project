package com.civicguide.dto;

import com.civicguide.entity.MentorProfile;
import com.civicguide.entity.VerificationStatus;

public record MentorResponse(Long id, String name, String email, String qualification,
                             String specialization, Integer experienceYears,
                             String languages, String bio, VerificationStatus verificationStatus) {
    public static MentorResponse from(MentorProfile p) {
        return new MentorResponse(p.getId(), p.getUser().getName(), p.getUser().getEmail(),
                p.getQualification(), p.getSpecialization(), p.getExperienceYears(),
                p.getLanguages(), p.getBio(), p.getVerificationStatus());
    }
}
