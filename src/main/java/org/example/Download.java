package org.example;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Simple download application to retrieve files from a REST server.
 */
public class Download {

    private static final Logger LOGGER = Logger.getLogger("Logger");

    /**
     * Downloads a file from the specified URL and saves it locally.
     *
     * @param args Array of strings with expected length 2, containing a base address and a filename.
     */
    public static void main(String[] args) {
        String baseUrl = args[0];
        String endpoint = args[1];
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        URL fullUrl;
        try {
            fullUrl = new URL(baseUrl + endpoint);
            // Download the file
            try (BufferedInputStream in = new BufferedInputStream(fullUrl.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(endpoint)) {
                byte[] dataBuffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                    fileOutputStream.write(dataBuffer, 0, bytesRead);
                }
                System.out.println("File was downloaded: " + endpoint);
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error downloading file: " + baseUrl + endpoint + ". Check arguments.");
            System.exit(1);
        }
    }

    /**
     * Downloads relevant files from the server for system operation.
     *
     * @param args Arguments used for specifying the server URL and the date for orders.
     */
    public static void downloadAll(String[] args) {
        Download.main(new String[]{args[1], "restaurants"});
        Download.main(new String[]{args[1], "centralArea"});
        Download.main(new String[]{args[1], "noFlyZones"});
        Download.main(new String[]{args[1] + "/orders/", args[0]});
    }
}