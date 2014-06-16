package edu.mit.ll.vizlincdb.geo;

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
public class GeoLocationTest {

    public GeoLocationTest() {
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
     * Test of GeoLocation constructor.
     */
    @Test
    public void testGeoLocation() {
        double latitude = 11.0;
        double longitude = -11.0;
        GeoBoundingBox boundingBox = new GeoBoundingBox(1.0, 2.0, 3.0, 4.0);
        String name = "name";
        String osmType = "hamlet";
        String ngaDesignation = "NGA";
        String country = "US";
        String source = "TESTSOURCE";
        GeoLocation loc = new GeoLocation(latitude, longitude, boundingBox, name, osmType, ngaDesignation, country, source);
        assertTrue(loc.latitude == latitude);
        assertTrue(loc.longitude == longitude);
        assertTrue(loc.boundingBox == boundingBox);
        assertTrue(loc.name.equals(name));
        assertTrue(loc.osmType.equals(osmType));
        assertTrue(loc.ngaDesignation.equals(ngaDesignation));
        assertTrue(loc.country.equals(country));

        // Certain empty strings get converted to null.
        GeoLocation loc1 = new GeoLocation(latitude, longitude, boundingBox, name, "", "", country, "TESTSOURCE");
        assertNull(loc1.osmType);
        assertNull(loc1.ngaDesignation);
}

    /**
     * Test of OSMGeoLocation method, of class GeoLocation.
     */
    @Test
    public void testOSMGeoLocation() {
        double latitude = 11.0;
        double longitude = -11.0;
        GeoBoundingBox boundingBox = new GeoBoundingBox(1.0, 2.0, 3.0, 4.0);
        String name = "name";
        String osmType = "hamlet";
        String country = "US";
        GeoLocation loc = GeoLocation.OSMGeoLocation(latitude, longitude, boundingBox, name, osmType, country);
        assertTrue(loc.latitude == latitude);
        assertTrue(loc.longitude == longitude);
        assertTrue(loc.boundingBox == boundingBox);
        assertTrue(loc.name.equals(name));
        assertTrue(loc.osmType.equals(osmType));
        assertNull(loc.ngaDesignation);
        assertTrue(loc.country.equals(country));
        assertTrue(loc.source.equals(GEO_SOURCE_OSM));
    }

    /**
     * Test of NGAGeoLocation method, of class GeoLocation.
     */
    @Test
    public void testNGAGeoLocation() {
        double latitude = 11.0;
        double longitude = -11.0;
        String name = "name";
        String ngaDesignation = "FOO";
        String country = "US";
        GeoLocation loc = GeoLocation.NGAGeoLocation(latitude, longitude, name, ngaDesignation, country);
        assertTrue(loc.latitude == latitude);
        assertTrue(loc.longitude == longitude);
        assertTrue(loc.boundingBox == null);
        assertTrue(loc.name.equals(name));
        assertNull(loc.osmType);
        assertTrue(loc.ngaDesignation.equals(ngaDesignation));
        assertTrue(loc.country.equals(country));
        assertTrue(loc.source.equals(GEO_SOURCE_NGA));
    }

    /**
     * Test of hasBoundingBox method, of class GeoLocation.
     */
    @Test
    public void testHasBoundingBox() {
        GeoBoundingBox bbox = new GeoBoundingBox(1.0, 2.0, 3.0, 4.0);
        GeoLocation locWithBBox = GeoLocation.OSMGeoLocation(11.0, -11.0, bbox, "name", "type", "US");
        GeoLocation locWithoutBBox = GeoLocation.NGAGeoLocation(11.0, -11.0, "name", "FOO", "US");
        assertTrue(locWithBBox.hasBoundingBox());
        assertFalse(locWithoutBBox.hasBoundingBox());
    }
}
