package com.marceldev.companylunchcomment.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CustomResponse<T> extends ResponseEntity<T> {

  private CustomResponse(T body, HttpStatus status) {
    super(body, status);
  }

  public static CustomResponse<?> success() {
    return new CustomResponse<>(null, HttpStatus.OK);
  }

  public static CustomResponse<?> success(Object body) {
    return new CustomResponse<>(body, HttpStatus.OK);
  }
}
