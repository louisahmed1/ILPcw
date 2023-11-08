package org.example;

import uk.ac.ed.inf.ilp.data.LngLat;

/**
 * Handler class for determining if a point lies within a geographical area.
 */
public class PointInAreaHandler {

    /**
     * Check if point q lies on segment pr.
     *
     * @param p Start point of segment
     * @param q Point to check
     * @param r End point of segment
     * @return true if point lies on segment, false otherwise
     */
    private static boolean onSegment(LngLat p, LngLat q, LngLat r) {
        return q.lng() <= Math.max(p.lng(), r.lng()) && q.lng() >= Math.min(p.lng(), r.lng()) &&
                q.lat() <= Math.max(p.lat(), r.lat()) && q.lat() >= Math.min(p.lat(), r.lat());
    }

    /**
     * Determine the orientation of the points (p, q, r).
     * If the orientation is collinear, returns 0.
     * If it is clockwise, returns 1.
     * If it is counterclockwise, returns 2.
     *
     * @param p First point
     * @param q Second point
     * @param r Third point
     * @return orientation of the points (p, q, r)
     */
    private static int orientation(LngLat p, LngLat q, LngLat r) {
        double epsilon = 1e-10;  // Adjust the epsilon value as needed
        double val = (q.lat() - p.lat()) * (r.lng() - q.lng()) - (q.lng() - p.lng()) * (r.lat() - q.lat());

        if (Math.abs(val) < epsilon) return 0;  // Collinear
        return (val > 0) ? 1 : 2;  // Clockwise or counterclockwise
    }

    /**
     * Main method to check if a point lies inside a given polygon.
     * The function uses the ray-casting algorithm to determine the result.
     *
     * @param point   The point to check
     * @param polygon An array representing the vertices of the polygon
     * @return true if the point lies inside the polygon, false otherwise
     */
    public static boolean isPointInsidePolygon(LngLat point, LngLat[] polygon) {
        int i, j;
        boolean result = false;

        for (i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            // Check if point is on a vertex or on a side of the polygon
            if ((point.lng() == polygon[i].lng() && point.lat() == polygon[i].lat()) ||
                    (orientation(polygon[i], point, polygon[j]) == 0 && onSegment(polygon[i], point, polygon[j]))) {
                return true;
            }

            // Check if the ray intersects with an edge of the polygon
            if ((polygon[i].lat() > point.lat()) != (polygon[j].lat() > point.lat()) &&
                    (point.lng() < (polygon[j].lng() - polygon[i].lng()) * (point.lat() - polygon[i].lat()) / (polygon[j].lat()-polygon[i].lat()) + polygon[i].lng())) {
                result = !result;
            }
        }

        return result;
    }

    private static boolean doLinesIntersect(LngLat p1, LngLat q1, LngLat p2, LngLat q2) {
        // Find the four orientations needed for the general and special cases
        int o1 = orientation(p1, q1, p2);
        int o2 = orientation(p1, q1, q2);
        int o3 = orientation(p2, q2, p1);
        int o4 = orientation(p2, q2, q1);

        // General case
        if (o1 != o2 && o3 != o4) {
            return true;
        }

        // Special Cases
        // p1, q1 and p2 are colinear and p2 lies on segment p1q1
        if (o1 == 0 && onSegment(p1, p2, q1)) return true;
        // p1, q1 and q2 are colinear and q2 lies on segment p1q1
        if (o2 == 0 && onSegment(p1, q2, q1)) return true;
        // p2, q2 and p1 are colinear and p1 lies on segment p2q2
        if (o3 == 0 && onSegment(p2, p1, q2)) return true;
        // p2, q2 and q1 are colinear and q1 lies on segment p2q2
        if (o4 == 0 && onSegment(p2, q1, q2)) return true;

        return false; // Doesn't fall in any of the above cases
    }

    public static boolean doesLineIntersectPolygon(LngLat point, LngLat current, LngLat[] polygon) {
        // Check if the line intersects with any of the edges of the polygon
        for (int i = 0; i < polygon.length; i++) {
            LngLat nextPoint = polygon[(i + 1) % polygon.length];
            if (doLinesIntersect(point, current, polygon[i], nextPoint)) {
                return true;
            }
        }
        return false;
    }
}



