package com.marceldev.companylunchcomment.dto;

import com.marceldev.companylunchcomment.entity.Diner;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDinerDto {

  @NotNull
  private String name;
  private String link;
  private String latitude;
  private String longitude;
  private List<String> tags;

  public Diner toEntity() {
    // tags 에 아무것도 들어오지 않을 때, DB에 빈 배열이라도 넣어야 함
    if (tags == null) {
      tags = new ArrayList<>();
    }

    return Diner.builder()
        .name(name)
        .link(link)
        .latitude(latitude)
        .longitude(longitude)
        .tags(tags)
        .build();
  }
}
