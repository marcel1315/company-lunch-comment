package com.marceldev.ourcompanylunch.dto.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {

  private final int errorCode;
  private final String message;

  static public ResponseEntity<ErrorResponse> badRequest(int errorCode, String message) {
    ErrorResponse errorResponse = new ErrorResponse(errorCode, message);
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  static public ResponseEntity<ErrorResponse> serverError(int errorCode, String message) {
    ErrorResponse errorResponse = new ErrorResponse(errorCode, message);
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}