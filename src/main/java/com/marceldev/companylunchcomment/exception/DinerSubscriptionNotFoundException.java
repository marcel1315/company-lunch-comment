package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class DinerSubscriptionNotFoundException extends CustomException {

  public DinerSubscriptionNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Diner subscription doesn't exist.");
  }
}
