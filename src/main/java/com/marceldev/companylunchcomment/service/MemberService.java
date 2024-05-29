package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.member.SignUpDto;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  /**
   * 회원가입
   */
  public void signUp(SignUpDto dto) {
    Member member = Member.builder()
        .email(dto.getEmail())
        .password(dto.getPassword())
        .name(dto.getName())
        .authYn(true)
        .role(Role.USER)
        .build();

    memberRepository.save(member);
  }
}
