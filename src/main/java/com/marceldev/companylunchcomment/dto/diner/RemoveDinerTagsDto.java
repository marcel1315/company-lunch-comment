package com.marceldev.companylunchcomment.dto.diner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class RemoveDinerTagsDto {

  @NotNull
  @Schema(example = "[\"멕시코\"]")
  private List<String> tags;
}
