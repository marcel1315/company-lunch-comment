package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.component.EmailSender;
import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyDto;
import com.marceldev.ourcompanylunch.dto.company.CompanyOutputDto;
import com.marceldev.ourcompanylunch.dto.company.CreateCompanyDto;
import com.marceldev.ourcompanylunch.dto.company.GetCompanyListDto;
import com.marceldev.ourcompanylunch.dto.company.UpdateCompanyDto;
import com.marceldev.ourcompanylunch.dto.member.SendVerificationCodeDto;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.Verification;
import com.marceldev.ourcompanylunch.exception.company.CompanyEnterKeyNotMatchException;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.company.SameCompanyNameExistException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.VerificationCodeNotFoundException;
import com.marceldev.ourcompanylunch.repository.company.CompanyRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.verification.VerificationRepository;
import com.marceldev.ourcompanylunch.util.GenerateVerificationCodeUtil;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

  private static final int VERIFICATION_CODE_VALID_SECOND = 60 * 3;

  private static final int VERIFICATION_CODE_LENGTH = 6;

  private final CompanyRepository companyRepository;

  private final VerificationRepository verificationRepository;

  private final MemberRepository memberRepository;

  private final EmailSender emailSender;

  /**
   * 회사 생성. 등록하는 사용자의 이메일 도메인을 활용한다. 같은 도메인 이름으로 동일한 회사명은 있을 수 없다.
   */
  @Transactional
  public void createCompany(CreateCompanyDto dto) {
    if (companyRepository.existsCompanyByName(dto.getName())) {
      throw new SameCompanyNameExistException();
    }
    Company company = dto.toEntity();

    companyRepository.save(company);
  }

  /**
   * 인증번호 발송. 회사 정보 수정을 위해 인정번호를 입력해야 함
   */
  @Transactional
  public void sendVerificationCode(SendVerificationCodeDto dto) {
    String email = dto.getEmail();
    String code = GenerateVerificationCodeUtil.generate(VERIFICATION_CODE_LENGTH);
    sendVerificationCodeEmail(email, code);
    saveVerificationCodeToDb(email, code);
  }

  /**
   * 회사 정보 수정. 인증번호가 맞아야 수정 가능
   */
  @Transactional
  public void updateCompany(long id, UpdateCompanyDto dto) {
    String email = getMemberEmail();

    // 회사가 존재하는지 확인
    Company company = memberRepository.findByEmailAndCompanyId(email, id)
        .map(Member::getCompany)
        .orElseThrow(CompanyNotFoundException::new);

    // 인증번호 확인
    Verification verification = verificationRepository.findByEmail(email)
        .filter((v) -> v.getCode().equals(dto.getVerificationCode()))
        .filter((v) -> v.getExpirationAt().isAfter(dto.getNow()))
        .orElseThrow(VerificationCodeNotFoundException::new);

    // 회사정보 업데이트
    company.setAddress(dto.getAddress());
    company.setLocation(dto.getLocation());
    company.setEnterKey(dto.getEnterKey());
    company.setEnterKeyEnabled(dto.getEnterKeyEnabled());

    verificationRepository.delete(verification);
  }

  /**
   * 회사 목록 보기. 로그인한 사용자의 이메일 도메인에 해당하는 회사들 목록만 볼 수 있음
   */
  public Page<CompanyOutputDto> getCompanyList(GetCompanyListDto dto) {
    Pageable pageable = PageRequest.of(
        dto.getPage(),
        dto.getSize()
    );
    return companyRepository.findAll(pageable)
        .map(CompanyOutputDto::of);
  }

  /**
   * 회사 선택하기
   */
  @Transactional
  public void chooseCompany(long companyId, ChooseCompanyDto dto) {
    String email = getMemberEmail();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(MemberNotFoundException::new);

    Company company = companyRepository.findById(companyId)
        .orElseThrow(CompanyNotFoundException::new);

    if (company.isEnterKeyEnabled() &&
        !company.getEnterKey().equals(dto.getEnterKey())) {
      throw new CompanyEnterKeyNotMatchException();
    }

    member.setCompany(company);
  }

  private void sendVerificationCodeEmail(String email, String code) {
    String subject = "[Our Company Lunch] 회사정보 수정을 위한 인증번호입니다";
    String body = String.format("인증번호는 %s 입니다. 회사정보 수정란에 입력해주세요.", code);
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

  /**
   * member email 을 반환함. DB 호출을 하지 않고, SecurityContextHolder 에 저장된 것을 사용
   */
  private String getMemberEmail() {
    return (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}