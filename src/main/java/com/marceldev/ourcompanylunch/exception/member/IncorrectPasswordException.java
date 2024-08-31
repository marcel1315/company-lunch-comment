package com.marceldev.ourcompanylunch.exception.member;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class IncorrectPasswordException extends CustomException {

  public IncorrectPasswordException() {
    super("Incorrect password");
  }
}
