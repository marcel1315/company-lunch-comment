package com.marceldev.companylunchcomment.dto.comments;

import com.marceldev.companylunchcomment.type.ShareStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateCommentsDto {

  @NotNull
  @Schema(example = "친절함")
  private String content;

  @NotNull
  @Schema(example = "COMPANY", allowableValues = {"COMPANY", "ME"})
  private ShareStatus shareStatus;
}
