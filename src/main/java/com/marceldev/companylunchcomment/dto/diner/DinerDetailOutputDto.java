package com.marceldev.companylunchcomment.dto.diner;

import com.marceldev.companylunchcomment.entity.Diner;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DinerDetailOutputDto {

  private Long id;
  private String name;
  private String link;
  private String latitude;
  private String longitude;
  private List<String> tags;
  private List<String> imageUrls;

  public static DinerDetailOutputDto of(Diner diner, List<String> imageUrls) {
    return DinerDetailOutputDto.builder()
        .id(diner.getId())
        .name(diner.getName())
        .link(diner.getLink())
        .latitude(diner.getLatitude())
        .longitude(diner.getLongitude())
        .tags(diner.getTags())
        .imageUrls(imageUrls)
        .build();
  }
}