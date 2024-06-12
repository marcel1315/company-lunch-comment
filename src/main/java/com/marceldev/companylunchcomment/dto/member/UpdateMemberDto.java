package com.marceldev.companylunchcomment.dto.member;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateMemberDto {

  @NotNull
  @Size(min = 1, max = 20)
  private String name;
}
