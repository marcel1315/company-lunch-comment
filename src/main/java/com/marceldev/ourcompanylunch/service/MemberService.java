package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.dto.member.SignUpDto;
import com.marceldev.ourcompanylunch.dto.member.UpdateMemberDto;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.member.AlreadyExistMemberException;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.type.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private final MemberRepository memberRepository;

  /**
   * Update member information
   */
  @Transactional
  public void updateMember(long id, UpdateMemberDto dto) {
    Member member = getMemberOf(id);
    member.setName(dto.getName());
  }

  /**
   * Register member
   */
  @Transactional
  public void signUp(SignUpDto dto) {
    String email = getEmail();
    if (memberRepository.existsByEmail(email)) {
      throw new AlreadyExistMemberException();
    }

    Member member = Member.builder()
        .email(email)
        .name(dto.getName())
        .role(getRole())
        .build();

    memberRepository.save(member);
  }

  /**
   * Get member from db id and see if email from authentication is correct.
   */
  private Member getMemberOf(long id) {
    String email = (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    return memberRepository.findByIdAndEmail(id, email)
        .orElseThrow(MemberUnauthorizedException::new);
  }

  /**
   * Find out role from authentication
   */
  private Role getRole() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getAuthorities().stream().findFirst()
        .map(a -> Role.valueOf(a.getAuthority()))
        .orElseThrow(RuntimeException::new);
  }

  private String getEmail() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return auth.getName();
  }
}
