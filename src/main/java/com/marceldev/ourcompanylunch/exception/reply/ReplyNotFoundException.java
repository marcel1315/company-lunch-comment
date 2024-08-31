package com.marceldev.ourcompanylunch.exception.reply;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class ReplyNotFoundException extends CustomException {

  public ReplyNotFoundException() {
    super("The reply doesn't exist");
  }
}
