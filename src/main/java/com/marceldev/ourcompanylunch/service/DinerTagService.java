package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.dto.diner.AddDinerTagsRequest;
import com.marceldev.ourcompanylunch.dto.diner.RemoveDinerTagsRequest;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class DinerTagService {

  private final MemberRepository memberRepository;

  private final DinerRepository dinerRepository;

  @Transactional
  public void addDinerTag(long id, AddDinerTagsRequest dto) {
    Diner diner = getDiner(id);
    dto.getTags().forEach(diner::addTag);
  }

  @Transactional
  public void removeDinerTag(long id, RemoveDinerTagsRequest dto) {
    Diner diner = getDiner(id);
    dto.getTags().forEach(diner::removeTag);
  }

  /**
   * Get diner. Check if the member can access the diner.
   */
  private Diner getDiner(long dinerId) {
    Company company = getCompany();
    return dinerRepository.findById(dinerId)
        .filter((diner) -> diner.getCompany().equals(company))
        .orElseThrow(() -> new DinerNotFoundException(dinerId));
  }

  /**
   * Get company. Check if the member can access the company.
   */
  private Company getCompany() {
    Member member = getMember();
    if (member.getCompany() == null) {
      throw new CompanyNotFoundException();
    }
    return member.getCompany();
  }

  private Member getMember() {
    String email = (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    if (email == null) {
      throw new MemberUnauthorizedException();
    }
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotFoundException::new);
  }
}
