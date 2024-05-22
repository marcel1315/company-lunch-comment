package com.marceldev.companylunchcomment.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateDinerDto {

  private String link;
  private String latitude;
  private String longitude;
}
