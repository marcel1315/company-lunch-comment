package com.marceldev.companylunchcomment.dto.company;

import com.marceldev.companylunchcomment.entity.Company;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompanyOutputDto {

  private Long id;
  private String name;
  private String address;
  private String domain;
  private String latitude;
  private String longitude;

  public static CompanyOutputDto of(Company company) {
    return CompanyOutputDto.builder()
        .id(company.getId())
        .name(company.getName())
        .address(company.getAddress())
        .domain(company.getDomain())
        .latitude(company.getLatitude())
        .longitude(company.getLongitude())
        .build();
  }
}
