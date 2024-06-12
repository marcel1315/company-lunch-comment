package com.marceldev.companylunchcomment.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class LocationUtil {

  // SRID(Spatial Reference System ID)는 Point에 넣는 데이터를 longitude와 latitude로 인식하도록 함. GPS에서 사용하는 규격.
  private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(),
      4326);

  public static Point createPoint(double longitude, double latitude) {
    return geometryFactory.createPoint(new Coordinate(longitude, latitude));
  }
}
