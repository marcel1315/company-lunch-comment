package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class ImageUploadFailException extends CustomException {

  public ImageUploadFailException(String key) {
    super(key);
  }
}
