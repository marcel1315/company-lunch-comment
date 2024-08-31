package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class DinerImageNotFoundException extends CustomException {

  public DinerImageNotFoundException(long id) {
    super("id tried: " + id);
  }
}
