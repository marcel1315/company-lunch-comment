package com.marceldev.companylunchcomment.util;

import com.marceldev.companylunchcomment.exception.InvalidEmailFormatException;

public class ExtractDomainUtil {

  public static String from(String email) {
    String[] split = email.split("@");
    if (split.length != 2 || split[0].isEmpty() || split[1].isEmpty()) {
      throw new InvalidEmailFormatException(email);
    }
    return split[1];
  }
}
