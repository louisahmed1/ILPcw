package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class JsonConverter {

    private static final Logger LOGGER = Logger.getLogger(JsonConverter.class.getName());

    /**
     * Converts a list of Nodes to a GeoJSON file.
     * @param nodes The list of Node objects.
     * @param fileName The name of the file to save.
     * @throws IOException If an I/O error occurs.
     */
    public void convertNodesToGeoJson(List<Node> nodes, String fileName) throws IOException {
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
        System.out.println("File was written: " + fileName + ".geojson");
    }

    // Nested class for Data Transfer Object
    static class OrderDTO {
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
    }

    /**
     * Converts orders to a JSON file.
     * @param orders Array of Order objects.
     * @param fileName The name of the deliveries file.
     */
    public void convertOrdersToJson(Order[] orders, String fileName) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            OrderDTO[] orderDTOs = new OrderDTO[orders.length];
            for (int i = 0; i < orders.length; i++) {
                orderDTOs[i] = new OrderDTO(orders[i]);
            }
            String orderString = mapper.writeValueAsString(orderDTOs);
            Files.write(Paths.get(fileName + ".json"), orderString.getBytes());
            System.out.println("File was written: " + fileName + ".json");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            System.exit(1);
        }
    }

    /**
     * Converts a flight path to JSON format.
     * @param flightpath List of Node objects representing the flight path.
     * @param fileName The file name to save the JSON data.
     */
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
                record.put("fromLongitude", parent.lng());
                record.put("fromLatitude", parent.lat());
                record.put("angle", current.angle);
                record.put("toLongitude", current.lng());
                record.put("toLatitude", current.lat());

                String json = mapper.writeValueAsString(record);
                writer.write(json);
                // If this is not the last item, add a comma
                if (i < flightpath.size() - 1) {
                    writer.write(",");
                }
                //newline for readability
                writer.newLine();
            }
            writer.write("]");
            System.out.println("File was written: " + outputPath);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.toString(), e);
            System.exit(1);
        }
    }
}
