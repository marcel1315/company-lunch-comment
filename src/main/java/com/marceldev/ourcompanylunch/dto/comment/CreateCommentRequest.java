package com.marceldev.ourcompanylunch.dto.comment;

import com.marceldev.ourcompanylunch.type.ShareStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateCommentRequest {

  @NotNull
  @Schema(example = "It's delicious")
  private String content;

  @NotNull
  @Schema(example = "COMPANY", allowableValues = {"COMPANY", "ME"})
  private ShareStatus shareStatus;

  @Builder
  private CreateCommentRequest(String content, ShareStatus shareStatus) {
    this.content = content;
    this.shareStatus = shareStatus;
  }

  public static CreateCommentRequest create(String content, ShareStatus shareStatus) {
    return CreateCommentRequest.builder()
        .content(content)
        .shareStatus(shareStatus)
        .build();
  }
}
