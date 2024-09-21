package com.marceldev.ourcompanylunch.dto.diner;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Data
@Builder
public class UpdateDinerDto {

  @NotNull
  @Schema(example = "https://link.me/FeOCTkYP")
  private String link;

  @NotNull
  @Schema(example = "37.4989021")
  private double latitude;

  @NotNull
  @Schema(example = "127.0276099")
  private double longitude;

  @JsonIgnore
  public Point getLocation() {
    return LocationUtil.createPoint(longitude, latitude);
  }
}
