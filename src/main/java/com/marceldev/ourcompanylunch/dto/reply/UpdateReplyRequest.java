package com.marceldev.ourcompanylunch.dto.reply;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateReplyRequest {

  @NotNull
  @Schema(example = "I'll try next time")
  private String content;

  @Builder
  private UpdateReplyRequest(@JsonProperty("content") String content) {
    this.content = content;
  }

  public static UpdateReplyRequest create(String content) {
    return UpdateReplyRequest.builder()
        .content(content)
        .build();
  }
}
