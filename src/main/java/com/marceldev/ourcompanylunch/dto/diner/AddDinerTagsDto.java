package com.marceldev.ourcompanylunch.dto.diner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class AddDinerTagsDto {

  @NotNull
  @Schema(example = "[\"Quick\"]")
  private List<String> tags;
}
