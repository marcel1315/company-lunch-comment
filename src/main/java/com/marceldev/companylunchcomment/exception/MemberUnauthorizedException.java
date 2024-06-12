package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class MemberUnauthorizedException extends CustomException {

  public MemberUnauthorizedException() {
    super(HttpStatus.UNAUTHORIZED, "Member doesn't have authorization.");
  }
}
