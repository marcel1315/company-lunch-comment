package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class FailToEnqueueNotifications extends CustomException {

  public FailToEnqueueNotifications() {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "Fail to enqueue notifications");
  }
}
