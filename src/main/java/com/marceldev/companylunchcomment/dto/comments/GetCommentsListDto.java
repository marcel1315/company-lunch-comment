package com.marceldev.companylunchcomment.dto.comments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marceldev.companylunchcomment.type.CommentsSort;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@Builder
public class GetCommentsListDto {

  @PositiveOrZero
  @Schema(example = "0", description = "페이지번호는 0부터 시작")
  private int page;

  @Positive
  @Schema(example = "10")
  private int pageSize;

  @NotNull
  @Schema(example = "CREATED_AT_DESC", allowableValues = {
      "CREATED_AT_ASC",
      "CREATED_AT_DESC"
  })
  private CommentsSort commentsSort;

  @Schema(example = "맛있는")
  private String keyword;

  @Schema(example = "김영수")
  private String commentedBy;

  @JsonIgnore
  public Sort getSort() {
    return Sort.by(
        Sort.Direction.valueOf(getCommentsSort().getDirection()),
        getCommentsSort().getField()
    );
  }
}
