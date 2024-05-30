package com.marceldev.companylunchcomment.component;

import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class VerificationCodeGenerator {

  public String generate(int length) {
    StringBuilder code = new StringBuilder();
    new Random().ints(length, 0, 10)
        .forEach(code::append);

    return code.toString();
  }
}
