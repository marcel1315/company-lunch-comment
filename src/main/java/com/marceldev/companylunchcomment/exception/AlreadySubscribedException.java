package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class AlreadySubscribedException extends CustomException {

  public AlreadySubscribedException() {
    super(HttpStatus.BAD_REQUEST, "Already subscribed.");
  }
}
