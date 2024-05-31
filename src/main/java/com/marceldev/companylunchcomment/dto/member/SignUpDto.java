package com.marceldev.companylunchcomment.dto.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpDto {

  @NotNull
  @Email
  @Schema(example = "hello@company.com")
  private String email;

  @NotNull
  @Schema(description = "비밀번호는 8~50자. 영문자, 숫자 포함", example = "secretpw12")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,50}$")
  private String password;

  @NotNull
  @Schema(example = "이영수")
  private String name;

  @NotNull
  @Schema(example = "123123")
  private String verificationCode;

  // Not getting from client request.
  @JsonIgnore
  private final LocalDateTime now = LocalDateTime.now();
}
