package com.marceldev.companylunchcomment.dto.diner;

import com.marceldev.companylunchcomment.type.DinerSort;
import com.marceldev.companylunchcomment.type.SortDirection;
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
  @Schema(example = "DINER_NAME", allowableValues = {
      "DINER_NAME",
      "COMMENTS_COUNT",
      "DISTANCE"
  })
  private DinerSort sortBy;

  @NotNull
  @Schema(example = "ASC", allowableValues = {"ASC", "DESC"})
  private SortDirection sortDirection;
}
