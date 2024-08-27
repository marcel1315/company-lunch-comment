package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class ImageUploadFailException extends CustomException {

  public ImageUploadFailException(String key) {
    super(key);
  }
}
