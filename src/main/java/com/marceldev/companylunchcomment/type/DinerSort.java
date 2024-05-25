package com.marceldev.companylunchcomment.type;

import lombok.Getter;

@Getter
public enum DinerSort {
  DINER_NAME_ASC("name", "ASC"),
  DINER_NAME_DESC("name", "DESC"),
  DISTANCE_FROM_COMPANY_ASC("distance_from_company", "ASC"),
  DISTANCE_FROM_COMPANY_DESC("distance_from_company", "DESC"),
  COMMENTS_COUNT_ASC("comments_count", "ASC"),
  COMMENTS_COUNT_DESC("comments_count", "DESC");

  private final String field;
  private final String direction;

  DinerSort(String field, String direction) {
    this.field = field;
    this.direction = direction;
  }
}
