package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class ImageUploadFail extends CustomException {

  public ImageUploadFail(String key) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, key);
  }
}
