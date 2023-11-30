package org.example;

import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class responsible for calculating flight paths for drone deliveries.
 */
public class Flightpath {

    private static final Logger LOGGER = Logger.getLogger(Flightpath.class.getName());
    private static final LngLat APPLETON = new LngLat(-3.186874, 55.944494);
    private static final LngLatHandler lngLatHandler = new LngLatHandler();

    /**
     * Generates the neighboring nodes for a given current node.
     * @param current Current node.
     * @param end Destination node.
     * @param noFlyZones Array of no-fly zones.
     * @param orderNo Order number for identification.
     * @return List of neighbor nodes.
     */
    private static List<Node> getNeighbors(Node current, Node end, NamedRegion[] noFlyZones, String orderNo) {
        List<Node> neighbors = new ArrayList<>();
        double[] angles = { 0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5, 180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5 };

        for (double angle : angles) {
            Node neighbor = new Node(lngLatHandler.nextPosition(current.lngLat, angle), angle, orderNo);
            double tentativeG = current.startCost + SystemConstants.DRONE_MOVE_DISTANCE;

            if (isValidPosition(neighbor, current, noFlyZones)) {
                neighbor.startCost = tentativeG;
                neighbor.endCost = lngLatHandler.distanceTo(neighbor.lngLat, end.lngLat);
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    /**
     * Validates if the given position is valid considering no-fly zones and the current position.
     * @param position Position to validate.
     * @param current Current node.
     * @param noFlyZones Array of no-fly zones.
     * @return true if valid, false otherwise.
     */
    private static boolean isValidPosition(Node position, Node current, NamedRegion[] noFlyZones) {
        for (NamedRegion zone : noFlyZones) {
            if (lngLatHandler.isInRegion(position.lngLat, zone)) return false;
            if (PointInAreaHandler.doesLineIntersectPolygon(position.lngLat, current.lngLat, zone.vertices())) return false;
        }
        return true;
    }

    /**
     * Retrieves the location of the restaurant for a given order.
     * @param order The order to find the restaurant for.
     * @param restaurants Array of available restaurants.
     * @return Location of the restaurant.
     */
    private static LngLat getOrderRestaurant(Order order, Restaurant[] restaurants) {
        for (Restaurant restaurant : restaurants) {
            if (Arrays.asList(restaurant.menu()).contains(order.getPizzasInOrder()[0])) {
                return restaurant.location();
            }
        }

        LOGGER.log(Level.SEVERE, "Error: No restaurant found for order: " + order.getOrderNo());
        System.exit(1);
        return null;
    }

    /**
     * Finds a path from start to end position avoiding no-fly zones.
     * Used to find sub-paths.
     * @param startLng Longitude of start position.
     * @param startLat Latitude of start position.
     * @param endLng Longitude of end position.
     * @param endLat Latitude of end position.
     * @param noFlyZones Array of no-fly zones.
     * @param orderNo Order number for identification.
     * @return List of nodes forming the path.
     */
    private static List<Node> findPath(double startLng, double startLat, double endLng, double endLat, NamedRegion[] noFlyZones, String orderNo) {
        //First and last nodes should hover (drop-off and collection respectively)
        Node start = new Node(new LngLat(startLng, startLat), 999, orderNo);
        Node end = new Node(new LngLat(endLng, endLat), 999, orderNo);
        List<Node> path = new ArrayList<>();

        PriorityQueue<Node> openSet = new PriorityQueue<>();

        //Hashsets used to provide O(1) time lookup
        Set<Node> closedSet = new HashSet<>();
        Set<Node> openSetLookup = new HashSet<>();

        //Set start node initial costs
        start.startCost = 0;
        start.endCost = lngLatHandler.distanceTo(start.lngLat, end.lngLat);
        openSet.add(start);
        openSetLookup.add(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            openSetLookup.remove(current);

            //Lower value than the system constant to address a bug
            if (lngLatHandler.distanceTo(current.lngLat, end.lngLat) < 0.00014999) {
                while (current != null) {
                    path.add(current);
                    current = current.parent;
                }
                Collections.reverse(path);
                return path;
            }

            closedSet.add(current);
            List<Node> neighbors = getNeighbors(current, end,  noFlyZones, orderNo);

            for (Node neighbor : neighbors) {
                if (closedSet.contains(neighbor)) continue;

                double tentativeG = current.startCost + SystemConstants.DRONE_MOVE_DISTANCE;
                if (!openSetLookup.contains(neighbor) || tentativeG < neighbor.startCost) {
                    neighbor.parent = current;
                    neighbor.startCost = tentativeG;
                    neighbor.endCost = lngLatHandler.distanceTo(neighbor.lngLat, end.lngLat);

                    if (openSetLookup.contains(neighbor)) {
                        openSet.remove(neighbor);
                    }
                    openSet.add(neighbor);
                    openSetLookup.add(neighbor);
                }
            }
        }
        return path;  // no path found
    }

    /**
     * Generates the path for an order from the starting point to the restaurant and back.
     * Combines sub-paths from the `findPath` function
     * @param noFlyZones Array of no-fly zones.
     * @param centralArea Central area of operation.
     * @param restaurantPos Position of the restaurant.
     * @param orderNo Order number for identification.
     * @return List of nodes forming the path.
     * @throws IOException If an I/O error occurs.
     */
    private static List<Node> getOrderPath(NamedRegion[] noFlyZones, NamedRegion centralArea, LngLat restaurantPos, String orderNo) throws IOException {
        //Find path to restaurant
        List<Node> toRestaurantPath = findPath(APPLETON.lng(), APPLETON.lat(), restaurantPos.lng(), restaurantPos.lat(), noFlyZones, orderNo);

        //Find the closest point on an edge of central area from drop-off point
        LngLat enterCentralPoint = LngLatHandler.findNearestPointOnPerimeter(toRestaurantPath.get(toRestaurantPath.size()-1).lngLat, centralArea);

        List<Node> path = new ArrayList<>(toRestaurantPath);

        //Finds return path for restaurant outside of central area
        if (!lngLatHandler.isInRegion(restaurantPos, centralArea)) {

            //Find path from drop-off point to the closest point on an edge of central area
            List<Node> enterCentralAreaPath = findPath(toRestaurantPath.get(toRestaurantPath.size() - 1).lng(), toRestaurantPath.get(toRestaurantPath.size() - 1).lat(), enterCentralPoint.lng(), enterCentralPoint.lat(), noFlyZones, orderNo);

            //Find path from point on the edge of central area to appleton
            List<Node> centralReturnPath = findPath(enterCentralAreaPath.get(enterCentralAreaPath.size()-1).lng(), enterCentralAreaPath.get(enterCentralAreaPath.size()-1).lat(), APPLETON.lng(), APPLETON.lat(), noFlyZones, orderNo);

            //Removes extra hover node
            centralReturnPath.remove(0);
            path.addAll(enterCentralAreaPath);
            path.addAll(centralReturnPath);

        //Finds return path for restaurant inside of central area
        } else {
            List<Node> directReturnPath = findPath(toRestaurantPath.get(toRestaurantPath.size() - 1).lng(), toRestaurantPath.get(toRestaurantPath.size() - 1).lat(), APPLETON.lng(), APPLETON.lat(), noFlyZones, orderNo);
            path.addAll(directReturnPath);
        }

        return path;
    }

    /**
     * Calculates the full day path for drone deliveries.
     * @param noFlyZones Array of no-fly zones.
     * @param centralArea Central area of operation.
     * @param restaurants Array of available restaurants.
     * @param dayOrders Orders to be delivered in a day.
     * @return Pair of a list of nodes forming the full path and a list of paths for each order.
     * @throws IOException If an I/O error occurs.
     */
    public static Pair<List<Node>, List<List<Node>>> getFullDayPath(NamedRegion[] noFlyZones, NamedRegion centralArea, Restaurant[] restaurants, Order[] dayOrders, String dateArg) throws IOException {
        JsonConverter jsonConverter = new JsonConverter();

        List<Node> fullPath = new ArrayList<Node>();
        //pathList used for debugging purposes
        List<List<Node>> pathList = new ArrayList<>();

        String orderNo = "";

        //Loop through orders to make a path for each
        for (Order order : dayOrders) {
            orderNo = order.getOrderNo();
            if (order.getOrderStatus() == OrderStatus.VALID_BUT_NOT_DELIVERED) {
                List<Node> orderPath = getOrderPath(noFlyZones, centralArea, getOrderRestaurant(order, restaurants), orderNo);

                //Adds each path separately for debugging
                pathList.add(orderPath);

                //Adds each node from path to create complete path for all orders
                fullPath.addAll(orderPath);

                order.setOrderStatus(OrderStatus.DELIVERED);
            }
        }

        //Add a hover node for the last delivery if path isn't empty
        if (fullPath.size() > 1) {
            Node lastNode = new Node(fullPath.get(fullPath.size() - 1).lngLat, 999, orderNo);
            lastNode.parent = fullPath.get(fullPath.size() - 1);
            fullPath.add(lastNode);
        }

        //Write delivery and flightpath data to output files
        jsonConverter.convertNodesToGeoJson(fullPath, "drone-" + dateArg);
        jsonConverter.convertFlightpathToJson(fullPath, "flightpath-" + dateArg);
        jsonConverter.convertOrdersToJson(dayOrders, "deliveries-" + dateArg);
        System.out.println("Complete day path number of moves: " + fullPath.size());

        return (Pair.of(fullPath, pathList));
    }
}