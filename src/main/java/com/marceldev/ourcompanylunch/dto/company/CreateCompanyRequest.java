package com.marceldev.ourcompanylunch.dto.company;


import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateCompanyRequest {

  @NotNull
  @Schema(example = "HelloCompany")
  private final String name;

  @NotNull
  @Schema(example = "123, Gangnam-daero Gangnam-gu Seoul")
  private final String address;

  @Schema(example = "company123")
  private final String enterKey;

  @NotNull
  @Schema(example = "false")
  private final Boolean enterKeyEnabled;

  @NotNull
  @Schema(example = "37.5665")
  private final double latitude;

  @NotNull
  @Schema(example = "126.9780")
  private final double longitude;

  @Builder
  private CreateCompanyRequest(String name, String address, String enterKey,
      Boolean enterKeyEnabled,
      double latitude, double longitude) {
    this.name = name;
    this.address = address;
    this.enterKey = enterKey;
    this.enterKeyEnabled = enterKeyEnabled;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public Company toEntity() {
    return Company.builder()
        .name(name)
        .address(address)
        .enterKey(enterKey)
        .enterKeyEnabled(enterKeyEnabled)
        .location(LocationUtil.createPoint(latitude, longitude))
        .build();
  }
}

