package com.marceldev.companylunchcomment.exception.reply;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class ReplyNotFoundException extends CustomException {

  public ReplyNotFoundException() {
    super("The reply doesn't exist");
  }
}
