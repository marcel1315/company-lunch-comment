package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class DinerNotFoundException extends CustomException {

  public DinerNotFoundException(long id) {
    super("id tried: " + id);
  }
}
