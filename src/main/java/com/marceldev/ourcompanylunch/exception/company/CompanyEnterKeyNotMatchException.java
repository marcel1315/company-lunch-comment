package com.marceldev.ourcompanylunch.exception.company;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class CompanyEnterKeyNotMatchException extends CustomException {

  public CompanyEnterKeyNotMatchException() {
    super("Company enter key is not matched.");
  }
}
