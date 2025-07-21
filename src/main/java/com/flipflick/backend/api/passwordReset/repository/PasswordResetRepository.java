package com.flipflick.backend.api.passwordReset.repository;

import com.flipflick.backend.api.passwordReset.entity.PasswordReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset,Long> {
    Optional<PasswordReset> findByCode(String code);
}
