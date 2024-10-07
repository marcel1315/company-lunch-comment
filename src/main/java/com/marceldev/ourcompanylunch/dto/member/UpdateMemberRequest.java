package com.marceldev.ourcompanylunch.dto.member;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateMemberRequest {

  @NotNull
  @Size(min = 1, max = 20)
  private final String name;

  @Builder
  private UpdateMemberRequest(String name) {
    this.name = name;
  }

  public static UpdateMemberRequest create(String name) {
    return UpdateMemberRequest.builder()
        .name(name)
        .build();
  }
}
