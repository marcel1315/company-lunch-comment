package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class DinerSubscriptionNotFound extends CustomException {

  public DinerSubscriptionNotFound() {
    super(HttpStatus.NOT_FOUND, "Diner subscription doesn't exist.");
  }
}
