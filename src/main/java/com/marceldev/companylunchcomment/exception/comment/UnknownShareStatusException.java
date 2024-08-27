package com.marceldev.companylunchcomment.exception.comment;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class UnknownShareStatusException extends CustomException {

  public UnknownShareStatusException() {
    super("Unknown share status");
  }
}
