package com.marceldev.ourcompanylunch.dto.company;

import com.marceldev.ourcompanylunch.entity.Company;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import org.locationtech.jts.geom.Point;

@Data
@Builder
public class CompanyOutputDto {

  private Long id;
  private String name;
  private String address;
  private Double latitude;
  private Double longitude;

  public static CompanyOutputDto of(Company company) {
    return CompanyOutputDto.builder()
        .id(company.getId())
        .name(company.getName())
        .address(company.getAddress())
        .latitude(Optional.ofNullable(company.getLocation()).map(Point::getX).orElse(null))
        .longitude(Optional.ofNullable(company.getLocation()).map(Point::getY).orElse(null))
        .build();
  }
}
