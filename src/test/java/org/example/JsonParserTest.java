package org.example;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

public class JsonParserTest extends TestCase {

    private static JsonParser jsonParser = new JsonParser();

    public JsonParserTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(JsonParserTest.class);
    }



    public void testRestaurantParse() throws IOException {

        Path path = Paths.get("restaurants");
        if (!Files.exists(path)) {
            Download.main(new String[]{"https://ilp-rest.azurewebsites.net/", "restaurants"});
        }

        Restaurant[] restaurants = jsonParser.parseRestaurant("restaurants");

        Assert.assertNotNull(restaurants);
        Assert.assertEquals(restaurants[0].name(), "Civerinos Slice");
    }

    public void testOrderParse() throws IOException {

        Path path = Paths.get("orders");
        if (!Files.exists(path)) {
            Download.main(new String[]{"https://ilp-rest.azurewebsites.net/", "orders"});
        }

        Order[] orders = jsonParser.parseOrders("orders");

        Assert.assertNotNull(orders);
        Assert.assertEquals(orders[0].getOrderNo(), "19514FE0");
        Assert.assertEquals(orders[0].getOrderDate(), LocalDate.parse("2023-09-01"));
    }

    public void testCentralAreaParse() throws IOException {

        Path path = Paths.get("centralArea");
        if (!Files.exists(path)) {
            Download.main(new String[]{"https://ilp-rest.azurewebsites.net/","centralArea"});
        }

        NamedRegion centralArea = jsonParser.parseCentralArea("centralArea");

        Assert.assertNotNull(centralArea);
        Assert.assertEquals(centralArea.name(), SystemConstants.CENTRAL_REGION_NAME);
        Assert.assertEquals(centralArea.vertices()[0].lat(), 55.946233);
    }

    public void testNoFlyZonesParse() throws IOException {

        Path path = Paths.get("noFlyZones");
        if (!Files.exists(path)) {
            Download.main(new String[]{"https://ilp-rest.azurewebsites.net","noFlyZones"});
        }

        NamedRegion[] namedRegions = jsonParser.parseNoFlyZones("noFlyZones");

        Assert.assertNotNull(namedRegions);
        Assert.assertEquals(namedRegions[0].name(), "George Square Area");
        Assert.assertEquals(namedRegions[0].vertices()[0].lng(), -3.190578818321228);
    }
}
