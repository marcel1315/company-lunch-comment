package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class NotMatchVerificationCode extends CustomException {

  public NotMatchVerificationCode() {
      super(HttpStatus.BAD_REQUEST, "Verification code not match");
  }
}
