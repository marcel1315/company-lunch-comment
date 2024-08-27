package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class ImageWithNoExtensionException extends CustomException {

  public ImageWithNoExtensionException() {
    super(HttpStatus.BAD_REQUEST, "No extension in filename.");
  }
}
