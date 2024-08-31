package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class ImageDeleteFailException extends CustomException {

  public ImageDeleteFailException(String key) {
    super(key);
  }
}
