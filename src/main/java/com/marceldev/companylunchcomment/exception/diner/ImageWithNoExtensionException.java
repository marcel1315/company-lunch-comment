package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class ImageWithNoExtensionException extends CustomException {

  public ImageWithNoExtensionException() {
    super("No extension in filename.");
  }
}
