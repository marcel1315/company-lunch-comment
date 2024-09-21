package com.marceldev.ourcompanylunch.controller;

import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyDto;
import com.marceldev.ourcompanylunch.dto.company.CompanyOutputDto;
import com.marceldev.ourcompanylunch.dto.company.CreateCompanyDto;
import com.marceldev.ourcompanylunch.dto.company.CreateCompanyDto.Response;
import com.marceldev.ourcompanylunch.dto.company.GetCompanyListDto;
import com.marceldev.ourcompanylunch.dto.company.UpdateCompanyDto;
import com.marceldev.ourcompanylunch.dto.error.ErrorResponse;
import com.marceldev.ourcompanylunch.dto.member.SendVerificationCodeDto;
import com.marceldev.ourcompanylunch.exception.company.CompanyEnterKeyNotMatchException;
import com.marceldev.ourcompanylunch.exception.company.SameCompanyNameExistException;
import com.marceldev.ourcompanylunch.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
@Tag(name = "2 Company")
public class CompanyController {

  private final CompanyService companyService;

  @Operation(
      summary = "Register a company",
      description = "A member can register a company.<br>"
          + "Company enterKey is the key that others can join this company with."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description = "errorCode: 2001 - A company with same name exists.")
  })
  @PostMapping("/companies")
  public ResponseEntity<CreateCompanyDto.Response> createCompany(
      @Validated @RequestBody CreateCompanyDto.Request createCompanyDto
  ) {
    Response response = companyService.createCompany(createCompanyDto);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Send email a verification code"
  )
  @PostMapping("/companies/send-verification-code")
  public ResponseEntity<Void> sendVerificationCode(
      @Validated @RequestBody SendVerificationCodeDto dto
  ) {
    companyService.sendVerificationCode(dto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Update company information",
      description = "A member can change address, latitude and longitude.<br>"
          + "Require email verification code to update company information."
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
      summary = "Get company list"
  )
  @GetMapping("/companies")
  public ResponseEntity<Page<CompanyOutputDto>> getCompanyList(
      @Validated @ModelAttribute GetCompanyListDto getCompanyListDto
  ) {
    Page<CompanyOutputDto> companies = companyService.getCompanyList(getCompanyListDto);
    return ResponseEntity.ok(companies);
  }

  @Operation(
      summary = "Join a company",
      description = "A member choose a company.<br>"
          + "If a company has many branches, choose by the branch where eat lunch."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK"),
      @ApiResponse(responseCode = "400", description = "errorCode: 2002 - Incorrect enter key")
  })
  @PutMapping("/companies/{id}/choose")
  public ResponseEntity<Void> chooseCompany(
      @PathVariable long id,
      @Validated @RequestBody ChooseCompanyDto chooseCompanyDto
  ) {
    companyService.chooseCompany(id, chooseCompanyDto);
    return ResponseEntity.ok().build();
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handle(SameCompanyNameExistException e) {
    return ErrorResponse.badRequest(2001, e.getMessage());
  }

  @ExceptionHandler
  public ResponseEntity<ErrorResponse> handle(CompanyEnterKeyNotMatchException e) {
    return ErrorResponse.badRequest(2002, e.getMessage());
  }
}
