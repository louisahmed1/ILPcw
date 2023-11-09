package org.example;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws IOException {
        Download.downloadAll(args);

        Restaurant[] restaurants = JsonParser.parseRestaurant("restaurants");
        NamedRegion centralArea = JsonParser.parseCentralArea("centralArea");
        NamedRegion[] noFlyZones = JsonParser.parseNoFlyZones("noFlyZones");
        Order[] orders = JsonParser.parseOrders(args[0]);
        Order[] validOrders = OrderValidator.validateDailyOrders(orders, restaurants);

        Flightpath.getFullDayPath(noFlyZones, centralArea, restaurants, orders);
    }

}
