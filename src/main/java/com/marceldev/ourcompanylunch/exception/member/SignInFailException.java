package com.marceldev.ourcompanylunch.exception.member;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class SignInFailException extends CustomException {

  public SignInFailException(Throwable cause) {
    super("Sign in failed.", cause);
  }
}
