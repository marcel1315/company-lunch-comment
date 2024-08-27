package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class ImageReadFailException extends CustomException {

  public ImageReadFailException() {
    super(HttpStatus.BAD_REQUEST, "Can't read image from the file.");
  }
}
