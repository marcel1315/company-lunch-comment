package com.marceldev.companylunchcomment.repository.company;

import com.marceldev.companylunchcomment.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

  boolean existsByDomainAndName(String domain, String name);

  Page<Company> findByDomain(String domain, Pageable pageable);
}
