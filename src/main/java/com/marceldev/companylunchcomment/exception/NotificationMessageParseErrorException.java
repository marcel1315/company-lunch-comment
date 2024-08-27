package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class NotificationMessageParseErrorException extends CustomException {

  public NotificationMessageParseErrorException() {
    super(HttpStatus.INTERNAL_SERVER_ERROR, "Notification message parse error");
  }
}
