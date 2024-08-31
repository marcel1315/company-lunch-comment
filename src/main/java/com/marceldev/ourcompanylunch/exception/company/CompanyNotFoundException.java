package com.marceldev.ourcompanylunch.exception.company;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class CompanyNotFoundException extends CustomException {

  public CompanyNotFoundException() {
    super("Company doesn't exist");
  }
}
