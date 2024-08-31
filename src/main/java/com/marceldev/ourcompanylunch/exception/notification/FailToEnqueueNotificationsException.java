package com.marceldev.ourcompanylunch.exception.notification;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class FailToEnqueueNotificationsException extends CustomException {

  public FailToEnqueueNotificationsException() {
    super("Fail to enqueue notifications");
  }
}
