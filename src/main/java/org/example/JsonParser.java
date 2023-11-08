package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonParser {
    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    public static Restaurant[] parseRestaurant(String jsonString) throws IOException {

        return objectMapper.readValue(jsonString, Restaurant[].class);
    }

    public static Order[] parseOrders(String jsonString) throws IOException {

        return objectMapper.readValue(jsonString, Order[].class);
    }

    public static NamedRegion parseCentralArea(String jsonString) throws IOException {

        return objectMapper.readValue(jsonString, NamedRegion.class);
    }

    public static NamedRegion[] parseNoFlyZones(String jsonString) throws IOException {

        return objectMapper.readValue(jsonString, NamedRegion[].class);
    }


}
