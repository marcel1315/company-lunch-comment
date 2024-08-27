package com.marceldev.companylunchcomment.exception.member;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class AlreadyExistMemberException extends CustomException {

  public AlreadyExistMemberException() {
    super("The member already exists");
  }
}
