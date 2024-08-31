package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class DuplicateDinerTagException extends CustomException {

  public DuplicateDinerTagException(String tag) {
    super(tag);
  }
}
