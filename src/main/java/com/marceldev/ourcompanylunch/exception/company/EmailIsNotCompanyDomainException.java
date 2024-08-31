package com.marceldev.ourcompanylunch.exception.company;

import com.marceldev.ourcompanylunch.exception.common.CustomException;

public class EmailIsNotCompanyDomainException extends CustomException {

  public EmailIsNotCompanyDomainException(String domain) {
    super(
        String.format(
            "The email shouldn't be from email providers(e.g., gmail.com, naver.com). %s",
            domain
        )
    );
  }
}
