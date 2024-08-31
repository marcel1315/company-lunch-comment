package com.marceldev.ourcompanylunch.exception.member;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class InvalidEmailFormatException extends CustomException {

  public InvalidEmailFormatException(String email) {
    super(String.format("Invalid email format: %s", email));
  }
}
