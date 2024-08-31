package com.marceldev.ourcompanylunch.type;

import lombok.Getter;

@Getter
public enum CommentSort {
  CREATED_AT("createdAt");

  private final String field;

  CommentSort(String field) {
    this.field = field;
  }
}
