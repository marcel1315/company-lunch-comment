package com.marceldev.companylunchcomment.repository;

import com.marceldev.companylunchcomment.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
