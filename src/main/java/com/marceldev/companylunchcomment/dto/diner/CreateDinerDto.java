package com.marceldev.companylunchcomment.dto.diner;

import com.marceldev.companylunchcomment.entity.Diner;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDinerDto {

  @NotNull
  @Schema(example = "감성타코")
  private String name;

  @Schema(example = "https://naver.me/FeOCTkYP", requiredMode = RequiredMode.NOT_REQUIRED)
  private String link;

  @Schema(example = "37.4989021")
  private String latitude;

  @Schema(example = "127.0276099")
  private String longitude;

  @Schema(example = "[\"멕시코\", \"감성\"]")
  private List<String> tags;

  public Diner toEntity() {
    // tags 에 아무것도 들어오지 않을 때, DB에 빈 배열이라도 넣어야 함
    if (tags == null) {
      tags = new ArrayList<>();
    }

    return Diner.builder()
        .name(name)
        .link(link)
        .latitude(latitude)
        .longitude(longitude)
        .tags(tags)
        .build();
  }
}
