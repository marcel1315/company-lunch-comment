package com.marceldev.companylunchcomment.repository;

import com.marceldev.companylunchcomment.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  boolean existsByEmail(String email);

  Optional<Member> findByEmail(String email);

  Optional<Member> findByEmailAndCompanyId(String email, long companyId);
}
