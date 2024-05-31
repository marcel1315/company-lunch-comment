package com.marceldev.companylunchcomment.util;

import java.util.Random;

public class VerificationCodeGenerator {

  public static String generate(int length) {
    StringBuilder code = new StringBuilder();
    new Random().ints(length, 0, 10)
        .forEach(code::append);

    return code.toString();
  }
}
