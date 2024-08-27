package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class ImageDeleteFailException extends CustomException {

  public ImageDeleteFailException(String key) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, key);
  }
}
