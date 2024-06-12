package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class CompanyNotExistException extends CustomException {

  public CompanyNotExistException() {
    super(HttpStatus.NOT_FOUND, "Company dosen't exist");
  }
}
