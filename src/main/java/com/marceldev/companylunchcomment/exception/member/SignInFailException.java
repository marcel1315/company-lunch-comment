package com.marceldev.companylunchcomment.exception.member;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class SignInFailException extends CustomException {

  public SignInFailException(Throwable cause) {
    super("Sign in failed.", cause);
  }
}
