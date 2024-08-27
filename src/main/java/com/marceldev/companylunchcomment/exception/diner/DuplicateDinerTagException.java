package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class DuplicateDinerTagException extends CustomException {

  public DuplicateDinerTagException(String tag) {
    super(tag);
  }
}
