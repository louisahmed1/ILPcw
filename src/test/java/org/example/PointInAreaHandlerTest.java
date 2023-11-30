package org.example;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.ed.inf.ilp.data.LngLat;

import static org.example.PointInAreaHandler.isPointInsidePolygon;

public class PointInAreaHandlerTest extends TestCase {

    public PointInAreaHandlerTest (String testName) {  super (testName); }

    public static Test suite() { return new TestSuite(PointInAreaHandlerTest.class);  }

    PointInAreaHandler pointInAreaHandler;
    @Override
    protected void setUp() {
        pointInAreaHandler = new PointInAreaHandler();
    }

    public void testPoint1() {
        // Test case 1: The point is within the shape.
        LngLat point = new LngLat(2.0, 3.0);
        LngLat[] shapeVertices = new LngLat[]{new LngLat(0.0, 4.0), new LngLat(2.0, 0.0), new LngLat(5.0, 1.0), new LngLat(6.0, 3.0)};

        boolean isPointInsidePolygon = isPointInsidePolygon(point, shapeVertices);

        assertTrue(isPointInsidePolygon);
    }
    public void testPoint2() {
// Test case 2: The point is not within the shape.
            LngLat point2 = new LngLat(7.0, 3.0);
            LngLat[] shapeVertices2 = new LngLat[]{new LngLat(0.0, 4.0), new LngLat(2.0, 0.0), new LngLat(5.0, 1.0), new LngLat(6.0, 3.0)};

            boolean isPointInsidePolygon = isPointInsidePolygon(point2, shapeVertices2);

            assertFalse(isPointInsidePolygon);
    }

    public void testPoint3() {
// Test case 3: The point is on the edge of the shape but not a vertex.
        LngLat point3 = new LngLat(1.5, 1.0);
        LngLat[] shapeVertices3 = new LngLat[]{new LngLat(0.0, 4.0), new LngLat(2.0, 0.0), new LngLat(5.0, 1.0), new LngLat(6.0, 3.0)};

        boolean isPointInsidePolygon = isPointInsidePolygon(point3, shapeVertices3);

        assertTrue(isPointInsidePolygon);
    }

    public void testPoint4() {
// Test case 4: The point is at a vertex of the shape.
        LngLat point4 = new LngLat(0.0, 4.0);
        LngLat[] shapeVertices4 = new LngLat[]{new LngLat(0.0, 4.0), new LngLat(2.0, 0.0), new LngLat(5.0, 1.0), new LngLat(6.0, 3.0)};

        boolean isPointInsidePolygon = isPointInsidePolygon(point4, shapeVertices4);

        assertTrue(isPointInsidePolygon);
    }

    public void testPoint5() {
// Test case 5: The shape is a degenerate triangle.
        LngLat point5 = new LngLat(0.0, 0.0);
        LngLat[] shapeVertices5 = new LngLat[]{new LngLat(0.0, 0.0), new LngLat(0.0, 1.0), new LngLat(0.0, 2.0)};

         boolean isPointInsidePolygon = isPointInsidePolygon(point5, shapeVertices5);

        assertTrue(isPointInsidePolygon);
    }

    public void testPoint6() {
// Test case 6: The point is inside of the shape and the shape has a divet in it.
        LngLat point6 = new LngLat(0.25, 0.5);
        LngLat point7 = new LngLat(0.75,0.5);
        LngLat[] shapeVertices6 = new LngLat[]{new LngLat(0.0, 0.0), new LngLat(1.0, 0.0),new LngLat(1.0, 1.0),new LngLat(0.0, 1.0), new LngLat(0.5, 0.5)};

        boolean isPointInsidePolygon = isPointInsidePolygon(point6, shapeVertices6);
        boolean isPoint2InsidePolygon = isPointInsidePolygon(point7, shapeVertices6);

        assertFalse(isPointInsidePolygon);
        assertTrue(isPoint2InsidePolygon);
    }

    public void testPoint7() {
// Test case 7: Same polygon tested as case 6 with different orientation due to order of vertices
        LngLat point8 = new LngLat(0.5,0.25);
        LngLat point9 = new LngLat(0.25,0.5);

        LngLat[] shapeVertices7 = new LngLat[]{new LngLat(0,0), new LngLat(0,1), new LngLat(1,1), new LngLat(1,0), new LngLat(0.5,0.5)};

        boolean isPointInsidePolygon = isPointInsidePolygon(point8, shapeVertices7);
        boolean isPoint2InsidePolygon = isPointInsidePolygon(point9, shapeVertices7);

        Assert.assertFalse(isPointInsidePolygon);
        Assert.assertTrue(isPoint2InsidePolygon);
    }
}
