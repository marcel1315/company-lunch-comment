package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class ImageUploadFail extends CustomException {

  public ImageUploadFail(String originalFilename) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, originalFilename);
  }
}
