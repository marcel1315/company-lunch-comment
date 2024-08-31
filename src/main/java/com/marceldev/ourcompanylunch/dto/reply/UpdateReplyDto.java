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
  @Schema(example = "댓글입니다")
  private String content;

  public UpdateReplyDto(@JsonProperty("content") String content) {
    this.content = content;
  }
}
