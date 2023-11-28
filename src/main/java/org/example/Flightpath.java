package org.example;

import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class Flightpath {

    public static final LngLat appleton = new LngLat(-3.186874, 55.944494);
    private static List<Node> getNeighbors(Node current, Node end, NamedRegion[] noFlyZones, String orderNo) {
        List<Node> neighbors = new ArrayList<>();
        LngLatHandler lngLatHandler = new LngLatHandler();

        double[] angles = { 0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5, 180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5 };

        for (double angle : angles) {
            Node neighbor = new Node(lngLatHandler.nextPosition(current.lngLat, angle), angle, orderNo);
            double tentativeG = current.startCost + SystemConstants.DRONE_MOVE_DISTANCE; // tentative start cost for the neighbor

            if (isValidPosition(neighbor, current, noFlyZones)) {;
                neighbor.startCost = tentativeG;
                neighbor.endCost = lngLatHandler.distanceTo(neighbor.lngLat, end.lngLat);
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    private static boolean isValidPosition(Node position, Node current, NamedRegion[] noFlyZones) {
        LngLatHandler lngLatHandler = new LngLatHandler();
        for (NamedRegion zone : noFlyZones) {
            if (lngLatHandler.isInRegion(position.lngLat, zone)) return false;
            if (PointInAreaHandler.doesLineIntersectPolygon(position.lngLat, current.lngLat, zone.vertices())) return false;
        }
        return true;
    }

    private static final LngLatHandler lngLatHandler = new LngLatHandler();

    public static List<Node> findPath(double startLng, double startLat, double endLng, double endLat, NamedRegion[] noFlyZones, String orderNo) throws IOException {
        Node start = new Node(new LngLat(startLng, startLat), 999, orderNo);
        Node end = new Node(new LngLat(endLng, endLat), 999, orderNo);

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        Set<Node> openSetLookup = new HashSet<>(); // HashSet for O(1) contains check

        start.startCost = 0;
        start.endCost = lngLatHandler.distanceTo(start.lngLat, end.lngLat);
        openSet.add(start);
        openSetLookup.add(start);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            openSetLookup.remove(current);

            if (lngLatHandler.distanceTo(current.lngLat, end.lngLat) < 0.00014999) {
                List<Node> path = new ArrayList<>();
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

                    // Ensure the node is correctly reordered in the PriorityQueue
                    if (openSetLookup.contains(neighbor)) {
                        openSet.remove(neighbor);
                    }
                    openSet.add(neighbor);
                    openSetLookup.add(neighbor);
                }
            }
        }
        return null;  // no path found
    }

    public static List<Node> getPath(NamedRegion[] noFlyZones, NamedRegion centralArea, LngLat restaurantPos, LngLat startPos, String orderNo) throws IOException {
        //LngLat enterCentralPoint = LngLatHandler.findNearestPointOnPerimeter(restaurantPos, centralArea);

        List<Node> toRestaurantPath = findPath(appleton.lng(), appleton.lat(), restaurantPos.lng(), restaurantPos.lat(), noFlyZones, orderNo);
        LngLat enterCentralPoint = LngLatHandler.findNearestPointOnPerimeter(toRestaurantPath.get(toRestaurantPath.size()-1).lngLat, centralArea);
        List<Node> path = new ArrayList<Node>(toRestaurantPath);

        if (!lngLatHandler.isInCentralArea(restaurantPos, centralArea)) {
            List<Node> enterCentralAreaPath = findPath(toRestaurantPath.get(toRestaurantPath.size() - 1).lng, toRestaurantPath.get(toRestaurantPath.size() - 1).lat, enterCentralPoint.lng(), enterCentralPoint.lat(), noFlyZones, orderNo);
            List<Node> centralReturnPath = findPath(enterCentralAreaPath.get(enterCentralAreaPath.size()-1).lng, enterCentralAreaPath.get(enterCentralAreaPath.size()-1).lat, -3.186874, 55.944494, noFlyZones, orderNo);
            centralReturnPath.remove(0);
            path.addAll(enterCentralAreaPath);
            path.addAll(centralReturnPath);
        } else {
            List<Node> directReturnPath = findPath(toRestaurantPath.get(toRestaurantPath.size() - 1).lng, toRestaurantPath.get(toRestaurantPath.size() - 1).lat, -3.186874, 55.944494, noFlyZones, orderNo);
            path.addAll(directReturnPath);
        }

        return path;
    }

    public static LngLat getOrderRestaurant(Order order, Restaurant[] restaurants) {

        for (Restaurant restaurant : restaurants) {
            if (Arrays.asList(restaurant.menu()).contains(order.getPizzasInOrder()[0])) {
                return restaurant.location();
            }
        }
        return null;
    }

    public static Pair<List<Node>, List<List<Node>>> getFullDayPath(NamedRegion[] noFlyZones, NamedRegion centralArea, Restaurant[] restaurants, Order[] dayOrders) throws IOException {
        JsonConverter jsonConverter = new JsonConverter();
        List<Node> fullPath = new ArrayList<Node>();
        LocalDate orderDate = dayOrders[0].getOrderDate();
        List<List<Node>> pathList = new ArrayList<>();
        String orderNo = "";
        LngLat startPos = new LngLat(-3.186874, 55.944494);

        for (Order order : dayOrders) {
            orderNo = order.getOrderNo();
            if (order.getOrderStatus() == OrderStatus.VALID_BUT_NOT_DELIVERED) {
                List<Node> orderPath = getPath(noFlyZones, centralArea, getOrderRestaurant(order, restaurants), startPos, orderNo);
                startPos = orderPath.get(orderPath.size() - 1).lngLat;
                pathList.add(orderPath);
                fullPath.addAll(orderPath);
                order.setOrderStatus(OrderStatus.DELIVERED);
            }
        }
        Node lastNode = new Node(fullPath.get(fullPath.size()-1).lngLat, 999, orderNo);
        lastNode.parent = fullPath.get(fullPath.size()-1);
        fullPath.add(lastNode);

        jsonConverter.convertNodesToGeoJson(fullPath, "drone-" + orderDate.toString());
        jsonConverter.convertFlightpathToJson(fullPath, "flightpath-" + orderDate);
        jsonConverter.convertOrdersToJson(dayOrders, orderDate);
        System.out.println("Complete day path number of moves: " + fullPath.size());

        return (Pair.of(fullPath, pathList));
    }
}