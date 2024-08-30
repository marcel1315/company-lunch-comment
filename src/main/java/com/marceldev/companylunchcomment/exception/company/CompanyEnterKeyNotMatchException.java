package com.marceldev.companylunchcomment.exception.company;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class CompanyEnterKeyNotMatchException extends CustomException {

  public CompanyEnterKeyNotMatchException() {
    super("Company enter key is not matched.");
  }
}
