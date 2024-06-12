package com.marceldev.companylunchcomment.dto.diner;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DinerOutputDto {

  private Long id;
  private String name;
  private String link;
  private Double latitude;
  private Double longitude;
  private LinkedHashSet<String> tags;
  private long commentsCount;
  private int distanceInMeter;

  public DinerOutputDto(Long id, String name, String link, Point location,
      Set<String> tags, long commentsCount, double distanceInMeter) {
    this.id = id;
    this.name = name;
    this.link = link;
    this.latitude = Optional.ofNullable(location).map(Point::getX).orElse(null);
    this.longitude = Optional.ofNullable(location).map(Point::getY).orElse(null);
    this.tags = new LinkedHashSet<>(tags);
    this.commentsCount = commentsCount;
    this.distanceInMeter = (int) (distanceInMeter);
  }
}
