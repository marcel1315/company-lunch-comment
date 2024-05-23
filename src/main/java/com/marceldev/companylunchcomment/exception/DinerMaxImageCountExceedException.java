package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class DinerMaxImageCountExceedException extends CustomException {

  public DinerMaxImageCountExceedException() {
    super(HttpStatus.BAD_REQUEST, "Diner max image count is exceeded");
  }
}
