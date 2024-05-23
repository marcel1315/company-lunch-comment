package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class ImageDeleteFail extends CustomException {

  public ImageDeleteFail(String key) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, key);
  }
}
