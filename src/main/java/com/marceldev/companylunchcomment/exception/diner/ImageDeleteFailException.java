package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class ImageDeleteFailException extends CustomException {

  public ImageDeleteFailException(String key) {
    super(key);
  }
}
