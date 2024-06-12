package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.component.EmailSender;
import com.marceldev.companylunchcomment.dto.company.CompanyOutputDto;
import com.marceldev.companylunchcomment.dto.company.CreateCompanyDto;
import com.marceldev.companylunchcomment.dto.company.GetCompanyListDto;
import com.marceldev.companylunchcomment.dto.company.UpdateCompanyDto;
import com.marceldev.companylunchcomment.dto.member.SendVerificationCodeDto;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.Verification;
import com.marceldev.companylunchcomment.exception.CompanyNotExistException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.exception.SameCompanyNameExist;
import com.marceldev.companylunchcomment.exception.VerificationCodeNotFound;
import com.marceldev.companylunchcomment.repository.company.CompanyRepository;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import com.marceldev.companylunchcomment.repository.verification.VerificationRepository;
import com.marceldev.companylunchcomment.util.ExtractDomain;
import com.marceldev.companylunchcomment.util.VerificationCodeGenerator;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    String email = getMemberEmail();
    String domain = ExtractDomain.from(email);

    if (companyRepository.existsByDomainAndName(domain, dto.getName())) {
      throw new SameCompanyNameExist();
    }
    Company company = dto.toEntityWithDomain(domain);

    companyRepository.save(company);
  }

  /**
   * 인증번호 발송. 회사 정보 수정을 위해 인정번호를 입력해야 함
   */
  @Transactional
  public void sendVerificationCode(SendVerificationCodeDto dto) {
    String email = dto.getEmail();
    String code = VerificationCodeGenerator.generate(VERIFICATION_CODE_LENGTH);
    sendVerificationCodeEmail(email, code);
    saveVerificationCodeToDb(email, code);
  }

  /**
   * 회사 정보 수정. 인증번호가 맞아야 수정 가능
   */
  @Transactional
  public void updateCompany(long id, UpdateCompanyDto updateCompanyDto) {
    String email = getMemberEmail();

    // 회사가 존재하는지 확인
    Company company = memberRepository.findByEmailAndCompanyId(email, id)
        .map(Member::getCompany)
        .orElseThrow(CompanyNotExistException::new);

    // 인증번호 확인
    Verification verification = verificationRepository.findByEmail(email)
        .filter((v) -> v.getCode().equals(updateCompanyDto.getVerificationCode()))
        .filter((v) -> v.getExpirationAt().isAfter(LocalDateTime.now()))
        .orElseThrow(VerificationCodeNotFound::new);

    // 회사정보 업데이트
    Optional.ofNullable(updateCompanyDto.getAddress())
        .ifPresent(company::setAddress);
    company.setLocation(updateCompanyDto.getLocation());

    verificationRepository.delete(verification);
  }

  /**
   * 회사 목록 보기. 로그인한 사용자의 이메일 도메인에 해당하는 회사들 목록만 볼 수 있음
   */
  public Page<CompanyOutputDto> getCompanyList(GetCompanyListDto dto, Pageable pageable) {
    String email = getMemberEmail();
    return companyRepository.findByDomain(ExtractDomain.from(email), pageable)
        .map(CompanyOutputDto::of);
  }

  /**
   * 회사 선택하기
   */
  @Transactional
  public void chooseCompany(long companyId) {
    String email = getMemberEmail();
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(MemberNotExistException::new);
    Company company = companyRepository.findById(companyId)
        .filter(c -> c.getDomain().equals(ExtractDomain.from(email)))
        .orElseThrow(CompanyNotExistException::new);

    member.setCompany(company);
  }

  private void sendVerificationCodeEmail(String email, String code) {
    String subject = "[Company Lunch Comment] 회사정보 수정을 위한 인증번호입니다";
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
   * member email을 반환함. DB 호출을 하지 않고, SecurityContextHolder에 저장된 것을 사용
   */
  private String getMemberEmail() {
    UserDetails user = (UserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    return user.getUsername();
  }
}
