package com.marceldev.companylunchcomment.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangePasswordDto {

  @NotNull
  @Schema(example = "hello@example.com")
  private String email;

  @NotNull
  @Schema(example = "secretpw12")
  private String oldPassword;

  @NotNull
  @Schema(example = "secretpw13")
  private String newPassword;
}
