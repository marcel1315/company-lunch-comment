package com.marceldev.companylunchcomment.type;

import lombok.Getter;

@Getter
public enum CommentsSort {
  CREATED_AT_ASC("createdAt", "ASC"),
  CREATED_AT_DESC("createdAt", "DESC");

  private final String field;
  private final String direction;

  CommentsSort(String field, String direction) {
    this.field = field;
    this.direction = direction;
  }
}
