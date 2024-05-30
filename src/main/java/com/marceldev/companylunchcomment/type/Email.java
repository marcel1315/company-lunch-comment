package com.marceldev.companylunchcomment.type;

import com.marceldev.companylunchcomment.exception.InvalidEmailFormatException;
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
    if (split.length != 2 || split[0].isEmpty() || split[1].isEmpty()) {
      throw new InvalidEmailFormatException(email);
    }
    this.username = split[0];
    this.domain = split[1];
  }
}
