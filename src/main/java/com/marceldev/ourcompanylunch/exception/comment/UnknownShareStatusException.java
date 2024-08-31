package com.marceldev.ourcompanylunch.exception.comment;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class UnknownShareStatusException extends CustomException {

  public UnknownShareStatusException() {
    super("Unknown share status");
  }
}
