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
    private static JsonConverter jsonConverter = new JsonConverter();
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

        orders = OrderValidator.validateDailyOrders(orders2, restaurants);
        Pair<List<Node>, List<List<Node>>> pair = Flightpath.getFullDayPath(noFlyZones, centralArea, restaurants, orders, "2023-10-15");
        fullPath = pair.getLeft();
        pathsList = pair.getRight();

        jsonConverter.convertNodesToGeoJson(Flightpath.findPath(tower.lng(), tower.lat(), -3.1912869215011597,55.945535152517735, noFlyZones, "test"), "halfpath1");
        jsonConverter.convertNodesToGeoJson(Flightpath.findPath(-3.1912869215011597,55.945535152517735, tower.lng(), tower.lat(), noFlyZones, "test"), "shalfpath2");
    }

    public void setUp() throws IOException {
        if (!alreadySetUp) {
            oneTimeSetUp();
            alreadySetUp = true;
        }
    }

    public void testBasicTest() {
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
        Assert.assertEquals(failPoints, new ArrayList<>());
    }

    //Old test for un-needed requirement, can remove
    public void testCompletePathNodeDistances() throws IOException {
        List<LngLat> failPoints = new ArrayList<>();
        for (int i = 1; i < fullPath.size(); i++) {
            Node prevNode = fullPath.get(i-1);
            Node currentNode = fullPath.get(i);
            double distance = Math.abs(lngLatHandler.distanceTo(prevNode.lngLat, currentNode.lngLat));
            if (!((distance < 0.0001501 && distance > 0.0001499) || distance == 0) && prevNode.lngLat != tower && currentNode.lngLat != tower) {
                if (!failPoints.contains(currentNode.lngLat)) {
                    failPoints.add(currentNode.lngLat);
                }
                if (!failPoints.contains(prevNode.lngLat)) {
                    failPoints.add(prevNode.lngLat);
                }
            }
        }
        //Assert.assertSame(failPoints, new ArrayList<>());
    }

    public void testPathAngles() {
        double delta = 0.0001;
        for (List<Node> subPath : pathsList) {
            Assert.assertTrue(Math.abs(subPath.get(0).angle - 999.0) < delta);
        }
        for (int i = 1; i < fullPath.size(); i++) {
            //checks for a hover move
            if (Math.abs(fullPath.get(i).angle - 999) < delta) {
                Assert.assertFalse(Math.abs(fullPath.get(i).angle - fullPath.get(i-1).angle) < delta);
                Assert.assertTrue(Math.abs(lngLatHandler.distanceTo(fullPath.get(i).lngLat, fullPath.get(i-1).lngLat)) < SystemConstants.DRONE_MOVE_DISTANCE);
                //checks that distance between hover move and previous move is 0
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
            Assert.assertTrue(lngLatHandler.isCloseTo(subPath.get(subPath.size()-1).lngLat, tower));
        }
    }


}
