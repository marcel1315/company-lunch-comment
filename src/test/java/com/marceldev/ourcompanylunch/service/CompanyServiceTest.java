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
@DisplayName("회사 서비스")
class CompanyServiceTest {

  @Mock
  private CompanyRepository companyRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private VerificationRepository verificationRepository;

  @InjectMocks
  private CompanyService companyService;

  // 테스트에서 목으로 사용될 company. diner를 가져올 때, member가 속한 company의 diner가 아니면 가져올 수 없음
  private final Company company1 = Company.builder()
      .id(1L)
      .name("좋은회사")
      .address("서울특별시 강남구 강남대로 200")
      .location(LocationUtil.createPoint(127.123123, 37.123123))
      .enterKey("company123")
      .build();

  // 테스트에서 목으로 사용될 member. diner를 가져올 때, 적절한 member가 아니면 가져올 수 없음
  private final Member member1 = Member.builder()
      .id(1L)
      .email("kys@example.com")
      .name("김영수")
      .role(Role.VIEWER)
      .password("somehashedvalue")
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
  @DisplayName("회사 생성 - 성공")
  void create_company() {
    //given
    CreateCompanyDto dto = CreateCompanyDto.builder()
        .name("좋은회사")
        .address("서울시 강남구 역삼동 123-456")
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
    assertEquals("좋은회사", captor.getValue().getName());
    assertEquals("서울시 강남구 역삼동 123-456", captor.getValue().getAddress());
    assertEquals(LocationUtil.createPoint(127.123456, 37.123456), captor.getValue().getLocation());
    assertEquals("company123", captor.getValue().getEnterKey());
  }

  @Test
  @DisplayName("회사 생성 - 실패(같은 회사 이름이 존재, 같은 이메일 도메인 안에)")
  void create_company_fail_same_email_domain() {
    //given
    CreateCompanyDto dto = CreateCompanyDto.builder()
        .name("좋은회사")
        .address("서울시 강남구 역삼동 123-456")
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
  @DisplayName("회사 정보 수정 - 성공")
  void update_company() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("서울시 강남구 역삼동 123-456")
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
  @DisplayName("회사 정보 수정 - 실패(해당 회사가 존재하지 않음)")
  void update_company_fail_no_company() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("서울시 강남구 역삼동 123-456")
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
  @DisplayName("회사 정보 수정 - 실패(인증번호가 맞지 않음)")
  void update_company_fail_verification_code_incorrect() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("서울시 강남구 역삼동 123-456")
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
  @DisplayName("회사 정보 수정 - 실패(인증번호의 만료시간이 현재시간보다 먼저인 경우)")
  void update_company_fail_verification_code_expired() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("서울시 강남구 역삼동 123-456")
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
  @DisplayName("회사 목록 불러오기 - 성공")
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
        .name("감정타코 강남점")
        .address("서울시 강남구 역삼동 123-456")
        .location(LocationUtil.createPoint(127.123456, 37.123456))
        .build();
    Company company2 = Company.builder()
        .id(2L)
        .name("감정타코 신사점")
        .address("서울시 강남구 신사동 123-456")
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
  @DisplayName("회사 목록 불러오기 - 성공(빈 페이지)")
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
  @DisplayName("회사 선택하기 - 성공")
  void choose_company() {
    //given
    Company company = Company.builder()
        .id(1L)
        .name("감정타코 강남점")
        .address("서울시 강남구 역삼동 123-456")
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
  @DisplayName("회사 선택하기 - 실패(회사가 없음)")
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