package com.marceldev.companylunchcomment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class AddDinerTagsDto {

  @NotNull
  private List<String> tags;
}
