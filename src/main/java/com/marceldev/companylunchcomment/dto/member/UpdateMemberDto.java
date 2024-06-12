package com.marceldev.companylunchcomment.dto.member;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateMemberDto {

  @NotNull
  @Size(min = 1, max = 20)
  private String name;
}
