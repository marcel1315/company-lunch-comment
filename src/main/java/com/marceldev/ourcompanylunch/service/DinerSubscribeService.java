package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerSubscription;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.AlreadySubscribedException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerSubscriptionNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerSubscriptionRepository;
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
public class DinerSubscribeService {

  private final MemberRepository memberRepository;

  private final DinerRepository dinerRepository;

  private final DinerSubscriptionRepository dinerSubscriptionRepository;

  @Transactional
  public void subscribeDiner(long id) {
    Diner diner = getDiner(id);
    Member member = getMember();

    if (dinerSubscriptionRepository.existsByDinerAndMember(diner, member)) {
      throw new AlreadySubscribedException();
    }

    dinerSubscriptionRepository.save(DinerSubscription.builder()
        .diner(diner)
        .member(member)
        .build());
  }

  @Transactional
  public void unsubscribeDiner(long id) {
    Diner diner = getDiner(id);
    Member member = getMember();

    DinerSubscription dinerSubscription = dinerSubscriptionRepository.findByDinerAndMember(diner,
            member)
        .orElseThrow(DinerSubscriptionNotFoundException::new);

    dinerSubscriptionRepository.delete(dinerSubscription);
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
