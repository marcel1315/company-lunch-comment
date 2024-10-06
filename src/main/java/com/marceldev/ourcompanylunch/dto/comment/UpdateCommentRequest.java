package com.marceldev.ourcompanylunch.dto.comment;

import com.marceldev.ourcompanylunch.type.ShareStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateCommentRequest {

  @NotNull
  @Schema(example = "Very kind")
  private final String content;

  @NotNull
  @Schema(example = "COMPANY", allowableValues = {"COMPANY", "ME"})
  private final ShareStatus shareStatus;

  @Builder
  private UpdateCommentRequest(String content, ShareStatus shareStatus) {
    this.content = content;
    this.shareStatus = shareStatus;
  }

  public static UpdateCommentRequest create(String content, ShareStatus shareStatus) {
    return UpdateCommentRequest.builder()
        .content(content)
        .shareStatus(shareStatus)
        .build();
  }
}
