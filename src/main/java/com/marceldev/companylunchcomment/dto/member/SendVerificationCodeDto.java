package com.marceldev.companylunchcomment.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendVerificationCodeDto {

  @NotNull
  @Email
  @Schema(example = "hello@company.com")
  private String email;
}
