package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class SameCompanyNameExistException extends CustomException {

  public SameCompanyNameExistException() {
    super(HttpStatus.BAD_REQUEST, "Same company name exists in same domain");
  }
}
