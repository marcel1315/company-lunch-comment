package com.marceldev.ourcompanylunch.dto.reply;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateReplyDto {

  @NotNull
  @Schema(example = "맛있어요")
  private String content;

  public CreateReplyDto(@JsonProperty("content") String content) {
    this.content = content;
  }
}
