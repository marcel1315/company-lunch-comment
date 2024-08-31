package com.marceldev.ourcompanylunch.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
