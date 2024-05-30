package com.marceldev.companylunchcomment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.companylunchcomment.dto.company.CreateCompanyDto;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.exception.InvalidEmailFormatException;
import com.marceldev.companylunchcomment.exception.SameCompanyNameExist;
import com.marceldev.companylunchcomment.repository.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

  @Mock
  private CompanyRepository companyRepository;

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
}