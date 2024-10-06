package com.marceldev.ourcompanylunch.dto.diner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class RemoveDinerTagsRequest {

  @NotNull
  @Schema(example = "[\"Mexico\"]")
  private final List<String> tags;

  @Builder
  private RemoveDinerTagsRequest(List<String> tags) {
    this.tags = tags;
  }

  public static RemoveDinerTagsRequest create(List<String> tags) {
    return RemoveDinerTagsRequest.builder()
        .tags(tags)
        .build();
  }
}
