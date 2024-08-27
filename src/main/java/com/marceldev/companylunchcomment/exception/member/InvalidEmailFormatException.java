package com.marceldev.companylunchcomment.exception.member;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class InvalidEmailFormatException extends CustomException {

  public InvalidEmailFormatException(String email) {
    super(String.format("Invalid email format: %s", email));
  }
}
