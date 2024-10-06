package com.marceldev.ourcompanylunch.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.marceldev.ourcompanylunch.component.EmailSender;
import com.marceldev.ourcompanylunch.component.S3Manager;
import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyRequest;
import com.marceldev.ourcompanylunch.dto.diner.AddDinerTagsRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerResponse;
import com.marceldev.ourcompanylunch.dto.diner.RemoveDinerTagsRequest;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.repository.company.CompanyRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerImageRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerSubscriptionRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.verification.VerificationRepository;
import com.marceldev.ourcompanylunch.security.WithCustomUser;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@WithCustomUser(username = "jack@example.com")
class DinerTagServiceTest {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private DinerRepository dinerRepository;

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private DinerImageRepository dinerImageRepository;

  @Autowired
  private DinerSubscriptionRepository dinerSubscriptionRepository;

  @Autowired
  private VerificationRepository verificationRepository;

  @Autowired
  private CompanyService companyService;

  @Autowired
  private DinerService dinerService;

  @MockBean
  private EmailSender emailSender;

  @MockBean
  private S3Manager s3Manager;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private DinerSubscribeService dinerSubscribeService;

  @Autowired
  private DinerTagService dinerTagService;

  @Test
  @DisplayName("Add diner tag - Success(When tags is empty)")
  void test_update_diner_add_tag_blank() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    AddDinerTagsRequest request = AddDinerTagsRequest.create(List.of("tag1", "tag2"));

    // when
    dinerTagService.addDinerTag(diner.getId(), request);

    // then
    Diner savedDiner = dinerRepository.findById(diner.getId()).orElseThrow();
    assertThat(savedDiner.getTags())
        .isEqualTo(new LinkedHashSet<>(List.of("tag1", "tag2")));
  }

  @Test
  @DisplayName("Add diner tag - Success(When other tags exists. Existing tags should remain.)")
  void test_update_diner_add_tag_not_blank() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    AddDinerTagsRequest request1 =  AddDinerTagsRequest.create(List.of("tag1", "tag2"));
    dinerTagService.addDinerTag(diner.getId(), request1);

    AddDinerTagsRequest request2 =  AddDinerTagsRequest.create(List.of("tag2", "tag3"));

    // when
    dinerTagService.addDinerTag(diner.getId(), request2);

    // then
    Diner savedDiner = dinerRepository.findById(diner.getId()).orElseThrow();
    assertThat(savedDiner.getTags())
        .isEqualTo(new LinkedHashSet<>(List.of("tag1", "tag2", "tag3")));
  }

  @Test
  @DisplayName("Remove diner tag - Success")
  void test_update_diner_remove_tag() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    AddDinerTagsRequest request1 = AddDinerTagsRequest.create(List.of("tag1", "tag2"));
    dinerTagService.addDinerTag(diner.getId(), request1);

    RemoveDinerTagsRequest removeRequest = RemoveDinerTagsRequest.create(List.of("tag2"));

    //when
    dinerTagService.removeDinerTag(diner.getId(), removeRequest);

    //then
    Diner savedDiner = dinerRepository.findById(diner.getId()).orElseThrow();
    assertThat(savedDiner.getTags())
        .isEqualTo(new LinkedHashSet<>(List.of("tag1")));
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