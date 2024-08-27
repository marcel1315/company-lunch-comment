package com.marceldev.companylunchcomment.exception.company;

import com.marceldev.companylunchcomment.exception.common.CustomException;

public class CompanyNotExistException extends CustomException {

  public CompanyNotExistException() {
    super("Company dosen't exist");
  }
}
