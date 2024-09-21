package com.marceldev.ourcompanylunch.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInDto {

  @NotNull
  @Email
  @Schema(example = "hello@company.com")
  private String email;

  @NotNull
  @Schema(example = "secretpw12")
  private String password;
}
