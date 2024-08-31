package com.marceldev.ourcompanylunch.dto.diner;

import com.marceldev.ourcompanylunch.entity.Diner;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Data
@Builder
public class DinerDetailOutputDto {

  private Long id;
  private String name;
  private String link;
  private Double latitude;
  private Double longitude;
  private LinkedHashSet<String> tags;
  private List<String> thumbnailUrls;
  private List<String> imageUrls;
  private long commentCount;
  private Integer distanceInMeter;

  public static DinerDetailOutputDto of(Diner diner, List<String> thumbnailUrls,
      List<String> imageUrls, Integer distance) {
    return DinerDetailOutputDto.builder()
        .id(diner.getId())
        .name(diner.getName())
        .link(diner.getLink())
        .latitude(Optional.ofNullable(diner.getLocation()).map(Point::getX).orElse(null))
        .longitude(Optional.ofNullable(diner.getLocation()).map(Point::getY).orElse(null))
        .tags(diner.getTags())
        .thumbnailUrls(thumbnailUrls)
        .imageUrls(imageUrls)
        .commentCount(diner.getComments().size())
        .distanceInMeter(distance)
        .build();
  }
}
