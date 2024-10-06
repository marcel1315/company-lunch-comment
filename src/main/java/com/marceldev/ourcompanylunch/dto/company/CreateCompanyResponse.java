package com.marceldev.ourcompanylunch.dto.company;

import com.marceldev.ourcompanylunch.entity.Company;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateCompanyResponse {

  private final Long id;
  private final String name;
  private final String address;
  private final String enterKey;
  private final Boolean enterKeyEnabled;
  private final double latitude;
  private final double longitude;

  @Builder
  private CreateCompanyResponse(long id, String name, String address, String enterKey,
      Boolean enterKeyEnabled,
      double latitude,
      double longitude) {
    this.id = id;
    this.name = name;
    this.address = address;
    this.enterKey = enterKey;
    this.enterKeyEnabled = enterKeyEnabled;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public static CreateCompanyResponse of(Company company) {
    return CreateCompanyResponse.builder()
        .id(company.getId())
        .name(company.getName())
        .address(company.getAddress())
        .enterKey(company.getEnterKey())
        .enterKeyEnabled(company.isEnterKeyEnabled())
        .latitude(company.getLocation().getY())
        .longitude(company.getLocation().getX())
        .build();
  }
}