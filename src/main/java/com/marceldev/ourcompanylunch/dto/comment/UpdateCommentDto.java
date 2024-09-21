package com.marceldev.ourcompanylunch.dto.comment;

import com.marceldev.ourcompanylunch.type.ShareStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateCommentDto {

  @NotNull
  @Schema(example = "Very kind")
  private String content;

  @NotNull
  @Schema(example = "COMPANY", allowableValues = {"COMPANY", "ME"})
  private ShareStatus shareStatus;
}
