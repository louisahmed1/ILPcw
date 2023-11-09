package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonConverter {
    public static void convertNodesToGeoJson(List<Node> nodes, String fileName) throws IOException {
        Map<String, Object> geoJson = new HashMap<>();
        geoJson.put("type", "FeatureCollection");

        List<double[]> coordinates = new ArrayList<>();
        for (Node node : nodes) {
            coordinates.add(new double[]{node.lng(), node.lat()});
        }

        Map<String, Object> feature = new HashMap<>();
        feature.put("type", "Feature");

        Map<String, Object> geometry = new HashMap<>();
        geometry.put("type", "LineString");
        geometry.put("coordinates", coordinates);

        // Adding an empty properties map for the feature
        Map<String, Object> properties = new HashMap<>();
        feature.put("properties", properties);

        feature.put("geometry", geometry);

        List<Map<String, Object>> features = new ArrayList<>();
        features.add(feature);
        geoJson.put("features", features);

        File file = new File(fileName + ".geojson");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(file, geoJson);
    }

    public static void writeGeoJson(List<LngLat> points, String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode geoJson = mapper.createObjectNode();

        geoJson.put("type", "FeatureCollection");
        ArrayNode features = mapper.createArrayNode();

        for (LngLat point : points) {
            ObjectNode feature = mapper.createObjectNode();
            feature.put("type", "Feature");

            ObjectNode geometry = mapper.createObjectNode();
            geometry.put("type", "Point");
            ArrayNode coordinates = mapper.createArrayNode();
            coordinates.add(point.lng());
            coordinates.add(point.lat());
            geometry.set("coordinates", coordinates);

            feature.set("geometry", geometry);
            feature.set("properties", mapper.createObjectNode()); // Adding an empty properties object

            features.add(feature);
        }

        geoJson.set("features", features);

        // Write the geoJson to a file
        mapper.writeValue(new File(filePath + ".geojson"), geoJson);
    }

    // Main method for testing
    // ...

    class OrderDTO {
        private String orderNo;
        private String orderStatus;
        private String orderValidationCode;
        private int priceTotalInPence;

        public OrderDTO(Order order) {
            this.orderNo = order.getOrderNo();
            this.orderStatus = String.valueOf(order.getOrderStatus());
            this.orderValidationCode = String.valueOf(order.getOrderValidationCode());
            this.priceTotalInPence = order.getPriceTotalInPence();
        }

        public String getOrderNo() { return this.orderNo; }
        public String getOrderStatus() { return this.orderStatus; }
        public String getOrderValidationCode() { return this.orderValidationCode; }

        public int getPriceTotalInPence() { return this.priceTotalInPence; }
        // Getters and setters go here
    }

    public void convertOrdersToJson(Order[] orders, LocalDate date) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            OrderDTO[] orderDTOs = new OrderDTO[orders.length];
            for (int i = 0; i < orders.length; i++) {
                orderDTOs[i] = new OrderDTO(orders[i]);
            }
            String orderString = mapper.writeValueAsString(orderDTOs);
            Files.write(Paths.get("deliveries-" + date.toString() + ".json"), orderString.getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void convertFlightpathToJson(List<Node> flightpath, String fileName) {
        // Initialize ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        // Define the output file path
        String outputPath = fileName + ".json";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            // Start of JSON array
            writer.write("[");
            for (int i = 1; i < flightpath.size(); i++) {
                Node parent = flightpath.get(i - 1);
                Node current = flightpath.get(i);

                // Create a map to hold the attributes
                Map<String, Object> record = new HashMap<>();
                record.put("orderNo", current.orderNo);
                record.put("fromLongitude", parent.lng);
                record.put("fromLatitude", parent.lat);
                record.put("angle", current.angle);
                record.put("toLongitude", current.lng);
                record.put("toLatitude", current.lat);



                // Serialize the map to JSON
                String json = mapper.writeValueAsString(record);

                // Write the JSON string to the file
                writer.write(json);

                // If this is not the last item, add a comma
                if (i < flightpath.size() - 1) {
                    writer.write(",");
                }

                // Add a newline for readability (optional)
                writer.newLine();
            }

            // End of JSON array
            writer.write("]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
