package org.example;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
/**
 * Simple download application to retrieve a file from the REST server
 **/

public class Download {

    /**
     *
     * @param args array of strings with expected length 2, containing a base address and a filename
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.err.println("Download Base−URL Filename");
            System.err.println("you must supply the base address of the ILP REST Service" + "e.g. http://restservice.somewhere and a filename to be loaded");
            System.exit(1);
        }
        URL finalUrl = null;
        String baseUrl = args[0];
        String filenameToLoad = args[1];
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        try {
            finalUrl = new URL(baseUrl + filenameToLoad);
        } catch (MalformedURLException e) {
            System.err.println("URL is invalid: " + baseUrl + filenameToLoad);
            System.exit(2);
        }
        try (BufferedInputStream in = new BufferedInputStream(finalUrl.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(filenameToLoad, false)) {
            byte[] dataBuffer = new byte [4096];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            System.out.println("File was written: " + filenameToLoad);
        } catch (IOException e) {
            System.err.format("Error loading file: %s from %s −> %s", filenameToLoad, finalUrl, e);
        }
    }

    public static void downloadAll(String[] args) {
        Download.main(new String[]{args[1], "restaurants"});
        Download.main(new String[]{args[1], "centralArea"});
        Download.main(new String[]{args[1], "noFlyZones"});
        Download.main(new String[]{args[1] + "/orders/", args[0]});
    }
}
