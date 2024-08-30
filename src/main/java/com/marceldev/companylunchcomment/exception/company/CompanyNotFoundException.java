package com.marceldev.companylunchcomment.exception.company;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class CompanyNotFoundException extends CustomException {

  public CompanyNotFoundException() {
    super("Company doesn't exist");
  }
}
