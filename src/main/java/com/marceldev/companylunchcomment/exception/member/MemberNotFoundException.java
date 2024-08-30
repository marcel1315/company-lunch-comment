package com.marceldev.companylunchcomment.exception.member;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class MemberNotFoundException extends CustomException {

  public MemberNotFoundException() {
    super("Member doesn't exist.");
  }
}
