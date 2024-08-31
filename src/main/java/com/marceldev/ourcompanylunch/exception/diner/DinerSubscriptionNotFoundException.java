package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class DinerSubscriptionNotFoundException extends CustomException {

  public DinerSubscriptionNotFoundException() {
    super("Diner subscription doesn't exist.");
  }
}
