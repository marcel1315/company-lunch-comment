package com.marceldev.ourcompanylunch.dto.reply;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateReplyDto {

  @NotNull
  @Schema(example = "I'll try next time")
  private String content;

  public UpdateReplyDto(@JsonProperty("content") String content) {
    this.content = content;
  }
}
