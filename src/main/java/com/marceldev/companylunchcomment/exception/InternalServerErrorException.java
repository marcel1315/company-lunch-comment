package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class InternalServerErrorException extends CustomException {

  public InternalServerErrorException() {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error");
  }

  public InternalServerErrorException(String message) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message);
  }
}
