package com.marceldev.companylunchcomment.dto.comment;

import com.marceldev.companylunchcomment.type.CommentSort;
import com.marceldev.companylunchcomment.type.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetCommentListDto {

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
