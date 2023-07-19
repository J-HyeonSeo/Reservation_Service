package com.jhsfully.reservation.util;

public class DistanceUtil {
    private static final double RADIUS = 6371;
    private static final double TORADIAN = Math.PI / 180;

    //거리를 반환하기 위한 공식 사용.
    public static double haversine(double x1, double y1, double x2, double y2) {

        if(x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1){
            return 0;
        }

        double distance;

        double deltaLatitude = Math.abs(x1 - x2) * TORADIAN;
        double deltaLongitude = Math.abs(y1 - y2) * TORADIAN;

        double sinDeltaLat = Math.sin(deltaLatitude / 2);
        double sinDeltaLng = Math.sin(deltaLongitude / 2);
        double squareRoot = Math.sqrt(
                sinDeltaLat * sinDeltaLat +
                        Math.cos(x1 * TORADIAN) * Math.cos(x2 * TORADIAN) * sinDeltaLng * sinDeltaLng
        );

        distance = 2 * RADIUS * Math.asin(squareRoot);

        return distance;
    }

}
