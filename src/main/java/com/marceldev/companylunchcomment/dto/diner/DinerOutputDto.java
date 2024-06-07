package com.marceldev.companylunchcomment.dto.diner;

import com.marceldev.companylunchcomment.entity.Diner;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DinerOutputDto {

  private Long id;
  private String name;
  private String link;
  private String latitude;
  private String longitude;
  private LinkedHashSet<String> tags;

  public DinerOutputDto(Long id, String name, String link, String latitude, String longitude,
      Set<String> tags) {
    this.id = id;
    this.name = name;
    this.link = link;
    this.latitude = latitude;
    this.longitude = longitude;
    this.tags = new LinkedHashSet<>(tags);
  }

  public static DinerOutputDto of(Diner diner) {
    return DinerOutputDto.builder()
        .id(diner.getId())
        .name(diner.getName())
        .link(diner.getLink())
        .latitude(diner.getLatitude())
        .longitude(diner.getLongitude())
        .tags(diner.getTags())
        .build();
  }
}
