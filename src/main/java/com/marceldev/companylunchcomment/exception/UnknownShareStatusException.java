package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class UnknownShareStatusException extends CustomException {

  public UnknownShareStatusException() {
    super(HttpStatus.BAD_REQUEST, "Unknown share status");
  }
}
