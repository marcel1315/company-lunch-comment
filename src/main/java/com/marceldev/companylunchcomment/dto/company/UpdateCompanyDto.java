package com.marceldev.companylunchcomment.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateCompanyDto {

  @NotNull
  @Schema(example = "123123")
  private String verificationCode;

  @Schema(example = "서울특별시 강남구 역삼동 123-1", requiredMode = RequiredMode.NOT_REQUIRED)
  private String address;

  @Schema(example = "37.281811322", requiredMode = RequiredMode.NOT_REQUIRED)
  private String latitude;

  @Schema(example = "127.202021111", requiredMode = RequiredMode.NOT_REQUIRED)
  private String longitude;
}
