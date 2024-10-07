package com.marceldev.ourcompanylunch.dto.diner;

import com.marceldev.ourcompanylunch.entity.DinerImage;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AddDinerImageResponse {

  private final Long id;
  private final int orders;

  @Builder
  private AddDinerImageResponse(Long id, int orders) {
    this.id = id;
    this.orders = orders;
  }

  public static AddDinerImageResponse of(DinerImage dinerImage) {
    return AddDinerImageResponse.builder()
        .id(dinerImage.getId())
        .orders(dinerImage.getOrders())
        .build();
  }
}
