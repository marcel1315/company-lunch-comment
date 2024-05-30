package com.marceldev.companylunchcomment.repository;

import com.marceldev.companylunchcomment.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {

  boolean existsByDomainAndName(String domain, String name);
}
