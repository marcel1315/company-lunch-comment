package com.marceldev.ourcompanylunch.type;

import lombok.Getter;

@Getter
public enum CompanySort {
  COMPANY_NAME("name");

  private final String field;

  CompanySort(String field) {
    this.field = field;
  }
}
