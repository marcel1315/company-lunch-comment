package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.company.CreateCompanyDto;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.exception.SameCompanyNameExist;
import com.marceldev.companylunchcomment.repository.CompanyRepository;
import com.marceldev.companylunchcomment.type.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CompanyService {

  private final CompanyRepository companyRepository;

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
}
