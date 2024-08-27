package com.marceldev.companylunchcomment.exception.member;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class VerificationCodeNotFoundException extends CustomException {

  public VerificationCodeNotFoundException() {
    super("Verification code doesn't exist.");
  }
}
