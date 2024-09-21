package com.marceldev.ourcompanylunch.dto.reply;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public class CreateReplyDto {

  @Data
  @Builder
  public static class Request {

    @NotNull
    @Schema(example = "I'll try")
    private String content;

    public Request(@JsonProperty("content") String content) {
      this.content = content;
    }
  }

  @Data
  @Builder
  public static class Response {

    private final long id;
  }
}