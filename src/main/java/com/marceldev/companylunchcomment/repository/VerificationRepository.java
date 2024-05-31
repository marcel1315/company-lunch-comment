package com.marceldev.companylunchcomment.repository;

import com.marceldev.companylunchcomment.entity.Verification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepository extends JpaRepository<Verification, Long> {

  Optional<Verification> findByEmail(String email);
}
