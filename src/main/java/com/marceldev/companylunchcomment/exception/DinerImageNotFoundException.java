package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class DinerImageNotFoundException extends CustomException {

  public DinerImageNotFoundException(long id) {
    super(HttpStatus.NOT_FOUND, "id tried: " + id);
  }
}
