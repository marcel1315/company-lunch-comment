package com.marceldev.ourcompanylunch.exception.diner;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class ImageWithNoExtensionException extends CustomException {

  public ImageWithNoExtensionException() {
    super("No extension in filename.");
  }
}
