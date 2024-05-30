package com.marceldev.companylunchcomment.exception;

import org.springframework.http.HttpStatus;

public class EmailIsNotCompanyDomain extends CustomException {

  public EmailIsNotCompanyDomain(String domain) {
    super(HttpStatus.BAD_REQUEST,
        String.format("The email shouldn't be from email providers(e.g., gmail.com, naver.com). %s",
            domain)
    );
  }
}
