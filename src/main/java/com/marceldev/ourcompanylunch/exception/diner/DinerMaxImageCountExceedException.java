package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class DinerMaxImageCountExceedException extends CustomException {

  public DinerMaxImageCountExceedException() {
    super("Diner max image count is exceeded");
  }
}
