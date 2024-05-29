package com.marceldev.companylunchcomment.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignInDto {

  @NotNull
  @Email
  @Schema(description = "이메일", example = "hello@company.com")
  private String email;

  @NotNull
  @Schema(example = "secretpw12")
  private String password;
}
