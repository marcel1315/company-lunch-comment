package com.marceldev.companylunchcomment.exception.diner;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class AlreadySubscribedException extends CustomException {

  public AlreadySubscribedException() {
    super("Already subscribed.");
  }
}
