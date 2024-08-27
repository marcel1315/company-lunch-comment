package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class ImageReadFailException extends CustomException {

  public ImageReadFailException() {
    super("Can't read image from the file.");
  }
}
