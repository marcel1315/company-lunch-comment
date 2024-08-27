package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class ImageUploadFailException extends CustomException {

  public ImageUploadFailException(String key) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, key);
  }
}
