package com.civicguide.repository;

import com.civicguide.entity.Role;
import com.civicguide.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Efficient role count — used by AdminController stats. */
    long countByRole(Role role);
}