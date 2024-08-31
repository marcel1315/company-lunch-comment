package com.marceldev.ourcompanylunch.dto.diner;

import com.marceldev.ourcompanylunch.type.DinerSort;
import com.marceldev.ourcompanylunch.type.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetDinerListDto {

  @Schema(example = "칼국수")
  private String keyword;

  @Schema
  @PositiveOrZero
  private int page;

  @Schema(example = "10")
  @Positive
  private int size;

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
