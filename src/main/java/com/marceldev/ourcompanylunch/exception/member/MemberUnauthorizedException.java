package com.marceldev.ourcompanylunch.exception.member;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class MemberUnauthorizedException extends CustomException {

  public MemberUnauthorizedException() {
    super("Member doesn't have authorization.");
  }
}
