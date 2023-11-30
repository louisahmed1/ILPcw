package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * JsonParser class for parsing various JSON files.
 */
public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger LOGGER = Logger.getLogger(JsonParser.class.getName());

    /**
     * Parses JSON file to Restaurant array.
     * @param fileString The path to the JSON file.
     * @return Array of Restaurant objects.
     * @throws IOException If file cannot be read.
     */
    public Restaurant[] parseRestaurant(String fileString) throws IOException {
        Path path = Paths.get(fileString);
        if (!Files.exists(path)) {
            System.out.println("Restaurant file does not exist");
            return new Restaurant[0];
        }

        return objectMapper.readValue(Files.readString(path), Restaurant[].class);
    }

    /**
     * Parses JSON file to Order array.
     * @param fileString The path to the JSON file.
     * @return Array of Order objects.
     * @throws IOException If file cannot be read.
     */
    public Order[] parseOrders(String fileString) throws IOException {
        Path path = Paths.get(fileString);
        if (!Files.exists(path)) {
            System.out.println("Order(s) file does not exist");
            return new Order[0];
        }

        return objectMapper.readValue(Files.readString(path), Order[].class);
    }

    /**
     * Parses JSON file to a NamedRegion object representing the central area.
     * @param fileString The path to the JSON file.
     * @return NamedRegion object.
     * @throws IOException If file cannot be read.
     */
    public NamedRegion parseCentralArea(String fileString) throws IOException {
        Path path = Paths.get(fileString);
        if (!Files.exists(path)) {
            System.out.println("Central Area file does not exist");
            return null;
        }

        return objectMapper.readValue(Files.readString(path), NamedRegion.class);
    }

    /**
     * Parses JSON file to NamedRegion array representing no-fly zones.
     * @param fileString The path to the JSON file.
     * @return Array of NamedRegion objects.
     * @throws IOException If file cannot be read.
     */
    public NamedRegion[] parseNoFlyZones(String fileString) throws IOException {
        Path path = Paths.get(fileString);
        if (!Files.exists(path)) {
            System.out.println("No Fly Zones file does not exist");
            return new NamedRegion[0];
        }

        return objectMapper.readValue(Files.readString(path), NamedRegion[].class);
    }
}