package com.marceldev.companylunchcomment.dto.diner;

import com.marceldev.companylunchcomment.type.DinerSort;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetDinerListDto {

  // private long companyId;

  @NotNull
  @Schema(example = "1")
  private int page;

  @NotNull
  @Schema(example = "10")
  private int pageSize;

  @NotNull
  @Schema(example = "DINER_NAME_ASC", allowableValues = {
      "DINER_NAME_ASC",
      "DINER_NAME_DESC"
  })
  private DinerSort dinerSort;
}
