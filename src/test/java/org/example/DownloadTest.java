package org.example;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadTest extends TestCase {
    /**
     * Rigorous Test :-)
     */

    public DownloadTest(String testName) { super(testName); }

    public static Test suite() { return new TestSuite( DownloadTest.class); }

    public void testDownload() {
        Download.main(new String[]{"https://ilp-rest.azurewebsites.net/","restaurants.geojson"});
        Path path = Paths.get("restaurants.geojson");
        Assert.assertTrue(Files.exists(path));
    }
}
