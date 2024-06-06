package com.marceldev.companylunchcomment.dto.comments;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.marceldev.companylunchcomment.type.CommentsSort;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Sort;

@Data
@Builder
public class GetCommentsListDto {

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
