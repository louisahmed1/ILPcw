package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    public static Restaurant[] parseRestaurant(String fileString) throws IOException {
        Path path = Paths.get(fileString);
        if (!Files.exists(path)) {
            System.out.println("Restaurant file does not exist");
        }

        return objectMapper.readValue(Files.readString(path), Restaurant[].class);
    }

    public static Order[] parseOrders(String fileString) throws IOException {
        Path path = Paths.get(fileString);
        if (!Files.exists(path)) {
            System.out.println("Order(s) file does not exist");
        }

        return objectMapper.readValue(Files.readString(path), Order[].class);
    }

    public static NamedRegion parseCentralArea(String fileString) throws IOException {
        Path path = Paths.get(fileString);
        if (!Files.exists(path)) {
            System.out.println("Central Area file does not exist");
        }

        return objectMapper.readValue(Files.readString(path), NamedRegion.class);
    }

    public static NamedRegion[] parseNoFlyZones(String fileString) throws IOException {
        Path path = Paths.get(fileString);
        if (!Files.exists(path)) {
            System.out.println("No Fly Zones file does not exist");
        }

        return objectMapper.readValue(Files.readString(path), NamedRegion[].class);
    }


}
