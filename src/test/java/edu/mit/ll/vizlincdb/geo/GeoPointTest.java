package edu.mit.ll.vizlincdb.geo;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class GeoPointTest {

    public GeoPointTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of hashCode method, of class GeoPoint.
     */
    @Test
    public void testHashCode() {
        assertEquals(new GeoPoint(45.0, 46.0, 25).hashCode(), new GeoPoint(45.0, 46.0, 25).hashCode());
    }

    /**
     * Test of equals method, of class GeoPoint.
     */
    @Test
    public void testEquals() {
        assertEquals(new GeoPoint(45.0, 46.0, 25), new GeoPoint(45.0, 46.0, 25));
   }

    /**
     * Test of toString method, of class GeoPoint.
     */
    @Test
    public void testToString() {
        assertEquals("GeoPoint{latitude=45.0, longitude=46.0, locationEntityId=25}", new GeoPoint(45.0, 46.0, 25).toString());
    }
}
