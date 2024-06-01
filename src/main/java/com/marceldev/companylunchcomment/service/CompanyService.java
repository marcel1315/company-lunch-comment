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
import com.marceldev.companylunchcomment.exception.SameCompanyNameExist;
import com.marceldev.companylunchcomment.exception.VerificationCodeNotFound;
import com.marceldev.companylunchcomment.repository.CompanyRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.repository.VerificationRepository;
import com.marceldev.companylunchcomment.type.Email;
import com.marceldev.companylunchcomment.util.VerificationCodeGenerator;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
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
  public void createCompany(CreateCompanyDto dto, String memberEmail) {
    String domain = Email.of(memberEmail).getDomain();

    if (companyRepository.existsByDomainAndName(domain, dto.getName())) {
      throw new SameCompanyNameExist();
    }
    Company company = dto.toEntityWithDomain(domain);

    companyRepository.save(company);
  }

  /**
   * 인증번호 발송. 회사 정보 수정을 위해 인정번호를 입력해야 함
   */
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
  public void updateCompany(long id, UpdateCompanyDto updateCompanyDto, String email) {
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
    Optional.ofNullable(updateCompanyDto.getLatitude())
        .ifPresent(company::setLatitude);
    Optional.ofNullable(updateCompanyDto.getLongitude())
        .ifPresent(company::setLongitude);

    companyRepository.save(company);
    verificationRepository.delete(verification);
  }

  /**
   * 회사 목록 보기. 로그인한 사용자의 이메일 도메인에 해당하는 회사들 목록만 볼 수 있음
   */
  public Page<CompanyOutputDto> getCompanyList(GetCompanyListDto dto, String email) {
    return companyRepository.findByDomain(
            Email.of(email).getDomain(),
            PageRequest.of(dto.getPage(), dto.getPageSize(), dto.getSort())
        )
        .map(CompanyOutputDto::of);
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

}
