package com.marceldev.companylunchcomment.dto.diner;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marceldev.companylunchcomment.type.DinerSort;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@Builder
public class GetDinerListDto {

  private long companyId;

  @Positive
  @Schema(example = "1")
  private int page;

  @Positive
  @Schema(example = "10")
  private int pageSize;

  @NotNull
  @Schema(example = "DINER_NAME_ASC", allowableValues = {
      "DINER_NAME_ASC",
      "DINER_NAME_DESC"
  })
  private DinerSort dinerSort;

  // Client 에서 받는 입력은 1번부터 시작하지만, DB에 호출할 때는 0번부터 시작해야 함
  public int getPage() {
    return page - 1;
  }

  @JsonIgnore
  public Sort getSort() {
    return Sort.by(
        Sort.Direction.valueOf(getDinerSort().getDirection()),
        getDinerSort().getField()
    );
  }
}
