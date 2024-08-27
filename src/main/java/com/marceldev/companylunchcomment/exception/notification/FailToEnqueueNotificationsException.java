package com.marceldev.companylunchcomment.exception.notification;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class FailToEnqueueNotificationsException extends CustomException {

  public FailToEnqueueNotificationsException() {
    super("Fail to enqueue notifications");
  }
}
