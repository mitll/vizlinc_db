package edu.mit.ll.vizlincdb.geo;

import edu.mit.ll.vizlincdb.util.InternedStringTable;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;

/**
 * A geolocation,using data from NGA, openstreetmaps, etc.
 */
public class GeoLocation {
    /**
     * Latitude of geographical point, in decimal degrees (+/-).
     */
    public final double latitude;
    /**
     * Longitude of geographical point, in decimal degrees (+/-).
     */
    public final double longitude;
    /**
     * Rectangular bounding box of location, if known; null if not supplied.
     */
    public final GeoBoundingBox boundingBox;
    /**
     * Name of location, as supplied by geo database.
     */
    public final String name;
    /**
     * Openstreetmaps feature type: locality, hamlet, etc.; null if not from osm. The empty string is converted to null.
     * See http://wiki.openstreetmap.org/wiki/Map_Features.
     */
    public final String osmType;
    /**
     * NGA feature type designation; null if not from NGA.  The empty string is converted to null.
     * See http://geonames.nga.mil/ggmagaz/feadesgsearchhtml.asp.
     */
    public final String ngaDesignation;
    /**
     * Two-letter country code (all caps).
     */
    public final String country;
    /**
     * Source of geolocation: NGA, openstreetmaps ("osm"), etc.
     */
    public final String source;
    
    protected static InternedStringTable osmTypeStrings = new InternedStringTable();
    protected static InternedStringTable ngaDesignationStrings = new InternedStringTable();
    protected static InternedStringTable countryStrings = new InternedStringTable();
    protected static InternedStringTable sourceStrings = new InternedStringTable();

    /**
     * Create a GeoLocation.
     * @param latitude
     * @param longitude
     * @param boundingBox
     * @param name
     * @param osmType may be be null or ""; "" is stored as null
     * @param ngaDesignation may be be null or ""; "" is stored as null
     * @param country
     * @param source
     */
    public GeoLocation(double latitude, double longitude, GeoBoundingBox boundingBox, String name, String osmType, String ngaDesignation, String country, String source) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.boundingBox = boundingBox;
        this.name = name;
        this.osmType = (osmType != null && osmType.isEmpty()) ? null : osmTypeStrings.get(osmType);
        this.ngaDesignation = (ngaDesignation != null && ngaDesignation.isEmpty()) ? null : ngaDesignationStrings.get(ngaDesignation);
        this.country = countryStrings.get(country);
        this.source = sourceStrings.get(source);
    }

    /**
     * Factory for creating GeoLocation from data supplied by openstreetmaps.
     * @param latitude
     * @param longitude
     * @param boundingBox
     * @param name
     * @param osmType
     * @param country
     * @return GeoLocation
     */
    public static GeoLocation OSMGeoLocation(double latitude, double longitude, GeoBoundingBox boundingBox, String name, String osmType, String country) {
        return new GeoLocation(latitude, longitude, boundingBox, name, osmType, null, country, GEO_SOURCE_OSM);
    }

    /**
     * Factory for creating GeoLocation from data supplied by NGA (no bounding box).
     * @param latitude
     * @param longitude
     * @param name
     * @param ngaDesignation
     * @param country
     */
    public static GeoLocation NGAGeoLocation(double latitude, double longitude, String name, String ngaDesignation, String country) {
        return new GeoLocation(latitude, longitude, null, name, null, ngaDesignation, country, GEO_SOURCE_NGA);
    }

    public boolean hasBoundingBox() {
        return boundingBox != null;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
        hash = 17 * hash + (this.boundingBox != null ? this.boundingBox.hashCode() : 0);
        hash = 17 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 17 * hash + (this.osmType != null ? this.osmType.hashCode() : 0);
        hash = 17 * hash + (this.ngaDesignation != null ? this.ngaDesignation.hashCode() : 0);
        hash = 17 * hash + (this.country != null ? this.country.hashCode() : 0);
        hash = 17 * hash + (this.source != null ? this.source.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeoLocation other = (GeoLocation) obj;
        if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude)) {
            return false;
        }
        if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        if (this.boundingBox != other.boundingBox && (this.boundingBox == null || !this.boundingBox.equals(other.boundingBox))) {
            return false;
        }
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.osmType == null) ? (other.osmType != null) : !this.osmType.equals(other.osmType)) {
            return false;
        }
        if ((this.ngaDesignation == null) ? (other.ngaDesignation != null) : !this.ngaDesignation.equals(other.ngaDesignation)) {
            return false;
        }
        if ((this.country == null) ? (other.country != null) : !this.country.equals(other.country)) {
            return false;
        }
        if ((this.source == null) ? (other.source != null) : !this.source.equals(other.source)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GeoLocation{" + "latitude=" + latitude + ", longitude=" + longitude + ", boundingBox=" + boundingBox + ", name=" + name + ", osmType=" + osmType + ", ngaDesignation=" + ngaDesignation + ", country=" + country + ", source=" + source + '}';
    }
}
