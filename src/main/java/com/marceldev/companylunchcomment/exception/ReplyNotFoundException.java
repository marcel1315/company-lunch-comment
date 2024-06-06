package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class ReplyNotFoundException extends CustomException {

  public ReplyNotFoundException() {
    super(HttpStatus.BAD_REQUEST, "The reply doesn't exist");
  }
}
