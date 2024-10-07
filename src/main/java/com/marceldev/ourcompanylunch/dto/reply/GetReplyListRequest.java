package com.marceldev.ourcompanylunch.dto.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GetReplyListRequest {

  @Schema
  @PositiveOrZero
  private int page;

  @Schema(example = "10")
  @Positive
  private int size;

  @Builder
  private GetReplyListRequest(int page, int size) {
    this.page = page;
    this.size = size;
  }

  public static GetReplyListRequest create() {
    return GetReplyListRequest.builder()
        .page(0)
        .size(10)
        .build();
  }
}
