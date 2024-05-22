package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class DinerNotFoundException extends CustomException {

  public DinerNotFoundException(long id) {
    super(HttpStatus.NOT_FOUND, "id tried: " + id);
  }
}
