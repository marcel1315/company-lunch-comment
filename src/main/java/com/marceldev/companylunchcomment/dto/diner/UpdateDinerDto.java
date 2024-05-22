package com.marceldev.companylunchcomment.dto.diner;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDinerDto {

  @Schema(example = "https://naver.me/FeOCTkYP")
  private String link;

  @Schema(example = "37.4989021")
  private String latitude;

  @Schema(example = "127.0276099")
  private String longitude;
}
