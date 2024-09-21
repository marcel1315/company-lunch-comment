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

  @Transactional
  public CreateCompanyDto.Response createCompany(CreateCompanyDto.Request dto) {
    if (companyRepository.existsCompanyByName(dto.getName())) {
      throw new SameCompanyNameExistException();
    }
    Company company = companyRepository.save(dto.toEntity());
    return CreateCompanyDto.Response.builder().id(company.getId()).build();
  }

  @Transactional
  public void sendVerificationCode(SendVerificationCodeDto dto) {
    String email = dto.getEmail();
    String code = GenerateVerificationCodeUtil.generate(VERIFICATION_CODE_LENGTH);
    sendVerificationCodeEmail(email, code);
    saveVerificationCodeToDb(email, code);
  }

  /**
   * Verification code is required.
   */
  @Transactional
  public void updateCompany(long id, UpdateCompanyDto dto) {
    String email = getMemberEmail();

    // Check if company exists.
    Company company = memberRepository.findByEmailAndCompanyId(email, id)
        .map(Member::getCompany)
        .orElseThrow(CompanyNotFoundException::new);

    // Check verification code.
    Verification verification = verificationRepository.findByEmail(email)
        .filter((v) -> v.getCode().equals(dto.getVerificationCode()))
        .filter((v) -> v.getExpirationAt().isAfter(dto.getNow()))
        .orElseThrow(VerificationCodeNotFoundException::new);

    // Update company info.
    company.setAddress(dto.getAddress());
    company.setLocation(dto.getLocation());
    company.setEnterKey(dto.getEnterKey());
    company.setEnterKeyEnabled(dto.getEnterKeyEnabled());

    verificationRepository.delete(verification);
  }

  public Page<CompanyOutputDto> getCompanyList(GetCompanyListDto dto) {
    Pageable pageable = PageRequest.of(
        dto.getPage(),
        dto.getSize()
    );
    return companyRepository.findAll(pageable)
        .map(CompanyOutputDto::of);
  }

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
    String subject = "[Our Company Lunch] This is the verification code for company update";
    String body = String.format(
        "The verification code is %s. Enter this code in company update field.", code);
    emailSender.sendMail(email, subject, body);
  }

  private void saveVerificationCodeToDb(String email, String code) {
    verificationRepository.findByEmail(email).ifPresent(verificationRepository::delete);

    Verification verification = Verification.builder()
        .code(code)
        .expirationAt(LocalDateTime.now().plusSeconds(VERIFICATION_CODE_VALID_SECOND))
        .email(email)
        .build();

    verificationRepository.save(verification);
  }

  /**
   * Doesn't query DB. SecurityContextHolder principal has email(username)
   */
  private String getMemberEmail() {
    return (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
