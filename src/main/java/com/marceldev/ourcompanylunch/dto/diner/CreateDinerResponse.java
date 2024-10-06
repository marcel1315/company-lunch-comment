package com.marceldev.ourcompanylunch.dto.diner;

import com.marceldev.ourcompanylunch.entity.Diner;
import java.util.LinkedHashSet;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateDinerResponse {

  private final Long id;
  private final String name;
  private final String link;
  private final double latitude;
  private final double longitude;
  private final LinkedHashSet<String> tags;

  @Builder
  private CreateDinerResponse(Long id, String name, String link, double latitude, double longitude,
      LinkedHashSet<String> tags) {
    this.id = id;
    this.name = name;
    this.link = link;
    this.latitude = latitude;
    this.longitude = longitude;
    this.tags = tags;
  }

  public static CreateDinerResponse of(Diner diner) {
    return CreateDinerResponse.builder()
        .id(diner.getId())
        .name(diner.getName())
        .link(diner.getLink())
        .latitude(diner.getLocation().getX())
        .longitude(diner.getLocation().getY())
        .tags(diner.getTags())
        .build();
  }
}
