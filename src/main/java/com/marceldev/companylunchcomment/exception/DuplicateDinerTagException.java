package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class DuplicateDinerTagException extends CustomException {

  public DuplicateDinerTagException(String tag) {
    super(HttpStatus.BAD_REQUEST, tag);
  }
}
