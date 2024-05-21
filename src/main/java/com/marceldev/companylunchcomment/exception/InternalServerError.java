package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class InternalServerError extends CustomException {

  public InternalServerError() {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
  }
}
