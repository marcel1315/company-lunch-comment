package com.marceldev.ourcompanylunch.dto.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class VerifyVerificationCodeDto {

  @NotNull
  @Email
  @Schema(example = "hello@company.com")
  private String email;

  @NotNull
  @Schema(example = "123456")
  private String code;

  @JsonIgnore // Doesn't take this from client request
  private final LocalDateTime now = LocalDateTime.now();
}
