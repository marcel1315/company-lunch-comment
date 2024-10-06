package com.marceldev.ourcompanylunch.util;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class LocationUtil {

  // SRID(Spatial Reference System ID) perceives the Point data as longitude and latitude. 
  // GPS use this format.
  private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(),
      4326);

  public static Point createPoint(double latitude, double longitude) {
    return geometryFactory.createPoint(new Coordinate(latitude, longitude));
  }
}
