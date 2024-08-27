package com.marceldev.companylunchcomment.exception.member;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class MemberUnauthorizedException extends CustomException {

  public MemberUnauthorizedException() {
    super("Member doesn't have authorization.");
  }
}
