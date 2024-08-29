package com.marceldev.companylunchcomment.exception.company;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class SameCompanyNameExistException extends CustomException {

  public SameCompanyNameExistException() {
    super("Same company name exists");
  }
}
