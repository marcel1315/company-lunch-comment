package com.marceldev.ourcompanylunch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.marceldev.ourcompanylunch.component.EmailSender;
import com.marceldev.ourcompanylunch.component.S3Manager;
import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyRequest;
import com.marceldev.ourcompanylunch.dto.company.CompanyOutputDto;
import com.marceldev.ourcompanylunch.dto.company.CreateCompanyRequest;
import com.marceldev.ourcompanylunch.dto.company.CreateCompanyResponse;
import com.marceldev.ourcompanylunch.dto.company.GetCompanyListRequest;
import com.marceldev.ourcompanylunch.dto.company.UpdateCompanyRequest;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.Verification;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.company.SameCompanyNameExistException;
import com.marceldev.ourcompanylunch.exception.member.VerificationCodeNotFoundException;
import com.marceldev.ourcompanylunch.repository.company.CompanyRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.verification.VerificationRepository;
import com.marceldev.ourcompanylunch.security.WithCustomUser;
import com.marceldev.ourcompanylunch.type.CompanySort;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.type.SortDirection;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import java.time.LocalDateTime;
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
class CompanyServiceTest {

  @Autowired
  private CompanyRepository companyRepository;

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private VerificationRepository verificationRepository;

  @Autowired
  private CompanyService companyService;

  @MockBean
  private EmailSender emailSender;

  @MockBean
  private S3Manager s3Manager;

  @Test
  @DisplayName("Create company - Success")
  void create_company() {
    // given
    CreateCompanyRequest request = createCreateCompanyRequest("HelloCompany");

    // when
    CreateCompanyResponse response = companyService.createCompany(request);

    // then
    assertThat(response.getId()).isNotNull();
    assertThat(response)
        .extracting("name", "address", "enterKey", "enterKeyEnabled", "latitude", "longitude")
        .contains("HelloCompany",
            "321, Teheran-ro Gangnam-gu Seoul",
            "company123",
            false,
            37.123456,
            127.123456
        );

  }

  @Test
  @DisplayName("Create company - Fail(Same company name exists)")
  void create_company_fail_same_name_exist() {
    // given
    CreateCompanyRequest request1 = createCreateCompanyRequest("HelloCompany1");
    CreateCompanyRequest request2 = createCreateCompanyRequest("HelloCompany2");
    CreateCompanyRequest request3 = createCreateCompanyRequest("HelloCompany1");
    companyService.createCompany(request1);
    companyService.createCompany(request2);

    // when // then
    assertThatThrownBy(() -> companyService.createCompany(request3))
        .isInstanceOf(SameCompanyNameExistException.class);
  }

  @Test
  @DisplayName("Choose company - Success")
  void choose_company() {
    // given
    Company company = saveCompany();
    Member member = saveMember();
    ChooseCompanyRequest chooseCompanyRequest = createChooseCompanyRequest();

    //when
    companyService.chooseCompany(company.getId(), chooseCompanyRequest);

    //then
    Member updatedMember = memberRepository.findById(member.getId()).orElseThrow();
    assertThat(updatedMember.getCompany().getId())
        .isEqualTo(company.getId());
  }

  @Test
  @DisplayName("Choose company - Fail(Company not found)")
  void choose_company_fail_no_company() {
    // given
    // Not saving company
    Member member = saveMember();
    ChooseCompanyRequest chooseCompanyRequest = createChooseCompanyRequest();

    // when // then
    assertThatThrownBy(() -> companyService.chooseCompany(1L, chooseCompanyRequest))
        .isInstanceOf(CompanyNotFoundException.class);
  }

  @Test
  @DisplayName("Update company - Success")
  void update_company() {
    // given
    Company company = saveCompany();
    Member member = saveMember();
    chooseCompany(company);

    Verification verification = saveVerification(member.getEmail(), "123456");
    UpdateCompanyRequest updateRequest = createUpdateRequest(
        "111, Teheran-ro Gangnam-gu Seoul",
        verification.getCode()
    );

    // when
    companyService.updateCompany(company.getId(), updateRequest);

    // then
    Company updatedCompany = companyRepository.findById(company.getId()).orElseThrow();
    assertThat(updatedCompany.getAddress())
        .isEqualTo("111, Teheran-ro Gangnam-gu Seoul");
  }

  @Test
  @DisplayName("Update company - Fail(Company not found)")
  void update_company_fail_no_company() {
    // given
    Company company = saveCompany();
    Member member = saveMember();
    // Not choosing company

    Verification verification = saveVerification(member.getEmail(), "123456");
    UpdateCompanyRequest request = createUpdateRequest(
        "111, Teheran-ro Gangnam-gu Seoul",
        verification.getCode()
    );

    // when // then
    assertThatThrownBy(() -> companyService.updateCompany(company.getId(), request))
        .isInstanceOf(CompanyNotFoundException.class);
  }

