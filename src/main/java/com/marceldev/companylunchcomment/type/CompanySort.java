package com.marceldev.companylunchcomment.type;

import lombok.Getter;

@Getter
public enum CompanySort {
  COMPANY_NAME_ASC("name", "ASC"),
  COMPANY_NAME_DESC("name", "DESC");

  private final String field;
  private final String direction;

  CompanySort(String field, String direction) {
    this.field = field;
    this.direction = direction;
  }
}
