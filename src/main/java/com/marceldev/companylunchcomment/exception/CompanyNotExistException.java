package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class CompanyNotExistException extends CustomException {

  public CompanyNotExistException() {
    super(HttpStatus.BAD_REQUEST, "Company dosen't exist");
  }
}
