package com.marceldev.ourcompanylunch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marceldev.ourcompanylunch.basic.IntegrationTest;
import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerResponse;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerSubscription;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.diner.AlreadySubscribedException;
import com.marceldev.ourcompanylunch.exception.diner.DinerSubscriptionNotFoundException;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DinerSubscribeServiceTest extends IntegrationTest {

  @Test
  @DisplayName("Subscribe diner - Success")
  void subscribe_diner() {
    // given
    Company company = saveCompany();
    Member member = saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    // when
    dinerSubscribeService.subscribeDiner(diner.getId());

    // then
    Optional<DinerSubscription> subscription = dinerSubscriptionRepository.findByDinerAndMember(
        diner, member);
    assertThat(subscription).isNotEmpty();
  }

  @Test
  @DisplayName("Subscribe diner - Fail(Already subscribing)")
  void subscribe_diner_fail_already_subscribed() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");
    dinerSubscribeService.subscribeDiner(diner.getId());

    // when // then
    assertThatThrownBy(() -> dinerSubscribeService.subscribeDiner(diner.getId()))
        .isInstanceOf(AlreadySubscribedException.class);
  }

  @Test
  @DisplayName("Unsubscribe diner - Success")
  void unsubscribe_diner() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");
    dinerSubscribeService.subscribeDiner(diner.getId());

    // when
    dinerSubscribeService.unsubscribeDiner(diner.getId());

    // then
    Optional<DinerSubscription> subscription = dinerSubscriptionRepository.findById(diner.getId());
    assertThat(subscription).isEmpty();
  }

  @Test
  @DisplayName("Unsubscribe diner - Fail(No subscription)")
  void unsubscribe_diner_fail_no_subscription() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    // when // then
    assertThatThrownBy(() -> dinerSubscribeService.unsubscribeDiner(diner.getId()))
        .isInstanceOf(DinerSubscriptionNotFoundException.class);
  }

  // --- Save some entity ---

  private Company saveCompany() {
    Company company = Company.builder()
        .name("HelloCompany")
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .enterKey("company123")
        .enterKeyEnabled(false)
        .location(LocationUtil.createPoint(37.123456, 127.123456))
        .build();
    return companyRepository.save(company);
  }

  private Member saveMember() {
    Member member = Member.builder()
        .name("Jack")
        .email("jack@example.com")
        .company(null)
        .role(Role.VIEWER)
        .build();
    return memberRepository.save(member);
  }

  private void chooseCompany(Company company) {
    ChooseCompanyRequest chooseCompanyRequest = new ChooseCompanyRequest("company123");
    companyService.chooseCompany(company.getId(), chooseCompanyRequest);
  }

  private Diner saveDiner(String name) {
    CreateDinerRequest request = CreateDinerRequest.builder()
        .name(name)
        .link("diner.com")
        .latitude(37.29283882)
        .longitude(127.39232323)
        .tags(new LinkedHashSet<>())
        .build();
    CreateDinerResponse response = dinerService.createDiner(request);
    return dinerRepository.findById(response.getId()).orElseThrow();
  }
}