package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class MemberNotExistException extends CustomException {

  public MemberNotExistException() {
    super(HttpStatus.NOT_FOUND, "Member doesn't exist.");
  }
}
