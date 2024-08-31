package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class DinerNotFoundException extends CustomException {

  public DinerNotFoundException(long id) {
    super("id tried: " + id);
  }
}
