package com.marceldev.companylunchcomment.exception.member;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class MemberNotExistException extends CustomException {

  public MemberNotExistException() {
    super("Member doesn't exist.");
  }
}
