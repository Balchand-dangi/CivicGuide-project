package com.civicguide.repository;

import com.civicguide.entity.MentorProfile;
import com.civicguide.entity.User;
import com.civicguide.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MentorProfileRepository extends JpaRepository<MentorProfile, Long> {

    Optional<MentorProfile> findByUser(User user);

    /** Efficient count by verification status — used by AdminController stats. */
    long countByVerificationStatus(VerificationStatus status);

    /**
     * Return all profiles matching a verification status — used by AdminController.
     */
    java.util.List<MentorProfile> findByVerificationStatus(VerificationStatus status);
}