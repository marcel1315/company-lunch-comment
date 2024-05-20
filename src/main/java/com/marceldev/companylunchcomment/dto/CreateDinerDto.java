package com.marceldev.companylunchcomment.dto;

import com.marceldev.companylunchcomment.entity.Diner;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDinerDto {

  private String name;
  private String link;
  private String latitude;
  private String longitude;
  private List<String> tags;

  public Diner toEntity() {
    return Diner.builder()
        .name(name)
        .link(link)
        .latitude(latitude)
        .longitude(longitude)
        .tags(tags)
        .build();
  }
}
