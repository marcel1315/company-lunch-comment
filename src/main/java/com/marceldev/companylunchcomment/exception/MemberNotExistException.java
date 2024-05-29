package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class MemberNotExistException extends CustomException {

  public MemberNotExistException() {
    super(HttpStatus.BAD_REQUEST, "Member doesn't exist.");
  }
}
