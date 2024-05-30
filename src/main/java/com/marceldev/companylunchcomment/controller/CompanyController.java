package com.marceldev.companylunchcomment.controller;

import com.marceldev.companylunchcomment.dto.company.CreateCompanyDto;
import com.marceldev.companylunchcomment.response.CustomResponse;
import com.marceldev.companylunchcomment.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Company", description = "회사 관련")
public class CompanyController {

  private final CompanyService companyService;

  @Operation(
      summary = "회사 등록",
      description = "사용자는 회사를 등록할 수 있다. 가입한 이메일 도메인으로 회사가 등록된다.<br>"
          + "회사 등록시 이름, 주소, 위도, 경도를 입력한다.<br>"
          + "같은 회사라도 여러 주소가 있을 수 있으므로, 같은 도메인의 회사 등록은 여러 곳이 가능하다."
  )
  @PostMapping("/company")
  public CustomResponse<?> createCompany(
      @Validated @RequestBody CreateCompanyDto createCompanyDto,
      Authentication auth
  ) {
    companyService.createCompany(createCompanyDto, auth.getName());
    return CustomResponse.success();
  }
}
