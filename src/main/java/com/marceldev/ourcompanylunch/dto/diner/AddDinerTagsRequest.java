package com.marceldev.ourcompanylunch.dto.diner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AddDinerTagsRequest {

  @NotNull
  @Schema(example = "[\"Quick\"]")
  private List<String> tags;

  @Builder
  public AddDinerTagsRequest(List<String> tags) {
    this.tags = tags;
  }

  public static AddDinerTagsRequest create(List<String> tags) {
    return AddDinerTagsRequest.builder()
        .tags(tags)
        .build();
  }
}
