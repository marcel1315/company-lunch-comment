package com.marceldev.companylunchcomment.dto.company;

import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.util.LocationUtil;
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
  private double latitude;

  @NotNull
  @Schema(example = "126.9780")
  private double longitude;

  public Company toEntityWithDomain(String domain) {
    return Company.builder()
        .name(name)
        .address(address)
        .domain(domain)
        .location(LocationUtil.createPoint(longitude, latitude))
        .build();
  }
}