  @Test
  @DisplayName("Update company - Fail(Verification code incorrect)")
  void update_company_fail_verification_code_incorrect() {
    //given
    Company company = saveCompany();
    Member member = saveMember();
    chooseCompany(company);

    saveVerification(member.getEmail(), "123456");
    UpdateCompanyRequest request = createUpdateRequest(
        "111, Teheran-ro Gangnam-gu Seoul",
        "111111" // Input different code
    );

    //when //then
    assertThatThrownBy(() -> companyService.updateCompany(company.getId(), request))
        .isInstanceOf(VerificationCodeNotFoundException.class);
  }

  @Test
  @DisplayName("Update company - Fail(Verification code expiration date is before now)")
  void update_company_fail_verification_code_expired() {
    // given
    Company company = saveCompany();
    Member member = saveMember();
    chooseCompany(company);

    Verification verification = saveVerification(member.getEmail(), "123456", -1);
    UpdateCompanyRequest request = createUpdateRequest(
        "111, Teheran-ro Gangnam-gu Seoul",
        verification.getCode()
    );

    // when // then
    assertThatThrownBy(() -> companyService.updateCompany(company.getId(), request))
        .isInstanceOf(
            VerificationCodeNotFoundException.class); // expired, but it's same as not found.
  }

  @Test
  @DisplayName("Get company list - Success")
  void get_company_list() {
    // given
    CreateCompanyRequest request1 = createCreateCompanyRequest("HelloCompany1");
    CreateCompanyRequest request2 = createCreateCompanyRequest("HelloCompany2");
    companyService.createCompany(request1);
    companyService.createCompany(request2);

    GetCompanyListRequest getRequest = GetCompanyListRequest.builder()
        .page(0)
        .size(10)
        .sortBy(CompanySort.COMPANY_NAME)
        .sortDirection(SortDirection.ASC)
        .build();

    // when
    Page<CompanyOutputDto> companies = companyService.getCompanyList(getRequest);

    // then
    assertThat(companies.getContent()).hasSize(2)
        .extracting("name", "address", "latitude", "longitude")
        .containsExactly(
            tuple("HelloCompany1",
                "321, Teheran-ro Gangnam-gu Seoul",
                37.123456,
                127.123456
            ),
            tuple("HelloCompany2",
                "321, Teheran-ro Gangnam-gu Seoul",
                37.123456,
                127.123456
            )
        );
  }

  @Test
  @DisplayName("Get company list - Success(Blank list)")
  void get_company_list_empty_page() {
    //given
    GetCompanyListRequest getRequest = GetCompanyListRequest.builder()
        .page(0)
        .size(10)
        .sortBy(CompanySort.COMPANY_NAME)
        .sortDirection(SortDirection.ASC)
        .build();

    //when
    Page<CompanyOutputDto> companies = companyService.getCompanyList(getRequest);

    //then
    assertThat(companies.getContent()).hasSize(0);
  }

  // --- Save some entity ---

  private Verification saveVerification(String email, String code) {
    Verification verification = Verification.builder()
        .email(email)
        .code(code)
        .expirationAt(LocalDateTime.now().plusMinutes(3))
        .build();
    return verificationRepository.save(verification);
  }

  private Verification saveVerification(String email, String code, int minuteAfter) {
    Verification verification = Verification.builder()
        .email(email)
        .code(code)
        .expirationAt(LocalDateTime.now().plusMinutes(minuteAfter))
        .build();
    return verificationRepository.save(verification);
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

  private void chooseCompany(Company company) {
    ChooseCompanyRequest chooseCompanyRequest = createChooseCompanyRequest();
    companyService.chooseCompany(company.getId(), chooseCompanyRequest);
  }

  // --- Create some request ---

  private CreateCompanyRequest createCreateCompanyRequest(String companyName) {
    return CreateCompanyRequest.builder()
        .name(companyName)
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .enterKey("company123")
        .enterKeyEnabled(false)
        .latitude(37.123456)
        .longitude(127.123456)
        .build();
  }

  private ChooseCompanyRequest createChooseCompanyRequest() {
    return new ChooseCompanyRequest("company123");
  }

  private UpdateCompanyRequest createUpdateRequest(String address, String verificationCode) {
    return UpdateCompanyRequest.builder()
        .address(address)
        .latitude(37.123456)
        .longitude(127.123456)
        .enterKeyEnabled(false)
        .verificationCode(verificationCode)
        .build();
  }
}