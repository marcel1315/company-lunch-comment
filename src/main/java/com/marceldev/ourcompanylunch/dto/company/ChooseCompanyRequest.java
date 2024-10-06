package com.marceldev.ourcompanylunch.dto.company;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChooseCompanyRequest {

  @Schema
  private String enterKey;
}
