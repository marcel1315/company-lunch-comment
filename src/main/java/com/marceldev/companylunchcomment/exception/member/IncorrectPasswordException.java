package com.marceldev.companylunchcomment.exception.member;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class IncorrectPasswordException extends CustomException {

  public IncorrectPasswordException() {
    super("Incorrect password");
  }
}
