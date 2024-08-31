package com.marceldev.ourcompanylunch.exception.member;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class MemberNotFoundException extends CustomException {

  public MemberNotFoundException() {
    super("Member doesn't exist.");
  }
}
