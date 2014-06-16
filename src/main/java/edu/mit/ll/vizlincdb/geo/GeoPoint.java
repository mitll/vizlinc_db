package edu.mit.ll.vizlincdb.geo;

import java.io.Serializable;

/**
 * A geographic point, designated by longitude and latitude.
 */
public class GeoPoint implements Serializable {
    
    static final long serialVersionUID = 0x20;

    /**
     * Latitude of geographical point, in decimal degrees (+/-).
     */
    public final double latitude;
    /**
     * Longitude of geographical point, in decimal degrees (+/-).
     */
    public final double longitude;
    /**
     * id of location entity designated by this point.
     */
    public final int locationEntityId;

    private GeoPoint() {
        this(0.0, 0.0, 0);
    }
    
    /**
     * Create a GeoPoint.
     * @param latitude
     * @param longitude
     * @param locationEntityId
     */
    public GeoPoint(double latitude, double longitude, int locationEntityId) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationEntityId = locationEntityId;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
        hash = 17 * hash + this.locationEntityId;
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
        final GeoPoint other = (GeoPoint) obj;
        if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude)) {
            return false;
        }
        if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude)) {
            return false;
        }
        if (this.locationEntityId != other.locationEntityId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "GeoPoint{" + "latitude=" + latitude + ", longitude=" + longitude + ", locationEntityId=" + locationEntityId + '}';
    }
}
