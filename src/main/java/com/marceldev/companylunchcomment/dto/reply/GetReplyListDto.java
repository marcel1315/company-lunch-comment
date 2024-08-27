package com.marceldev.companylunchcomment.dto.reply;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GetReplyListDto {

  @Schema
  @PositiveOrZero
  private int page;

  @Schema(example = "10")
  @Positive
  private int size;
}
