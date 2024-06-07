package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.component.EmailSender;
import com.marceldev.companylunchcomment.dto.member.SecurityMember;
import com.marceldev.companylunchcomment.dto.member.SendVerificationCodeDto;
import com.marceldev.companylunchcomment.dto.member.SignInDto;
import com.marceldev.companylunchcomment.dto.member.SignInResult;
import com.marceldev.companylunchcomment.dto.member.SignUpDto;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.Verification;
import com.marceldev.companylunchcomment.exception.AlreadyExistMemberException;
import com.marceldev.companylunchcomment.exception.EmailIsNotCompanyDomain;
import com.marceldev.companylunchcomment.exception.IncorrectPasswordException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.exception.VerificationCodeNotFound;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import com.marceldev.companylunchcomment.repository.verification.VerificationRepository;
import com.marceldev.companylunchcomment.type.Role;
import com.marceldev.companylunchcomment.util.ExtractDomain;
import com.marceldev.companylunchcomment.util.VerificationCodeGenerator;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements UserDetailsService {

  private static final int VERIFICATION_CODE_VALID_SECOND = 60 * 3;

  private static final int VERIFICATION_CODE_LENGTH = 6;

  private final MemberRepository memberRepository;

  private final PasswordEncoder passwordEncoder;

  private final EmailSender emailSender;

  private final VerificationRepository verificationRepository;

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
    Verification verification = verificationRepository.findByEmail(dto.getEmail())
        .orElseThrow(VerificationCodeNotFound::new);

    matchVerificationCode(dto, verification);
    saveMember(dto);
    verificationRepository.delete(verification);
  }

  /**
   * 로그인
   */
  public SignInResult signIn(SignInDto dto) {
    Member member = memberRepository.findByEmail(dto.getEmail())
        .orElseThrow(MemberNotExistException::new);

    if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
      throw new IncorrectPasswordException();
    }

    return new SignInResult(member.getEmail(), member.getRole());
  }

  /**
   * 이메일 전송
   */
  // TODO: Batch delete unused expired verification code in verification table
  @Transactional
  public void sendVerificationCode(SendVerificationCodeDto dto) {
    String email = dto.getEmail();

    checkCompanyDomainNotEmailProvider(email);
    checkAlreadyExistsMember(email);

    String code = VerificationCodeGenerator.generate(VERIFICATION_CODE_LENGTH);
    sendVerificationCodeEmail(email, code);
    saveVerificationCodeToDb(email, code);
  }

  /**
   * Spring Security의 UserDetailsService의 메서드 구현 Spring Security의 username으로 해당 서비스의 email이 사용됨
   */
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    return memberRepository.findByEmail(email)
        .map(SecurityMember::new)
        .orElseThrow(
            () -> new UsernameNotFoundException(String.format("Member email not found: %s", email))
        );
  }

  private void checkCompanyDomainNotEmailProvider(String email) {
    // only for dev
    if (bypassEmailDomainCheck) {
      return;
    }

    String domain = ExtractDomain.from(email);

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
    verificationRepository.findByEmail(email).ifPresent(verificationRepository::delete);

    // 새로운 인증번호 저장
    Verification verification = Verification.builder()
        .code(code)
        .expirationAt(LocalDateTime.now().plusSeconds(VERIFICATION_CODE_VALID_SECOND))
        .email(email)
        .build();

    verificationRepository.save(verification);
  }

  private void matchVerificationCode(SignUpDto dto, Verification verification) {
    if (dto.getNow().isAfter(verification.getExpirationAt())) {
      throw new VerificationCodeNotFound();
    }

    if (!verification.getCode().equals(dto.getVerificationCode())) {
      throw new VerificationCodeNotFound();
    }
  }

  private void saveMember(SignUpDto dto) {
    String encPassword = passwordEncoder.encode(dto.getPassword());
    Member member = Member.builder()
        .email(dto.getEmail())
        .password(encPassword)
        .name(dto.getName())
        .role(Role.USER)
        .build();

    memberRepository.save(member);
  }
}
