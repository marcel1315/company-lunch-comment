package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.dto.company.CompanyOutputDto;
import com.marceldev.companylunchcomment.dto.company.CreateCompanyDto;
import com.marceldev.companylunchcomment.dto.company.GetCompanyListDto;
import com.marceldev.companylunchcomment.dto.company.UpdateCompanyDto;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.Verification;
import com.marceldev.companylunchcomment.exception.CompanyNotExistException;
import com.marceldev.companylunchcomment.exception.InvalidEmailFormatException;
import com.marceldev.companylunchcomment.exception.SameCompanyNameExist;
import com.marceldev.companylunchcomment.exception.VerificationCodeNotFound;
import com.marceldev.companylunchcomment.repository.CompanyRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import com.marceldev.companylunchcomment.repository.VerificationRepository;
import com.marceldev.companylunchcomment.type.CompanySort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

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

  @Test
  @DisplayName("회사 생성 - 성공")
  void create_company() {
    //given
    CreateCompanyDto dto = CreateCompanyDto.builder()
        .name("좋은회사")
        .address("서울시 강남구 역삼동 123-456")
        .latitude("37.123456")
        .longitude("127.123456")
        .build();
    when(companyRepository.existsByDomainAndName(any(), any()))
        .thenReturn(false);

    //when
    companyService.createCompany(dto, "hello@example.com");
    ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);

    //then
    verify(companyRepository).save(captor.capture());
    assertEquals("좋은회사", captor.getValue().getName());
    assertEquals("서울시 강남구 역삼동 123-456", captor.getValue().getAddress());
    assertEquals("37.123456", captor.getValue().getLatitude());
    assertEquals("127.123456", captor.getValue().getLongitude());
    assertEquals("example.com", captor.getValue().getDomain());
  }

  @Test
  @DisplayName("회사 생성 - 실패(같은 회사 이름이 존재, 같은 이메일 도메인 안에)")
  void create_company_fail_same_email_domain() {
    //given
    CreateCompanyDto dto = CreateCompanyDto.builder()
        .name("좋은회사")
        .address("서울시 강남구 역삼동 123-456")
        .latitude("37.123456")
        .longitude("127.123456")
        .build();
    when(companyRepository.existsByDomainAndName(any(), any()))
        .thenReturn(true);

    //when
    //then
    assertThrows(SameCompanyNameExist.class,
        () -> companyService.createCompany(dto, "hello@example.com"));
  }

  @Test
  @DisplayName("회사 생성 - 실패(잘못된 이메일 주소)")
  void create_company_fail_invalid_email() {
    //given
    CreateCompanyDto dto = CreateCompanyDto.builder()
        .name("좋은회사")
        .address("서울시 강남구 역삼동 123-456")
        .latitude("37.123456")
        .longitude("127.123456")
        .build();

    //when
    //then
    assertThrows(InvalidEmailFormatException.class,
        () -> companyService.createCompany(dto, "hello")); // hello@example.com 이 들어가야 함
  }

  @Test
  @DisplayName("회사 정보 수정 - 성공")
  void update_company() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("서울시 강남구 역삼동 123-456")
        .latitude("37.123456")
        .longitude("127.123456")
        .verificationCode("123456")
        .build();
    String email = "hello@example.com";
    when(memberRepository.findByEmailAndCompanyId(email, 1L))
        .thenReturn(Optional.of(Member.builder()
            .company(Company.builder().id(1L).build())
            .build()
        ));
    when(verificationRepository.findByEmail(email))
        .thenReturn(Optional.of(Verification.builder()
            .code("123456")
            .expirationAt(LocalDateTime.now().plusMinutes(2))
            .build()
        ));

    //when
    companyService.updateCompany(1L, dto, email);

    //then
    verify(companyRepository).save(any());
    verify(verificationRepository).delete(any());
  }

  @Test
  @DisplayName("회사 정보 수정 - 실패(해당 회사가 존재하지 않음)")
  void update_company_fail_no_company() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("서울시 강남구 역삼동 123-456")
        .latitude("37.123456")
        .longitude("127.123456")
        .verificationCode("123456")
        .build();
    String email = "hello@example.com";
    when(memberRepository.findByEmailAndCompanyId(email, 1L))
        .thenReturn(Optional.empty());

    //when
    //then
    assertThrows(CompanyNotExistException.class,
        () -> companyService.updateCompany(1L, dto, email));
  }

  @Test
  @DisplayName("회사 정보 수정 - 실패(인증번호가 맞지 않음)")
  void update_company_fail_verification_code_incorrect() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("서울시 강남구 역삼동 123-456")
        .latitude("37.123456")
        .longitude("127.123456")
        .verificationCode("123456")
        .build();
    String email = "hello@example.com";
    when(memberRepository.findByEmailAndCompanyId(email, 1L))
        .thenReturn(Optional.of(Member.builder()
            .company(Company.builder().id(1L).build())
            .build()
        ));
    when(verificationRepository.findByEmail(email))
        .thenReturn(Optional.of(Verification.builder()
            .code("111111")
            .expirationAt(LocalDateTime.now().plusMinutes(2))
            .build()
        ));

    //when
    //then
    assertThrows(VerificationCodeNotFound.class,
        () -> companyService.updateCompany(1L, dto, email));
  }

  @Test
  @DisplayName("회사 정보 수정 - 실패(인증번호의 만료시간이 현재시간보다 먼저인 경우)")
  void update_company_fail_verification_code_expired() {
    //given
    UpdateCompanyDto dto = UpdateCompanyDto.builder()
        .address("서울시 강남구 역삼동 123-456")
        .latitude("37.123456")
        .longitude("127.123456")
        .verificationCode("123456")
        .build();
    String email = "hello@example.com";
    when(memberRepository.findByEmailAndCompanyId(email, 1L))
        .thenReturn(Optional.of(Member.builder()
            .company(Company.builder().id(1L).build())
            .build()
        ));
    when(verificationRepository.findByEmail(email))
        .thenReturn(Optional.of(Verification.builder()
            .code("123456")
            .expirationAt(LocalDateTime.now().minusMinutes(2))
            .build()
        ));

    //when
    //then
    assertThrows(VerificationCodeNotFound.class,
        () -> companyService.updateCompany(1L, dto, email));
  }

  @Test
  @DisplayName("회사 목록 불러오기 - 성공")
  void get_company_list() {
    //given
    GetCompanyListDto dto = GetCompanyListDto.builder()
        .page(1)
        .pageSize(10)
        .companySort(CompanySort.COMPANY_NAME_ASC)
        .build();
    String email = "hello@example.com";
    Company company1 = Company.builder()
        .id(1L)
        .name("감정타코 강남점")
        .address("서울시 강남구 역삼동 123-456")
        .latitude("37.123456")
        .longitude("127.123456")
        .build();
    Company company2 = Company.builder()
        .id(2L)
        .name("감정타코 신사점")
        .address("서울시 강남구 신사동 123-456")
        .latitude("37.123457")
        .longitude("127.123457")
        .build();

    Page<Company> pages = new PageImpl<>(List.of(company1, company2));

    when(companyRepository.findByDomain(any(), any()))
        .thenReturn(pages);

    //when
    Page<CompanyOutputDto> companies = companyService.getCompanyList(dto, email);

    //then
    assertEquals(2, companies.getSize());
  }

  @Test
  @DisplayName("회사 목록 불러오기 - 성공(빈 페이지)")
  void get_company_list_empty_page() {
    //given
    GetCompanyListDto dto = GetCompanyListDto.builder()
        .page(1)
        .pageSize(10)
        .companySort(CompanySort.COMPANY_NAME_ASC)
        .build();
    String email = "hello@example.com";

    Page<Company> pages = new PageImpl<>(List.of());
    when(companyRepository.findByDomain(any(), any()))
        .thenReturn(pages);

    //when
    Page<CompanyOutputDto> companies = companyService.getCompanyList(dto, email);

    //then
    assertEquals(0, companies.getSize());
    assertEquals(0, companies.getTotalElements());
  }
}