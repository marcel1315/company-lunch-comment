package com.marceldev.ourcompanylunch.dto.comment;

import com.marceldev.ourcompanylunch.type.CommentSort;
import com.marceldev.ourcompanylunch.type.SortDirection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCommentListRequest {

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

  @Schema(example = "delicious")
  private String keyword;

  @Schema(example = "Jack")
  private String commentedBy;

  @Builder
  private GetCommentListRequest(int page, int size, CommentSort sortBy, SortDirection sortDirection,
      String keyword, String commentedBy) {
    this.page = page;
    this.size = size;
    this.sortBy = sortBy;
    this.sortDirection = sortDirection;
    this.keyword = keyword;
    this.commentedBy = commentedBy;
  }

  public static GetCommentListRequest create(String keyword, String commentedBy) {
    return GetCommentListRequest.builder()
        .page(0)
        .size(20)
        .sortBy(CommentSort.CREATED_AT)
        .sortDirection(SortDirection.ASC)
        .keyword(keyword)
        .commentedBy(commentedBy)
        .build();
  }
}
