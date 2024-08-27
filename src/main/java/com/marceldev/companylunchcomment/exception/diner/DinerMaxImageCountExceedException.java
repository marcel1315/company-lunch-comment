package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class DinerMaxImageCountExceedException extends CustomException {

  public DinerMaxImageCountExceedException() {
    super("Diner max image count is exceeded");
  }
}
