package org.example;

import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Main application class for handling flightpath operations.
 */
public class App {

    private static final Logger logger = Logger.getLogger("Logger");
    /**
     * Main method to start the application.
     *
     * @param args Command line arguments
     * @throws IOException If an I/O error occurs
     */
    public static void main(String[] args) throws IOException {
        checkArgs(args);
        Download.downloadAll(args);

        Restaurant[] restaurants = JsonParser.parseRestaurant("restaurants");
        NamedRegion centralArea = JsonParser.parseCentralArea("centralArea");
        NamedRegion[] noFlyZones = JsonParser.parseNoFlyZones("noFlyZones");
        Order[] orders = JsonParser.parseOrders(args[0]);
        Order[] validOrders = OrderValidator.validateDailyOrders(orders, restaurants);

        Flightpath.getFullDayPath(noFlyZones, centralArea, restaurants, validOrders);
    }

    private static void checkArgs(String[] args) {
        String datePattern = "^\\d{4}-\\d{2}-\\d{2}$";
        Pattern r = Pattern.compile(datePattern);
        Matcher m = r.matcher(args[0]);

        if (args.length != 2) {
            String errorMessage = "Error: Expected 2 arguments. Received " + args.length + ".";
            //System.err.println(errorMessage);
            logger.severe(errorMessage);
            System.exit(1);
        }

        if (!m.matches()) {
            String errorMessage = "Error: Argument 0 is invalid. Expected a string of format YYYY-DD-MM";
            //System.err.println(errorMessage);
            logger.severe(errorMessage);
            System.exit(1);
        }

        try {
            new URL(args[1]);
        } catch (MalformedURLException e) {
            String errorMessage = "Error: Argument 1 is invalid. Expected a URL (e.g. https://ilp-rest.azurewebsites.net)";
            //System.err.println(errorMessage);
            logger.severe(errorMessage);
            System.exit(1);
        }
    }
}