package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class SameCompanyNameExist extends CustomException {

  public SameCompanyNameExist() {
    super(HttpStatus.BAD_REQUEST, "Same company name exists in same domain");
  }
}
