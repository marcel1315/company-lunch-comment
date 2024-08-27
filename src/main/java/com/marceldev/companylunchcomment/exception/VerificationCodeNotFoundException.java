package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class VerificationCodeNotFoundException extends CustomException {

  public VerificationCodeNotFoundException() {
    super(HttpStatus.BAD_REQUEST, "Verification code doesn't exist.");
  }
}
