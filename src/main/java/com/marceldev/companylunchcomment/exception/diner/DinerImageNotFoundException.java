package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class DinerImageNotFoundException extends CustomException {

  public DinerImageNotFoundException(long id) {
    super("id tried: " + id);
  }
}
