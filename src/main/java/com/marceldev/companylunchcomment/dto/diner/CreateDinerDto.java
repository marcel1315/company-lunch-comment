package com.marceldev.companylunchcomment.dto.diner;

import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.util.LocationUtil;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDinerDto {

  @NotNull
  @Schema(example = "감성타코")
  private String name;

  @NotNull
  @Schema(example = "https://naver.me/FeOCTkYP", requiredMode = RequiredMode.NOT_REQUIRED)
  private String link;

  @NotNull
  @Schema(example = "37.4989021")
  private double latitude;

  @NotNull
  @Schema(example = "127.0276099")
  private double longitude;

  @NotNull
  @Schema(example = "[\"멕시코\", \"감성\"]")
  private LinkedHashSet<String> tags;

  public Diner toEntity() {
    return Diner.builder()
        .name(name)
        .link(link)
        .location(LocationUtil.createPoint(longitude, latitude))
        .tags(tags)
        .build();
  }
}
