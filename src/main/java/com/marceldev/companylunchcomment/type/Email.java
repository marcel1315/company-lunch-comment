package com.marceldev.companylunchcomment.type;

import lombok.Getter;

@Getter
public class Email {

  private final String username;
  private final String domain;

  public static Email of(String email) {
    return new Email(email);
  }

  private Email(String email) {
    String[] split = email.split("@");
    this.username = split[0];
    this.domain = split[1];
  }
}
