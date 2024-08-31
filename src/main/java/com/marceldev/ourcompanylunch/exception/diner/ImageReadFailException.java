package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class ImageReadFailException extends CustomException {

  public ImageReadFailException() {
    super("Can't read image from the file.");
  }
}
