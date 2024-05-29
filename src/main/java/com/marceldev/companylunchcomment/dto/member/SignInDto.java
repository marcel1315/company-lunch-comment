package com.marceldev.companylunchcomment.dto.member;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignInDto {

  @NotNull
  private String email;

  @NotNull
  private String password;
}
