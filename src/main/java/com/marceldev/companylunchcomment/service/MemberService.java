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
import com.marceldev.companylunchcomment.exception.NotMatchVerificationCode;
import com.marceldev.companylunchcomment.exception.VerificationCodeNotFound;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.repository.SignupVerificationRepository;
import com.marceldev.companylunchcomment.type.Role;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

  private static final int VERIFICATION_CODE_VALID_SECOND = 60 * 3;

  private static final int VERIFICATION_CODE_LENGTH = 6;

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

  @Value("${bypass-email-domain-check-for-dev:false}")
  private boolean bypassEmailDomainCheck;

  /**
   * 회원가입
   */
  @Transactional
  public void signUp(SignUpDto dto) {
    SignupVerification verification = signupVerificationRepository.findByEmail(dto.getEmail())
        .orElseThrow(VerificationCodeNotFound::new);

    matchVerificationCode(dto, verification);
    saveMember(dto);
    signupVerificationRepository.delete(verification);
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
  @Transactional
  public void sendVerificationCode(SendVerificationCodeDto dto) {
    String email = dto.getEmail();

    checkCompanyDomainNotEmailProvider(email);
    checkAlreadyExistsMember(email);

    String code = verificationCodeGenerator.generate(VERIFICATION_CODE_LENGTH);
    sendVerificationCodeEmail(email, code);
    saveVerificationCodeToDb(email, code);
  }

  private void checkCompanyDomainNotEmailProvider(String email) {
    // only for dev
    if (bypassEmailDomainCheck) {
      return;
    }

    String domain = email.split("@")[1];

    if (NOT_SUPPORTED_DOMAINS.contains(domain)) {
      throw new EmailIsNotCompanyDomain(domain);
    }
  }

  private void checkAlreadyExistsMember(String email) {
    if (memberRepository.existsByEmail(email)) {
      throw new AlreadyExistMemberException();
    }
  }

  private void sendVerificationCodeEmail(String email, String code) {
    String subject = "[Company Lunch Comment] 회원가입을 위한 인증번호입니다";
    String body = String.format("인증번호는 %s 입니다. 회원가입란에 입력해주세요.", code);
    emailSender.sendMail(email, subject, body);
  }

  private void saveVerificationCodeToDb(String email, String code) {
    // 기존에 있다면 제거
    signupVerificationRepository.findByEmail(email).ifPresent(signupVerificationRepository::delete);

    // 새로운 인증번호 저장
    SignupVerification signupVerification = SignupVerification.builder()
        .code(code)
        .expirationAt(LocalDateTime.now().plusSeconds(VERIFICATION_CODE_VALID_SECOND))
        .email(email)
        .build();

    signupVerificationRepository.save(signupVerification);
  }

  private void matchVerificationCode(SignUpDto dto, SignupVerification verification) {
    if (dto.getNow().isAfter(verification.getExpirationAt())) {
      throw new NotMatchVerificationCode();
    }

    if (!verification.getCode().equals(dto.getVerificationCode())) {
      throw new NotMatchVerificationCode();
    }
  }

  private void saveMember(SignUpDto dto) {
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
}
