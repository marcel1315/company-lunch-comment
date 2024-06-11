package com.marceldev.companylunchcomment.type;

import lombok.Getter;

@Getter
public enum CommentsSort {
  CREATED_AT("createdAt");

  private final String field;

  CommentsSort(String field) {
    this.field = field;
  }
}
