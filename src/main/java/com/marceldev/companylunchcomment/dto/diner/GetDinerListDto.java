package com.marceldev.companylunchcomment.dto.diner;

import com.marceldev.companylunchcomment.type.DinerSort;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetDinerListDto {

  @Schema(example = "칼국수")
  private String keyword;

  @NotNull
  @Schema(example = "DINER_NAME_ASC", allowableValues = {
      "DINER_NAME_ASC",
      "DINER_NAME_DESC"
  })
  private DinerSort dinerSort;
}
