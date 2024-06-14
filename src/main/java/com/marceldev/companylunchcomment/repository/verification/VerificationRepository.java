package com.marceldev.companylunchcomment.repository.verification;

import com.marceldev.companylunchcomment.entity.Verification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface VerificationRepository extends JpaRepository<Verification, Long> {

  Optional<Verification> findByEmail(String email);

  @Modifying(clearAutomatically = true)
  @Query("delete from Verification v where v.expirationAt < :localDateTime")
  int deleteAllExpiredVerificationCode(LocalDateTime localDateTime);
}
