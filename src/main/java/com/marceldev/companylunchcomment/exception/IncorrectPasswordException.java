package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class IncorrectPasswordException extends CustomException{

  public IncorrectPasswordException() {
    super(HttpStatus.BAD_REQUEST, "Incorrect password");
  }
}
