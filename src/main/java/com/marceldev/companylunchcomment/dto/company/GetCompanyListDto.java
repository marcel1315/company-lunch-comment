package com.marceldev.companylunchcomment.dto.company;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marceldev.companylunchcomment.type.CompanySort;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@Builder
public class GetCompanyListDto {

  @PositiveOrZero
  @Schema(example = "0", description = "페이지 번호는 0부터 시작")
  private int page;

  @Positive
  @Schema(example = "10")
  private int pageSize;

  @NotNull
  @Schema(example = "COMPANY_NAME_ASC", allowableValues = {
      "COMPANY_NAME_ASC",
      "COMPANY_NAME_DESC"
  })
  private CompanySort companySort;

  @JsonIgnore
  public Sort getSort() {
    return Sort.by(
        Sort.Direction.valueOf(getCompanySort().getDirection()),
        getCompanySort().getField()
    );
  }
}
