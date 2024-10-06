package com.marceldev.ourcompanylunch.dto.company;

import com.marceldev.ourcompanylunch.type.CompanySort;
import com.marceldev.ourcompanylunch.type.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetCompanyListRequest {

  @Schema
  @PositiveOrZero
  private int page;

  @Schema(example = "10")
  @Positive
  private int size;

  @NotNull
  @Schema(example = "COMPANY_NAME", allowableValues = {
      "COMPANY_NAME"
  })
  private CompanySort sortBy;

  @NotNull
  @Schema(example = "ASC", allowableValues = {"ASC", "DESC"})
  private SortDirection sortDirection;
}
