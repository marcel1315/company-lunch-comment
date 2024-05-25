package com.marceldev.companylunchcomment.dto.diner;

import com.marceldev.companylunchcomment.entity.Diner;
import java.util.List;
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
  private List<String> tags;

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
