package edu.mit.ll.vizlincdb.geo;

import edu.mit.ll.vizlincdb.geo.GeoBoundingBox;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class GeoBoundingBoxTest {

    public GeoBoundingBoxTest() {
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
     * Test of isValid method, of class GeoBoundingBox.
     */
    @Test
    public void testIsValid() {
        assertFalse(new GeoBoundingBox(INVALID_LAT_LON, INVALID_LAT_LON, INVALID_LAT_LON, INVALID_LAT_LON).isValid());
        assertFalse(GeoBoundingBox.INVALID.isValid());
        assertTrue(new GeoBoundingBox(1.0, 2.0, 3.0, 4.0).isValid());
    }
}
