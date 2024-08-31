package com.marceldev.ourcompanylunch.repository.member;

import com.marceldev.ourcompanylunch.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

  boolean existsByEmail(String email);

  Optional<Member> findByEmail(String email);

  Optional<Member> findByIdAndEmail(long id, String email);

  Optional<Member> findByEmailAndCompanyId(String email, long companyId);
}
