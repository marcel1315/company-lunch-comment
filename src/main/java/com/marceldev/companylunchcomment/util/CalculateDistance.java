package com.marceldev.companylunchcomment.util;

import static java.lang.Math.asin;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toRadians;

public class CalculateDistance {

  public static int calculate(double lat1, double lon1, double lat2, double lon2) {
    // Haversine Formula. 6371km은 지구의 반지름. 지구를 완전한 구형으로 가정했을 때의 계산

    // Earth's radius in kilometers
    final int r = 6371;

    // Convert degrees to radians
    double lat1Rad = toRadians(lat1);
    double lon1Rad = toRadians(lon1);
    double lat2Rad = toRadians(lat2);
    double lon2Rad = toRadians(lon2);

    // Differences in coordinates
    double deltaLat = lat2Rad - lat1Rad;
    double deltaLon = lon2Rad - lon1Rad;

    // Haversine formula
    double distance = 2 * r * asin(sqrt(
            sin(deltaLat / 2) * sin(deltaLat / 2)
                + cos(lat1Rad) * cos(lat2Rad)
                * sin(deltaLon / 2) * sin(deltaLon / 2)
        )
    );

    // Distance in meter
    return (int) (distance * 1000);
  }
}
