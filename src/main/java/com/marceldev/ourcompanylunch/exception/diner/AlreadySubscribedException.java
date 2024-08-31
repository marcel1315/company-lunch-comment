package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class AlreadySubscribedException extends CustomException {

  public AlreadySubscribedException() {
    super("Already subscribed.");
  }
}
