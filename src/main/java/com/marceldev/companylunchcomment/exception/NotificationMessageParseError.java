package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class NotificationMessageParseError extends CustomException {

  public NotificationMessageParseError() {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "Notification message parse error");
  }
}
