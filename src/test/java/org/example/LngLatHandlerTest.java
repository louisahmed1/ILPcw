package org.example;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.text.DecimalFormat;

public class LngLatHandlerTest extends TestCase {

    public LngLatHandlerTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(LngLatHandlerTest.class);
    }

    LngLatHandler lngLatHandler;

    @Override
    protected void setUp() throws Exception {
        lngLatHandler = new LngLatHandler();
    }

    public void testDistanceTo() {
        LngLat pointStart = new LngLat(-3.5, 56.5);
        LngLat pointEnd = new LngLat(-3.6, 56.7);
        double distanceBetween = Math.sqrt(Math.pow(0.1, 2.0) + Math.pow(0.2, 2.0));

        DecimalFormat df = new DecimalFormat("#.#############");

        Assert.assertEquals(Double.parseDouble(df.format(lngLatHandler.distanceTo(pointStart, pointEnd))), Double.parseDouble(df.format(distanceBetween)));
    }

    public void testIsCloseTo() {
        LngLat point1 = new LngLat(-3, 56);
        LngLat point2 = new LngLat(-3.00009, 56.00012);
        //exactly 0.00015 degree distance --> not close
        boolean isClose1 = lngLatHandler.isCloseTo(point1, point2);

        LngLat point3 = new LngLat(-3.00008, 56.00011);
        //just less than 0.00015 degree distance from point1 --> close
        boolean isClose2 = lngLatHandler.isCloseTo(point1, point3);

        LngLat point4 = new LngLat(-3.0001, 56.00013);
        //just more than 0.00015 degree distance from point1 --> not close
        boolean isClose3 = lngLatHandler.isCloseTo(point1, point4);


        Assert.assertFalse(isClose1);
        Assert.assertTrue(isClose2);
        Assert.assertFalse(isClose3);
    }

    public void testInCentralArea() {
        NamedRegion centralArea = new NamedRegion(SystemConstants.CENTRAL_REGION_NAME, new LngLat[]{new LngLat(-3.192473, 55.946233), new LngLat(-3.192473, 55.942617), new LngLat(-3.184319, 55.942617), new LngLat(-3.184319, 55.946233)});

        LngLat pointInCentral = new LngLat(-3.19, 55.945);
        LngLat pointOnCentral = new LngLat(-3.192473, 55.942617);
        LngLat pointOutOfCentral = new LngLat(-3.195, 56);

        Assert.assertTrue(lngLatHandler.isInCentralArea(pointInCentral, centralArea));
        Assert.assertTrue(lngLatHandler.isInCentralArea(pointOnCentral, centralArea));
        Assert.assertFalse(lngLatHandler.isInCentralArea(pointOutOfCentral, centralArea));
    }

    public void testNextPosition() {
        LngLat startPos = new LngLat(-3.2, 56);

        LngLat newPos = lngLatHandler.nextPosition(startPos, 45);
        LngLat newPos2 = lngLatHandler.nextPosition(startPos, 225);
        LngLat samePos = lngLatHandler.nextPosition(startPos, 999);

        DecimalFormat df = new DecimalFormat("#.#############");

        Assert.assertEquals(startPos, samePos);

        Assert.assertEquals(Double.parseDouble(df.format(newPos.lng())), Double.parseDouble(df.format(-3.1998939339828)));
        Assert.assertEquals(Double.parseDouble(df.format(newPos.lat())), Double.parseDouble(df.format(56.0001060660172)));

        Assert.assertEquals(Double.parseDouble(df.format(newPos2.lng())), Double.parseDouble(df.format(-3.2001060660172)));
        Assert.assertEquals(Double.parseDouble(df.format(newPos2.lat())), Double.parseDouble(df.format(55.9998939339828)));
    }
}
