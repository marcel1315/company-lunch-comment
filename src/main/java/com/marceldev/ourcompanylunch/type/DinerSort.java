package com.marceldev.ourcompanylunch.type;

import lombok.Getter;

@Getter
public enum DinerSort {
  DINER_NAME("name"),
  COMMENTS_COUNT("commentsCount"),
  DISTANCE("distance");

  private final String field;

  DinerSort(String field) {
    this.field = field;
  }
}
