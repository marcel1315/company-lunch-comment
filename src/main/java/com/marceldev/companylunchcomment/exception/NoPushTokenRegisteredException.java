package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class NoPushTokenRegisteredException extends CustomException {

  public NoPushTokenRegisteredException() {
    super(HttpStatus.BAD_REQUEST, "No token is registered for the member");
  }
}
