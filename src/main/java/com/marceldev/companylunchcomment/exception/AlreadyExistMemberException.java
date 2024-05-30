package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class AlreadyExistMemberException extends CustomException {

  public AlreadyExistMemberException() {
    super(HttpStatus.BAD_REQUEST, "The member already exists");
  }
}
