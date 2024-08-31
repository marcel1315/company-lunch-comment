package com.marceldev.ourcompanylunch.exception.member;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class AlreadyExistMemberException extends CustomException {

  public AlreadyExistMemberException() {
    super("The member already exists");
  }
}
