package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class NoPushTokenRegistered extends CustomException {

  public NoPushTokenRegistered() {
    super(HttpStatus.BAD_REQUEST, "No token is registered for the member");
  }
}
