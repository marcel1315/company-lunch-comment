package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class FailToEnqueueNotificationsException extends CustomException {

  public FailToEnqueueNotificationsException() {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "Fail to enqueue notifications");
  }
}
