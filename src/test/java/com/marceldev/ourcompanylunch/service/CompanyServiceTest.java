package com.marceldev.ourcompanylunch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyDto;
import com.marceldev.ourcompanylunch.dto.company.CompanyOutputDto;
import com.marceldev.ourcompanylunch.dto.company.CreateCompanyDto;
import com.marceldev.ourcompanylunch.dto.company.GetCompanyListDto;
import com.marceldev.ourcompanylunch.dto.company.UpdateCompanyDto;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.Verification;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.company.SameCompanyNameExistException;
import com.marceldev.ourcompanylunch.exception.member.VerificationCodeNotFoundException;
import com.marceldev.ourcompanylunch.repository.company.CompanyRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.verification.VerificationRepository;
import com.marceldev.ourcompanylunch.type.CompanySort;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.type.SortDirection;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private VerificationRepository verificationRepository;

  @InjectMocks
  private CompanyService companyService;

  // Mock company.
  // When retrieving diner, if it's not the diner in the company of the member, the diner is not accessible.
  private final Company company1 = Company.builder()
      .id(1L)
      .name("HelloCompany")
      .address("123, Gangnam-daero Gangnam-gu Seoul")
      .location(LocationUtil.createPoint(127.123123, 37.123123))
      .enterKey("company123")
      .build();

  // Mock member
  private final Member member1 = Member.builder()
      .id(1L)
      .email("jack@example.com")
      .name("Jack")
      .role(Role.VIEWER)
      .company(company1)
      .build();

  @BeforeEach
  public void setupMember() {
    GrantedAuthority authority = new SimpleGrantedAuthority("VIEWER");
    Authentication authentication = new UsernamePasswordAuthenticationToken(member1.getEmail(),
        null, List.of(authority));

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    lenient().when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(member1));
  }

  @AfterEach
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Create company - Success")
  void create_company() {
    //given
    CreateCompanyDto dto = CreateCompanyDto.builder()
        .name("HelloCompany")
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .enterKey("company123")
        .enterKeyEnabled(false)
        .latitude(37.123456)
        .longitude(127.123456)
        .build();
    when(companyRepository.existsCompanyByName(any()))
        .thenReturn(false);

    //when
    companyService.createCompany(dto);
    ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);

    //then
    verify(companyRepository).save(captor.capture());
    assertEquals("HelloCompany", captor.getValue().getName());
    assertEquals("321, Teheran-ro Gangnam-gu Seoul", captor.getValue().getAddress());
    assertEquals(LocationUtil.createPoint(127.123456, 37.123456), captor.getValue().getLocation());
    assertEquals("company123", captor.getValue().getEnterKey());
  }

  @Test
  @DisplayName("Create company - Fail(Same company name exists)")
  void create_company_fail_same_email_domain() {
    //given
    CreateCompanyDto dto = CreateCompanyDto.builder()
        .name("HelloCompany")
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .latitude(37.123456)
        .longitude(127.123456)
        .build();
    when(companyRepository.existsCompanyByName(any()))
        .thenReturn(true);

    //when
    //then
    assertThrows(SameCompanyNameExistException.class,
        () -> companyService.createCompany(dto));
  }

  @Test
  @DisplayName("Update company - Success")
  void update_company() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .latitude(37.123456)
        .longitude(127.123456)
        .enterKeyEnabled(false)
        .verificationCode("123456")
        .build();
    when(memberRepository.findByEmailAndCompanyId(any(), anyLong()))
        .thenReturn(Optional.of(Member.builder()
            .company(Company.builder().id(1L).build())
            .build()
        ));
    when(verificationRepository.findByEmail(any()))
        .thenReturn(Optional.of(Verification.builder()
            .code("123456")
            .expirationAt(LocalDateTime.now().plusMinutes(2))
            .build()
        ));

    //when
    //then
    companyService.updateCompany(1L, dto);
  }

  @Test
  @DisplayName("Update company - Fail(Company not found)")
  void update_company_fail_no_company() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .latitude(37.123456)
        .longitude(127.123456)
        .verificationCode("123456")
        .build();
    when(memberRepository.findByEmailAndCompanyId(any(), anyLong()))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(CompanyNotFoundException.class,
        () -> companyService.updateCompany(1L, dto));
  }

  @Test
  @DisplayName("Update company - Fail(Verification code incorrect)")
  void update_company_fail_verification_code_incorrect() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .latitude(37.123456)
        .longitude(127.123456)
        .verificationCode("123456")
        .build();
    when(memberRepository.findByEmailAndCompanyId(any(), anyLong()))
        .thenReturn(Optional.of(Member.builder()
            .company(Company.builder().id(1L).build())
            .build()
        ));
    when(verificationRepository.findByEmail(any()))
        .thenReturn(Optional.of(Verification.builder()
            .code("111111")
            .expirationAt(LocalDateTime.now().plusMinutes(2))
            .build()
        ));

    //when
    //then
    assertThrows(VerificationCodeNotFoundException.class,
        () -> companyService.updateCompany(1L, dto));
  }

  @Test
  @DisplayName("Update company - Fail(Verification code expiration date is before now)")
  void update_company_fail_verification_code_expired() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .latitude(37.123456)
        .longitude(127.123456)
        .verificationCode("123456")
        .build();
    when(memberRepository.findByEmailAndCompanyId(any(), anyLong()))
        .thenReturn(Optional.of(Member.builder()
            .company(Company.builder().id(1L).build())
            .build()
        ));
    when(verificationRepository.findByEmail(any()))
        .thenReturn(Optional.of(Verification.builder()
            .code("123456")
            .expirationAt(LocalDateTime.now().minusMinutes(2))
            .build()
        ));

    //when
    //then
    assertThrows(VerificationCodeNotFoundException.class,
        () -> companyService.updateCompany(1L, dto));
  }

  @Test
  @DisplayName("Get company list - Success")
  void get_company_list() {
    //given
    GetCompanyListDto dto = GetCompanyListDto.builder()
        .page(0)
        .size(10)
        .sortBy(CompanySort.COMPANY_NAME)
        .sortDirection(SortDirection.ASC)
        .build();
    String email = "hello@example.com";
    Company company1 = Company.builder()
        .id(1L)
        .name("Emotion Taco Gangnam branch")
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .location(LocationUtil.createPoint(127.123456, 37.123456))
        .build();
    Company company2 = Company.builder()
        .id(2L)
        .name("Emotion Taco Sinsa branch")
        .address("432, Teheran-ro Gangnam-gu Seoul")
        .location(LocationUtil.createPoint(127.123457, 37.123457))
        .build();

    Page<Company> pages = new PageImpl<>(List.of(company1, company2));
    PageRequest pageable = PageRequest.of(0, 10);
    when(companyRepository.findAll(pageable))
        .thenReturn(pages);

    //when
    Page<CompanyOutputDto> companies = companyService.getCompanyList(dto);

    //then
    assertEquals(2, companies.getSize());
  }

  @Test
  @DisplayName("Get company list - Success(Blank list)")
  void get_company_list_empty_page() {
    //given
    GetCompanyListDto dto = GetCompanyListDto.builder()
        .page(0)
        .size(10)
        .sortBy(CompanySort.COMPANY_NAME)
        .sortDirection(SortDirection.ASC)
        .build();
    String email = "hello@example.com";

    Page<Company> pages = new PageImpl<>(List.of());
    PageRequest pageable = PageRequest.of(0, 10);
    when(companyRepository.findAll(pageable))
        .thenReturn(pages);

    //when
    Page<CompanyOutputDto> companies = companyService.getCompanyList(dto);

    //then
    assertEquals(0, companies.getSize());
    assertEquals(0, companies.getTotalElements());
  }

  @Test
  @DisplayName("Choose company - Success")
  void choose_company() {
    //given
    Company company = Company.builder()
        .id(1L)
        .name("Emotion Taco Gangnam branch")
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .location(LocationUtil.createPoint(127.123456, 37.123456))
        .enterKey("company123")
        .build();

    ChooseCompanyDto dto = new ChooseCompanyDto("company123");

    //when
    when(companyRepository.findById(1L))
        .thenReturn(Optional.of(company));

    //then
    companyService.chooseCompany(1L, dto);
  }

  @Test
  @DisplayName("Choose company - Fail(Company not found)")
  void choose_company_fail_no_company() {
    //given
    Member member = Member.builder()
        .id(1L)
        .email("hello@example.com")
        .role(Role.VIEWER)
        .build();
    ChooseCompanyDto dto = new ChooseCompanyDto("company123");

    //when
    when(companyRepository.findById(1L))
        .thenReturn(Optional.empty());

    //then
    assertThrows(CompanyNotFoundException.class,
        () -> companyService.chooseCompany(1L, dto));
  }
}