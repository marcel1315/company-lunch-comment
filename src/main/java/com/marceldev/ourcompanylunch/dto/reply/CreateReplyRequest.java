package com.marceldev.ourcompanylunch.dto.reply;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateReplyRequest {

  @NotNull
  @Schema(example = "I'll try")
  private String content;

  @Builder
  private CreateReplyRequest(@JsonProperty("content") String content) {
    this.content = content;
  }

  public static CreateReplyRequest create(String content) {
    return CreateReplyRequest.builder()
        .content(content)
        .build();
  }
}