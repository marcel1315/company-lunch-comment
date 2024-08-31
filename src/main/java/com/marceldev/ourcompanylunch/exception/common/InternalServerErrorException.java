package com.marceldev.ourcompanylunch.exception.common;

public class InternalServerErrorException extends CustomException {

  public InternalServerErrorException() {
    super("Internal Server Error");
  }

  public InternalServerErrorException(String message) {
    super(message);
  }
}
