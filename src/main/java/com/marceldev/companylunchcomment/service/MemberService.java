package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.component.EmailSender;
import com.marceldev.companylunchcomment.component.TokenProvider;
import com.marceldev.companylunchcomment.component.VerificationCodeGenerator;
import com.marceldev.companylunchcomment.dto.member.SendVerificationCodeDto;
import com.marceldev.companylunchcomment.dto.member.SignInDto;
import com.marceldev.companylunchcomment.dto.member.SignUpDto;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.SignupVerification;
import com.marceldev.companylunchcomment.exception.AlreadyExistMemberException;
import com.marceldev.companylunchcomment.exception.EmailIsNotCompanyDomain;
import com.marceldev.companylunchcomment.exception.IncorrectPasswordException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.repository.SignupVerificationRepository;
import com.marceldev.companylunchcomment.type.Role;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

  private final MemberRepository memberRepository;

  private final PasswordEncoder passwordEncoder;

  private final TokenProvider tokenProvider;

  private final EmailSender emailSender;

  private final VerificationCodeGenerator verificationCodeGenerator;

  private final SignupVerificationRepository signupVerificationRepository;

  private final HashSet<String> NOT_SUPPORTED_DOMAINS = new HashSet<>(List.of(
      "naver.com", "gmail.com", "daum.net", "kakao.com", "hanmail.net", "yahoo.com",
      "nate.com", "hotmail.com", "outlook.com", "live.com", "msn.com",
      "icloud.com", "me.com", "aol.com", "protonmail.com"
  ));

  /**
   * 회원가입
   */
  public void signUp(SignUpDto dto) {
    String encPassword = passwordEncoder.encode(dto.getPassword());

    Member member = Member.builder()
        .email(dto.getEmail())
        .password(encPassword)
        .name(dto.getName())
        .authYn(true) // TODO: 이메일 인증을 마친 후 true로 바꿔주도록 수정
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

  /**
   * 이메일 전송
   */
  public void sendVerificationCode(SendVerificationCodeDto dto) {
    String email = dto.getEmail();
    String domain = email.split("@")[1];

    if (NOT_SUPPORTED_DOMAINS.contains(domain)) {
      throw new EmailIsNotCompanyDomain(domain);
    }

    if (memberRepository.existsByEmail(email)) {
      throw new AlreadyExistMemberException();
    }

    // 이메일 전송
    String code = verificationCodeGenerator.generate(6);
    String subject = "[Company Lunch Comment] 회원가입을 위한 인증번호입니다";
    String body = String.format("인증번호는 %s 입니다. 회원가입란에 입력해주세요.", code);
    emailSender.sendMail(email, subject, body);

    // 기존에 있다면 제거
    Optional<SignupVerification> verification = signupVerificationRepository.findByEmail(
        email);
    verification.ifPresent(signupVerificationRepository::delete);

    // 새로운 인증번호 저장
    SignupVerification signupVerification = SignupVerification.builder()
        .code(code)
        .expiration(LocalDateTime.now().plusMinutes(3))
        .email(email)
        .build();

    signupVerificationRepository.save(signupVerification);
  }
}
