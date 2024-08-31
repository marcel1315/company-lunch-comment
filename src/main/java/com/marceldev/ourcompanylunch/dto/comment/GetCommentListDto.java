package com.marceldev.ourcompanylunch.dto.comment;

import com.marceldev.ourcompanylunch.type.CommentSort;
import com.marceldev.ourcompanylunch.type.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetCommentListDto {

  @Schema
  @PositiveOrZero
  private int page;

  @Schema(example = "10")
  @Positive
  private int size;

  @NotNull
  @Schema(example = "CREATED_AT", allowableValues = {
      "CREATED_AT"
  })
  private CommentSort sortBy;

  @NotNull
  @Schema(example = "ASC", allowableValues = {"ASC", "DESC"})
  private SortDirection sortDirection;

  @Schema(example = "맛있는")
  private String keyword;

  @Schema(example = "김영수")
  private String commentedBy;
}
