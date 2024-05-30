package com.marceldev.companylunchcomment.dto.company;

import com.marceldev.companylunchcomment.entity.Company;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateCompanyDto {

  @NotNull
  @Schema(example = "좋은회사")
  private String name;

  @NotNull
  @Schema(example = "서울특별시 강남구 강남대로 123")
  private String address;

  @NotNull
  @Schema(example = "37.5665")
  private String latitude;

  @NotNull
  @Schema(example = "126.9780")
  private String longitude;

  public Company toEntityWithDomain(String domain) {
    return Company.builder()
        .name(getName())
        .address(getAddress())
        .latitude(getLatitude())
        .longitude(getLongitude())
        .domain(domain)
        .build();
  }
}
