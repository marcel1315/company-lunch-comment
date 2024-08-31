package com.marceldev.ourcompanylunch.exception.company;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class SameCompanyNameExistException extends CustomException {

  public SameCompanyNameExistException() {
    super("Same company name exists");
  }
}
