package com.marceldev.ourcompanylunch.dto.diner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class RemoveDinerTagsDto {

  @NotNull
  @Schema(example = "[\"Mexico\"]")
  private List<String> tags;
}
