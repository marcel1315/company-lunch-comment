package com.marceldev.companylunchcomment.dto.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marceldev.companylunchcomment.util.LocationUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Data
@Builder
public class UpdateCompanyDto {

  @NotNull
  @Schema(example = "123123")
  private String verificationCode;

  @NotNull
  @Schema(example = "company123")
  private String enterKey;

  @NotNull
  @Schema(example = "서울특별시 강남구 역삼동 123-1", requiredMode = RequiredMode.NOT_REQUIRED)
  private String address;

  @NotNull
  @Schema(example = "37.281811322", requiredMode = RequiredMode.NOT_REQUIRED)
  private double latitude;

  @NotNull
  @Schema(example = "127.202021111", requiredMode = RequiredMode.NOT_REQUIRED)
  private double longitude;

  @JsonIgnore
  private final LocalDateTime now = LocalDateTime.now();

  @JsonIgnore
  public Point getLocation() {
    return LocationUtil.createPoint(longitude, latitude);
  }
}
