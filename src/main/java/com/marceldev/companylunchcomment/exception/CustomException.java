package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {

  final private HttpStatus httpStatus;

  public CustomException(HttpStatus httpStatus, String message) {
    super(message);
    this.httpStatus = httpStatus;
  }

  public int getHttpStatusCode() {
    return httpStatus.value();
  }

  public String getHttpStatusType() {
    return httpStatus.getReasonPhrase();
  }
}
