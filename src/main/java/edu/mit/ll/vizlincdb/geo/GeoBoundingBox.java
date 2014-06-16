package edu.mit.ll.vizlincdb.geo;

import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;

/**
 * A geographic bounding box supplied by some geolocation databases.
 */
public class GeoBoundingBox {
    /**
     * Southern edge of the bounding box.
     */
    public final double latitudeSouth;
    /**
     * Northern edge of the bounding box.
     */
    public final double latitudeNorth;
    /**
     * Western edge of the bounding box.
     */
    public final double longitudeWest;
    /**
     * Eastern edge of the bounding box.
     */
    public final double longitudeEast;

    public GeoBoundingBox(double latitudeSouth, double latitudeNorth, double longitudeWest, double longitudeEast) {
        this.latitudeSouth = latitudeSouth;
        this.latitudeNorth = latitudeNorth;
        this.longitudeWest = longitudeWest;
        this.longitudeEast = longitudeEast;
    }

    /**
     * Sometimes we need a placeholder bounding box that isn't to be used.
     */
    public static GeoBoundingBox INVALID = new GeoBoundingBox(INVALID_LAT_LON, INVALID_LAT_LON, INVALID_LAT_LON, INVALID_LAT_LON);

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.latitudeSouth) ^ (Double.doubleToLongBits(this.latitudeSouth) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.latitudeNorth) ^ (Double.doubleToLongBits(this.latitudeNorth) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.longitudeWest) ^ (Double.doubleToLongBits(this.longitudeWest) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.longitudeEast) ^ (Double.doubleToLongBits(this.longitudeEast) >>> 32));
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
        final GeoBoundingBox other = (GeoBoundingBox) obj;
        if (Double.doubleToLongBits(this.latitudeSouth) != Double.doubleToLongBits(other.latitudeSouth)) {
            return false;
        }
        if (Double.doubleToLongBits(this.latitudeNorth) != Double.doubleToLongBits(other.latitudeNorth)) {
            return false;
        }
        if (Double.doubleToLongBits(this.longitudeWest) != Double.doubleToLongBits(other.longitudeWest)) {
            return false;
        }
        if (Double.doubleToLongBits(this.longitudeEast) != Double.doubleToLongBits(other.longitudeEast)) {
            return false;
        }
        return true;
    }

    /**
     * Does this GeoBoundingBox contain any data?
     * @return boolean
     */
    public boolean isValid() {
        return latitudeSouth != INVALID_LAT_LON;
    }

    @Override
    public String toString() {
        return "GeoBoundingBox{" + "latitudeSouth=" + latitudeSouth + ", latitudeNorth=" + latitudeNorth + ", longitudeWest=" + longitudeWest + ", longitudeEast=" + longitudeEast + '}';
    }
}
