package com.marceldev.companylunchcomment.exception.comment;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class CommentNotFoundException extends CustomException {

  public CommentNotFoundException() {
    super("The comment doesn't exist");
  }
}
