package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class CommentNotFoundException extends CustomException {

  public CommentNotFoundException() {
    super(HttpStatus.NOT_FOUND, "The comment doesn't exist");
  }
}
