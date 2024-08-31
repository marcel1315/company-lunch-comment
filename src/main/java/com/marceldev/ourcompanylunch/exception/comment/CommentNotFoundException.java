package com.marceldev.ourcompanylunch.exception.comment;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class CommentNotFoundException extends CustomException {

  public CommentNotFoundException() {
    super("The comment doesn't exist");
  }
}
