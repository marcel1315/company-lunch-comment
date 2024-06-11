package com.marceldev.companylunchcomment.dto.company;

import com.marceldev.companylunchcomment.type.CompanySort;
import com.marceldev.companylunchcomment.type.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetCompanyListDto {

  @NotNull
  @Schema(example = "COMPANY_NAME", allowableValues = {
      "COMPANY_NAME"
  })
  private CompanySort sortBy;

  @NotNull
  @Schema(example = "ASC", allowableValues = {"ASC", "DESC"})
  private SortDirection sortDirection;
}
