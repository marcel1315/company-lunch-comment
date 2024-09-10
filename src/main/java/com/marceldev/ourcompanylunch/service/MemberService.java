package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.component.EmailSender;
import com.marceldev.ourcompanylunch.dto.member.SendVerificationCodeDto;
import com.marceldev.ourcompanylunch.dto.member.UpdateMemberDto;
import com.marceldev.ourcompanylunch.dto.member.VerifyVerificationCodeDto;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.Verification;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.exception.member.VerificationCodeNotFoundException;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.verification.VerificationRepository;
import com.marceldev.ourcompanylunch.util.GenerateVerificationCodeUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

  private static final int VERIFICATION_CODE_VALID_SECOND = 60 * 3;

  private static final int VERIFICATION_CODE_LENGTH = 6;

  private final MemberRepository memberRepository;

  private final EmailSender emailSender;

  private final VerificationRepository verificationRepository;

  /**
   * 이메일로 인증번호 전송
   */
  @Transactional
  public void sendVerificationCode(SendVerificationCodeDto dto) {
    String email = dto.getEmail();
    String code = GenerateVerificationCodeUtil.generate(VERIFICATION_CODE_LENGTH);

    sendVerificationCodeEmail(email, code);
    saveVerificationCodeToDb(email, code);
  }

  /**
   * 인증번호 검증
   */
  @Transactional
  public void verifyVerificationCode(VerifyVerificationCodeDto dto) {
    Verification verification = verificationRepository.findByEmail(dto.getEmail())
        .orElseThrow(VerificationCodeNotFoundException::new);

    matchVerificationCode(dto.getCode(), verification, dto.getNow());

    Member member = getMember();
    member.promoteToEditor();

    verificationRepository.delete(verification);
  }

  /**
   * 회원정보 수정
   */
  @Transactional
  public void updateMember(long id, UpdateMemberDto dto) {
    Member member = getMember(id);
    member.setName(dto.getName());
  }

  @Scheduled(cron = "${scheduler.clear-verification-code.cron}")
  public void clearUnusedVerificationCodes() {
    int rows = verificationRepository.deleteAllExpiredVerificationCode(LocalDateTime.now());
    log.info("MemberService.clearUnusedVerificationCodes executed: {} rows deleted", rows);
  }

  private void sendVerificationCodeEmail(String email, String code) {
    String subject = "[Our Company Lunch] 인증번호입니다.";
    String body = String.format("인증번호는 %s 입니다. 인증번호란에 입력해주세요.", code);
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

  private void matchVerificationCode(String code, Verification verification, LocalDateTime now) {
    if (now.isAfter(verification.getExpirationAt())) {
      throw new VerificationCodeNotFoundException();
    }

    if (!verification.getCode().equals(code)) {
      throw new VerificationCodeNotFoundException();
    }
  }

  /**
   * member 를 찾아 반환함. 토큰에 들어있던 사용자가 접근할 수 있는 member id 인지 체크하고 반환함
   */
  private Member getMember(long id) {
    String email = (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    return memberRepository.findByIdAndEmail(id, email)
        .orElseThrow(MemberUnauthorizedException::new);
  }

  /**
   * member 를 찾아 반환함.
   */
  private Member getMember() {
    String email = (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotFoundException::new);
  }
}
