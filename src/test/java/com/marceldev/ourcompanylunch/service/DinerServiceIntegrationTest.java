package com.marceldev.ourcompanylunch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.marceldev.ourcompanylunch.component.EmailSender;
import com.marceldev.ourcompanylunch.component.S3Manager;
import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerResponse;
import com.marceldev.ourcompanylunch.dto.diner.DinerDetailOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.DinerOutputDto;
import com.marceldev.ourcompanylunch.dto.diner.GetDinerListRequest;
import com.marceldev.ourcompanylunch.dto.diner.UpdateDinerRequest;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.repository.company.CompanyRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerImageRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerSubscriptionRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.verification.VerificationRepository;
import com.marceldev.ourcompanylunch.security.WithCustomUser;
import com.marceldev.ourcompanylunch.type.DinerSort;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.type.SortDirection;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@WithCustomUser(username = "jack@example.com")
class DinerServiceIntegrationTest {

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private DinerRepository dinerRepository;

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
  private DinerImageService dinerImageService;

  @BeforeEach
  public void setUp() {
    setUpH2Procedure();
  }

  private void setUpH2Procedure() {
    // Test db(H2) doesn't have ST_Distance_sphere procedure. So create dummy procedure.
    entityManager.createNativeQuery(
        "CREATE ALIAS IF NOT EXISTS ST_Distance_Sphere FOR \"com.marceldev.ourcompanylunch.etc.H2Functions.stDistanceSphere\""
    ).executeUpdate();
  }

  @Test
  @DisplayName("Create diner - Success")
  void test_create_diner() {
    // given
    Company company = saveCompany();
    Member member = saveMember();
    chooseCompany(company);

    LinkedHashSet<String> tags = new LinkedHashSet<>(
        List.of("Mexico", "Good atmosphere")
    );

    CreateDinerRequest request = createCreateDinerRequest(tags);

    // when
    CreateDinerResponse response = dinerService.createDiner(request);

    // then
    assertThat(response)
        .extracting("name", "link", "latitude", "longitude", "tags")
        .containsExactly(
            "Gamsung Taco", "diner.com", 37.29283882, 127.39232323,
            new HashSet<>(List.of("Mexico", "Good atmosphere"))
        );
  }

  @Test
  @DisplayName("Create diner - Fail(No company chosen)")
  void test_create_diner_no_company_chosen() {
    // given
    saveCompany();
    saveMember();

    CreateDinerRequest request = createCreateDinerRequest();

    // when // then
    assertThatThrownBy(() -> dinerService.createDiner(request))
        .isInstanceOf(CompanyNotFoundException.class);
  }

  @Test
  @DisplayName("Get diner list - Success")
  void test_get_diner_list() {
    //given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);

    CreateDinerRequest createDinerRequest1 = createCreateDinerRequest("Gamsung Taco1");
    CreateDinerRequest createDinerRequest2 = createCreateDinerRequest("Gamsung Taco2");
    dinerService.createDiner(createDinerRequest1);
    dinerService.createDiner(createDinerRequest2);

    GetDinerListRequest request = GetDinerListRequest.builder()
        .page(0)
        .size(10)
        .sortBy(DinerSort.DINER_NAME)
        .sortDirection(SortDirection.ASC)
        .build();

    //when
    Page<DinerOutputDto> page = dinerService.getDinerList(request);

    //then
    assertThat(page.getContent()).hasSize(2)
        .extracting("name", "link", "latitude", "longitude", "tags")
        .containsExactly(
            tuple("Gamsung Taco1", "diner.com", 37.29283882, 127.39232323, new HashSet<>()),
            tuple("Gamsung Taco2", "diner.com", 37.29283882, 127.39232323, new HashSet<>())
        );
  }

  @Test
  @DisplayName("Get diner detail - Success")
  void test_get_diner_detail() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);

    CreateDinerRequest createDinerRequest = createCreateDinerRequest("Gamsung Taco");
    CreateDinerResponse response = dinerService.createDiner(createDinerRequest);

    // when
    DinerDetailOutputDto dinerDetail = dinerService.getDinerDetail(response.getId());

    // then
    assertThat(dinerDetail)
        .extracting("name", "link", "latitude", "longitude", "tags")
        .containsExactly("Gamsung Taco", "diner.com", 37.29283882, 127.39232323, new HashSet<>());
  }

  @Test
  @DisplayName("Update diner - Success")
  void test_update_diner() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);

    CreateDinerRequest createDinerRequest = createCreateDinerRequest();
    CreateDinerResponse response = dinerService.createDiner(createDinerRequest);

    UpdateDinerRequest request = UpdateDinerRequest.builder()
        .link("diner1.com")
        .latitude(37.11111111)
        .longitude(127.11111111)
        .build();

    //when
    dinerService.updateDiner(response.getId(), request);

    //then
    DinerDetailOutputDto dinerDetail = dinerService.getDinerDetail(response.getId());
    assertThat(dinerDetail)
        .extracting("name", "link", "latitude", "longitude", "tags")
        .containsExactly("Gamsung Taco", "diner1.com", 37.11111111, 127.11111111, new HashSet<>());
  }

  @Test
  @DisplayName("Update diner - Fail(Diner not found)")
  void test_update_diner_fail_not_found() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    // Not saving Diner

    UpdateDinerRequest dto = UpdateDinerRequest.builder()
        .link("diner1.com")
        .build();

    // when // then
    assertThatThrownBy(() -> dinerService.updateDiner(1L, dto))
        .isInstanceOf(DinerNotFoundException.class);
  }

  @Test
  @DisplayName("Remove diner - Success")
  void test_remove_diner() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);

    CreateDinerRequest createDinerRequest = createCreateDinerRequest();
    CreateDinerResponse response = dinerService.createDiner(createDinerRequest);

    // when
    dinerService.removeDiner(response.getId());

    // then
    Optional<Diner> diner = dinerRepository.findById(response.getId());
    assertThat(diner).isEmpty();
  }

  @Test
  @DisplayName("Remove diner - Success(Removing diner success, even if removing in S3 fails.")
  void test_remove_diner_even_if_s3_fail() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);

    CreateDinerRequest createDinerRequest = createCreateDinerRequest();
    CreateDinerResponse response = dinerService.createDiner(createDinerRequest);

    // when
    doThrow(new RuntimeException())
        .when(s3Manager).removeFile(any());
    dinerService.removeDiner(response.getId());

    // then
    Optional<Diner> diner = dinerRepository.findById(response.getId());
    assertThat(diner).isEmpty();
  }

  // --- Create some request ---

  private CreateDinerRequest createCreateDinerRequest() {
    return createCreateDinerRequest("Gamsung Taco");
  }

  private CreateDinerRequest createCreateDinerRequest(String name) {
    return CreateDinerRequest.builder()
        .name(name)
        .link("diner.com")
        .latitude(37.29283882)
        .longitude(127.39232323)
        .tags(new LinkedHashSet<>())
        .build();
  }

  private CreateDinerRequest createCreateDinerRequest(LinkedHashSet<String> tags) {
    return CreateDinerRequest.builder()
        .name("Gamsung Taco")
        .link("diner.com")
        .latitude(37.29283882)
        .longitude(127.39232323)
        .tags(tags)
        .build();
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
}