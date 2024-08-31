package com.marceldev.ourcompanylunch.exception.member;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class VerificationCodeNotFoundException extends CustomException {

  public VerificationCodeNotFoundException() {
    super("Verification code doesn't exist.");
  }
}
