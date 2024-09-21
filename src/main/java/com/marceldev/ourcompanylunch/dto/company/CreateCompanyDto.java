package com.marceldev.ourcompanylunch.dto.company;

import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class CreateCompanyDto {

  @Data
  @Builder
  public static class Request {

    @NotNull
    @Schema(example = "HelloCompany")
    private String name;

    @NotNull
    @Schema(example = "123, Gangnam-daero Gangnam-gu Seoul")
    private String address;

    @Schema(example = "company123")
    private String enterKey;

    @NotNull
    @Schema(example = "false")
    private Boolean enterKeyEnabled;

    @NotNull
    @Schema(example = "37.5665")
    private double latitude;

    @NotNull
    @Schema(example = "126.9780")
    private double longitude;

    public Company toEntity() {
      return Company.builder()
          .name(name)
          .address(address)
          .enterKey(enterKey)
          .enterKeyEnabled(enterKeyEnabled)
          .location(LocationUtil.createPoint(longitude, latitude))
          .build();
    }
  }

  @Data
  @Builder
  public static class Response {
    private final long id;
  }
}
