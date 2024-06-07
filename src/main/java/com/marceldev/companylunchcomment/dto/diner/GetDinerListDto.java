package com.marceldev.companylunchcomment.dto.diner;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marceldev.companylunchcomment.type.DinerSort;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@Builder
public class GetDinerListDto {

  @Positive
  private long companyId;

  @NotNull
  @Schema(example = "DINER_NAME_ASC", allowableValues = {
      "DINER_NAME_ASC",
      "DINER_NAME_DESC"
  })
  private DinerSort dinerSort;

  @JsonIgnore
  public Sort getSort() {
    return Sort.by(
        Sort.Direction.valueOf(getDinerSort().getDirection()),
        getDinerSort().getField()
    );
  }
}
