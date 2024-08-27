package com.marceldev.companylunchcomment.controller;

import com.marceldev.companylunchcomment.dto.company.CompanyOutputDto;
import com.marceldev.companylunchcomment.dto.company.CreateCompanyDto;
import com.marceldev.companylunchcomment.dto.company.GetCompanyListDto;
import com.marceldev.companylunchcomment.dto.company.UpdateCompanyDto;
import com.marceldev.companylunchcomment.dto.error.ErrorResponse;
import com.marceldev.companylunchcomment.dto.member.SendVerificationCodeDto;
import com.marceldev.companylunchcomment.exception.company.SameCompanyNameExistException;
import com.marceldev.companylunchcomment.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "2 Company", description = "회사 관련")
public class CompanyController {

  private final CompanyService companyService;

  @Operation(
      summary = "회사 등록",
      description = "사용자는 회사를 등록할 수 있다. 가입한 이메일 도메인으로 회사가 등록된다.<br>"
          + "회사 등록시 이름, 주소, 위도, 경도를 입력한다.<br>"
          + "같은 회사라도 여러 주소가 있을 수 있으므로, 같은 도메인의 회사 등록은 여러 곳이 가능하다."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description = "errorCode: 2001 - 같은 이름의 회사가 존재")
  })
  @PostMapping("/companies")
  public ResponseEntity<Void> createCompany(
      @Validated @RequestBody CreateCompanyDto createCompanyDto
  ) {
    companyService.createCompany(createCompanyDto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "이메일 인증번호 발송",
      description = "해당 이메일로 인증번호를 발송한다."
  )
  @PostMapping("/companies/send-verification-code")
  public ResponseEntity<Void> sendVerificationCode(
      @Validated @RequestBody SendVerificationCodeDto dto
  ) {
    companyService.sendVerificationCode(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "회사 정보 수정",
      description = "사용자는 회사 정보를 수정할 수 있다. 주소, 위도, 경도를 수정할 수 있다.<br>"
          + "회사 정보 수정을 위해 이메일을 통한 번호 인증을 해야한다."
  )
  @PutMapping("/companies/{id}")
  public ResponseEntity<Void> updateCompany(
      @PathVariable long id,
      @Validated @RequestBody UpdateCompanyDto updateCompanyDto
  ) {
    companyService.updateCompany(id, updateCompanyDto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "회사 목록 조회",
      description = "사용자는 가입한 이메일 도메인으로 등록된 회사들을 조회할 수 있다."
  )
  @GetMapping("/companies")
  public ResponseEntity<Page<CompanyOutputDto>> getCompanyList(
      @Validated @ModelAttribute GetCompanyListDto getCompanyListDto
  ) {
    Pageable pageable = PageRequest.of(
        getCompanyListDto.getPage(),
        getCompanyListDto.getSize()
    );
    Page<CompanyOutputDto> companies = companyService.getCompanyList(getCompanyListDto, pageable);
    return ResponseEntity.ok(companies);
  }

  @Operation(
      summary = "회사 선택",
      description = "사용자는 회사를 선택할 수 있다.<br>"
          + "같은 회사라도 여러 지점이 있을 수 있다. 자신이 점심 먹는 회사를 선택한다."
  )
  @PutMapping("/companies/{id}/choose")
  public ResponseEntity<Void> chooseCompany(
      @PathVariable long id
  ) {
    companyService.chooseCompany(id);
    return ResponseEntity.ok().build();
  }

  @ExceptionHandler(SameCompanyNameExistException.class)
  public ResponseEntity<ErrorResponse> handle(SameCompanyNameExistException e) {
    return ErrorResponse.badRequest(2001, e.getMessage());
  }
}
