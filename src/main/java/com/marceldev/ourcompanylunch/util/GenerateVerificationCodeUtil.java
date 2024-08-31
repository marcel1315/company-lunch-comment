package com.marceldev.ourcompanylunch.util;

import java.util.Random;

public class GenerateVerificationCodeUtil {

  public static String generate(int length) {
    StringBuilder code = new StringBuilder();
    new Random().ints(length, 0, 10)
        .forEach(code::append);

    return code.toString();
  }
}
