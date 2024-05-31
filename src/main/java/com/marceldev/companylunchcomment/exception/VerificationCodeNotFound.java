package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class VerificationCodeNotFound extends CustomException {

  public VerificationCodeNotFound() {
    super(HttpStatus.BAD_REQUEST, "Verification code doesn't exist.");
  }
}
