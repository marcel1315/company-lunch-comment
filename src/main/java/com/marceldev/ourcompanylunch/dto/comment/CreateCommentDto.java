package com.marceldev.ourcompanylunch.dto.comment;

import com.marceldev.ourcompanylunch.type.ShareStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

public class CreateCommentDto {

  @Data
  @Builder
  public static class Request {

    @NotNull
    @Schema(example = "It's delicious")
    private String content;

    @NotNull
    @Schema(example = "COMPANY", allowableValues = {"COMPANY", "ME"})
    private ShareStatus shareStatus;
  }

  @Data
  @Builder
  public static class Response {

    private final long id;
  }
}