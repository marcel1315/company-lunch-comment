package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class ReplyNotFoundException extends CustomException {

  public ReplyNotFoundException() {
    super(HttpStatus.NOT_FOUND, "The reply doesn't exist");
  }
}
