package org.example;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.example.PointInAreaHandler.isPointInsidePolygon;

/**
 * Provides utilities for handling geographical points represented as LngLat.
 */
public class LngLatHandler implements LngLatHandling {

    private static final Logger LOGGER = Logger.getLogger(LngLatHandler.class.getName());

    public LngLatHandler() {
        // Default constructor for LngLatHandler
    }

    /**
     * Calculate the straight-line distance between two geographical points.
     *
     * @param startPosition The starting point.
     * @param endPosition   The end point.
     * @return The distance between the two points.
     */
    public double distanceTo(LngLat startPosition, LngLat endPosition) {
        double distanceLng = Math.abs(endPosition.lng() - startPosition.lng());
        double distanceLat = Math.abs(endPosition.lat() - startPosition.lat());

        return Math.hypot(distanceLat, distanceLng);
    }

    /**
     * Find the closest point on a line segment to a given point.
     *
     * @param p Point to find the closest point for.
     * @param a Start point of the line segment.
     * @param b End point of the line segment.
     * @return Closest point on the line segment to point p.
     */
    private static LngLat closestPointOnSegment(LngLat p, LngLat a, LngLat b) {
        double ap_lng = p.lng() - a.lng();
        double ap_lat = p.lat() - a.lat();
        double ab_lng = b.lng() - a.lng();
        double ab_lat = b.lat() - a.lat();

        double magnitudeAB = ab_lng * ab_lng + ab_lat * ab_lat;
        double abDotAp = ap_lng * ab_lng + ap_lat * ab_lat;
        double t = abDotAp / magnitudeAB;

        if (t < 0.0) {
            return a;
        } else if (t > 1.0) {
            return b;
        }
        return new LngLat(a.lng() + ab_lng * t, a.lat() + ab_lat * t);
    }

    /**
     * Find the nearest point on the perimeter of a region to a given start point.
     *
     * @param startPoint Starting point for the search.
     * @param area The region to search around.
     * @return The nearest point on the perimeter of the region.
     */
    public static LngLat findNearestPointOnPerimeter(LngLat startPoint, NamedRegion area) {
        LngLat nearestPoint = null;
        double minDistance = Double.MAX_VALUE;
        LngLatHandler lngLatHandler = new LngLatHandler();

        for (int i = 0; i < area.vertices().length; i++) {
            LngLat startSegment = area.vertices()[i];
            LngLat endSegment = area.vertices()[(i + 1) % area.vertices().length];  // Wrap around for the last segment

            LngLat closestPoint = closestPointOnSegment(startPoint, startSegment, endSegment);
            double distance = lngLatHandler.distanceTo(startPoint, closestPoint);

            if (distance < minDistance) {
                minDistance = distance;
                nearestPoint = closestPoint;
            }
        }

        return nearestPoint;
    }

    /**
     * Determine if two geographical points are close to each other.
     *
     * @param startPosition The first point.
     * @param otherPosition The second point.
     * @return true if the points are close, false otherwise.
     */
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) {
        return distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }

    /**
     * Determine if a geographical point lies within a defined region.
     *
     * @param position The point to check.
     * @param region   The region to check against.
     * @return true if the point is inside the region, false otherwise.
     */
    public boolean isInRegion(LngLat position, NamedRegion region) {
        return isPointInsidePolygon(position, region.vertices());
    }

    /**
     * Calculate the next position of a point when moved in a certain direction by a fixed distance.
     *
     * @param startPosition The starting position of the point.
     * @param angle         The direction in which the point should move, in degrees.
     *                      Must be a multiple of 22.5 degrees. An angle of 999 means no movement.
     * @return The new position after movement.
     */
    public LngLat nextPosition(LngLat startPosition, double angle) throws IllegalArgumentException {
        if (angle == 999) {
            return startPosition;
        }

        double sine = Math.sin(Math.toRadians(angle));
        double cosine = Math.cos(Math.toRadians(angle));

        double lngMovement = SystemConstants.DRONE_MOVE_DISTANCE * cosine;
        double latMovement = SystemConstants.DRONE_MOVE_DISTANCE * sine;

        return new LngLat(startPosition.lng() + lngMovement, startPosition.lat() + latMovement);
    }
}