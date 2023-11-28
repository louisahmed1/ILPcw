package org.example;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.tuple.Pair;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FlightpathTest extends TestCase {

    private static boolean alreadySetUp = false;
    private static NamedRegion[] noFlyZones;
    private static NamedRegion centralArea;
    private static Restaurant[] restaurants;
    private static LngLatHandler lngLatHandler = new LngLatHandler();

    private static LngLat tower = new LngLat(-3.186874, 55.944494);
    static Order[] orders;
    static List<Node> fullPath;
    static List<List<Node>> pathsList;
    public FlightpathTest(String testName) { super(testName) ; }

    public static Test suite() { return new TestSuite(FlightpathTest.class);}

    public static synchronized void oneTimeSetUp() throws IOException {
        Path path = Paths.get("noFlyZones");
        if (!Files.exists(path)) {
            Download.main(new String[]{"https://ilp-rest.azurewebsites.net/", "noFlyZones"});
        }
        noFlyZones = JsonParser.parseNoFlyZones("noFlyZones");

        Path path2 = Paths.get("centralArea");
        if (!Files.exists(path2)) {
            Download.main(new String[]{"https://ilp-rest.azurewebsites.net/", "centralArea"});
        }
        centralArea = JsonParser.parseCentralArea("centralArea");

        Path path3 = Paths.get("restaurants");
        if (!Files.exists(path3)) {
            Download.main(new String[]{"https://ilp-rest.azurewebsites.net/", "restaurants"});
        }
        restaurants = JsonParser.parseRestaurant("restaurants");

        Order[] orders2 = JsonParser.parseOrders("2023-10-15");

        LocalDate localDate = LocalDate.of(2023, 10, 10);
        LocalDate localDate1 = LocalDate.of(2023, 11, 11);
        orders = OrderValidator.validateDailyOrders(orders2, restaurants);
        //Order[] orders1 = OrderValidator.validateDailyOrders(orders2, restaurants);

        //List<Node> flightpath1 = Flightpath.getPath(noFlyZones, centralArea, new LngLat(-3.2000614, 55.951675));
        //Flightpath.convertNodesToGeoJson(flightpath1, "test1");
        //Path testPath = Paths.get("flightpath.geojson");
        //assertTrue(Files.exists(testPath));

        //List<Node> flightpath3 = Flightpath.getPath(noFlyZones, centralArea, Flightpath.getRestaurantPosition(orders[0], restaurants));
        //Flightpath.convertNodesToGeoJson(flightpath3, "test3");

        //Flightpath.getFullDayPath(noFlyZones, centralArea, restaurants, orders1);
        Pair<List<Node>, List<List<Node>>> pair = Flightpath.getFullDayPath(noFlyZones, centralArea, restaurants, orders);
        fullPath = pair.getLeft();
        pathsList = pair.getRight();

        JsonConverter.convertNodesToGeoJson(Flightpath.findPath(tower.lng(), tower.lat(), -3.1912869215011597,55.945535152517735, noFlyZones, "test"), "halfpath1");
        JsonConverter.convertNodesToGeoJson(Flightpath.findPath(-3.1912869215011597,55.945535152517735, tower.lng(), tower.lat(), noFlyZones, "test"), "shalfpath2");
        //JsonConverter.convertNodesToGeoJson(Flightpath.findPath(tower.lng(), tower.lat(), -3.202541470527649,55.943284737579376, noFlyZones), "halfpath2");
        //JsonConverter.convertNodesToGeoJson(Flightpath.findPath(tower.lng(), tower.lat(), -3.1838572025299072,55.94449876875712, noFlyZones), "halfpath3");
        //JsonConverter.convertNodesToGeoJson(Flightpath.findPath(tower.lng(), tower.lat(), -3.1940174102783203,55.94390696616939, noFlyZones), "halfpath4");

        //JsonConverter.convertNodesToGeoJson(Flightpath.getPath(noFlyZones, centralArea, new LngLat(-3.1912869215011597,55.945535152517735), tower), "rest1path");
        //JsonConverter.convertNodesToGeoJson(Flightpath.getPath(noFlyZones, centralArea, new LngLat(-3.202541470527649,55.943284737579376), tower), "rest2path");
        //JsonConverter.convertNodesToGeoJson(Flightpath.getPath(noFlyZones, centralArea, new LngLat(-3.1838572025299072,55.94449876875712), tower), "rest3path");
        //JsonConverter.convertNodesToGeoJson(Flightpath.getPath(noFlyZones, centralArea, new LngLat(-3.1940174102783203,55.94390696616939), tower), "rest4path");

    }

    public void setUp() throws IOException {
        if (!alreadySetUp) {
            oneTimeSetUp();
            alreadySetUp = true;
        }
    }

    public void testBasicTest() throws IOException {
        for (int i = 1; i < fullPath.size(); i++) {
            Node prevNode = fullPath.get(i - 1);
            Node currentNode = fullPath.get(i);
            for (NamedRegion zone : noFlyZones) {
                if (lngLatHandler.isInRegion(currentNode.lngLat, zone)) System.out.println("node in zone");
                if (PointInAreaHandler.doesLineIntersectPolygon(prevNode.lngLat, currentNode.lngLat, zone.vertices())) ;
            }
        }
    }

    public void testEachPathNodeDistances() throws IOException {
        List<LngLat> failPoints = new ArrayList<>();
        for (List<Node> orderPath : pathsList) {
            for (int i = 1; i < orderPath.size(); i ++) {
                Node prevNode = orderPath.get(i - 1);
                Node currentNode = orderPath.get(i);
                double distance = Math.abs(lngLatHandler.distanceTo(prevNode.lngLat, currentNode.lngLat));
                if (!((distance < 0.0001501 && distance > 0.0001499) || distance == 0)) {
                    if (!failPoints.contains(currentNode.lngLat)) {
                        failPoints.add(currentNode.lngLat);
                    }
                    if (!failPoints.contains(prevNode.lngLat)) {
                        failPoints.add(prevNode.lngLat);
                    }
                }
            }
        }
        if (failPoints.isEmpty()) {
            System.out.println("Sub path nodes have correct distances");
        } else {
            JsonConverter.writeGeoJson(failPoints, "sub_path_fail_points");
            System.out.println("Sub path node distances incorrect; geojson file of fail nodes created");
        }
    }

    public void testCompletePathNodeDistances() throws IOException {
        List<LngLat> failPoints = new ArrayList<>();
        for (int i = 1; i < fullPath.size(); i++) {
            Node prevNode = fullPath.get(i-1);
            Node currentNode = fullPath.get(i);
            double distance = Math.abs(lngLatHandler.distanceTo(prevNode.lngLat, currentNode.lngLat));
            if (!((distance < 0.0001501 && distance > 0.0001499) || distance == 0)) {
                if (!failPoints.contains(currentNode.lngLat)) {
                    failPoints.add(currentNode.lngLat);
                }
                if (!failPoints.contains(prevNode.lngLat)) {
                    failPoints.add(prevNode.lngLat);
                }
            }
        }
        if (failPoints.isEmpty()) {
            System.out.println("Complete path nodes have correct distances");
        } else {
            JsonConverter.writeGeoJson(failPoints, "full_path_fail_points");
            System.out.println("Full path node distances incorrect; geojson file of fail nodes created");
        }
    }

    public void testPathAngles() {
        double delta = 0.0001;
        for (List<Node> subPath : pathsList) {
            Assert.assertTrue(Math.abs(subPath.get(0).angle - 999.0) < delta);
        }
        //Assert.assertTrue(Math.abs(fullPath.get(fullPath.size()-1).angle - 999) < delta);
        for (int i = 1; i < fullPath.size(); i++) {
            //checks for a hover move
            if (Math.abs(fullPath.get(i).angle - 999) < delta) {
                Assert.assertFalse(Math.abs(fullPath.get(i).angle - fullPath.get(i-1).angle) < delta);
                Assert.assertTrue(Math.abs(lngLatHandler.distanceTo(fullPath.get(i).lngLat, fullPath.get(i-1).lngLat)) < SystemConstants.DRONE_MOVE_DISTANCE);
                //checks that distance between hover move and previous move is 0
                if (Math.abs(lngLatHandler.distanceTo(fullPath.get(i).lngLat , fullPath.get(i-1).lngLat)) > delta) {
                    System.out.println(fullPath.get(i).lngLat);
                    System.out.println(fullPath.get(i-1).lngLat);
                }

            }
        }
        for (List<Node> subPath : pathsList) {
            //Hovers variable to check that each journey to-and-from a restaurant has two hovers (pickup and dropoff)
            int hovers = 0;
            Assert.assertTrue(Math.abs(subPath.get(0).angle - 999) < delta);
            for (Node node : subPath) {
                if (Math.abs(node.angle - 999) < delta) {
                    hovers += 1;
                }
            }
            Assert.assertEquals(2, hovers);
        }
    }

    public void testSubPathEndPoints() {
        for (List<Node> subPath : pathsList) {
            System.out.println(lngLatHandler.distanceTo(subPath.get(subPath.size()-1).lngLat, tower));
            Assert.assertTrue(lngLatHandler.isCloseTo(subPath.get(subPath.size()-1).lngLat, tower));
        }
    }


}
