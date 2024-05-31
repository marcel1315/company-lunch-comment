package com.marceldev.companylunchcomment.dto;

import com.marceldev.companylunchcomment.type.Role;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class SignInResult {

  private final String email;
  private final Role role;

  public String getRoleString() {
    return role.toString();
  }
}
