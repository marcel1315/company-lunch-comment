package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class DinerSubscriptionNotFoundException extends CustomException {

  public DinerSubscriptionNotFoundException() {
    super("Diner subscription doesn't exist.");
  }
}
