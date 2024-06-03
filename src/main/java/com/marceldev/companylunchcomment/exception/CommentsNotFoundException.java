package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class CommentsNotFoundException extends CustomException {

  public CommentsNotFoundException() {
    super(HttpStatus.BAD_REQUEST, "The comments doesn't exist");
  }
}
