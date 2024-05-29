package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.component.TokenProvider;
import com.marceldev.companylunchcomment.dto.member.SignInDto;
import com.marceldev.companylunchcomment.dto.member.SignUpDto;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.IncorrectPasswordException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.type.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  private final PasswordEncoder passwordEncoder;

  private final TokenProvider tokenProvider;

  /**
   * 회원가입
   */
  public void signUp(SignUpDto dto) {
    String encPassword = passwordEncoder.encode(dto.getPassword());

    Member member = Member.builder()
        .email(dto.getEmail())
        .password(encPassword)
        .name(dto.getName())
        .authYn(true)
        .role(Role.USER)
        .build();

    memberRepository.save(member);
  }

  /**
   * 로그인
   */
  public String signIn(SignInDto dto) {
    Member member = memberRepository.findByEmail(dto.getEmail())
        .orElseThrow(MemberNotExistException::new);

    if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
      throw new IncorrectPasswordException();
    }

    return tokenProvider.generateToken(member.getEmail(), Role.USER.toString());
  }
}
