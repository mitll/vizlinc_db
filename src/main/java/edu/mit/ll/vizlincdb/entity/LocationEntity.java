package edu.mit.ll.vizlincdb.entity;

import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * To get GeoLocation information, use VizLincRDB#getGeoLocations() and GetFirstNGeoLocations().
 */
public class LocationEntity extends Entity {
    
    static final long serialVersionUID = 0x33E;

    private LocationEntity() { }
    
    public LocationEntity(String text, int num_documents, int num_mentions, String createdBy, int id) {
        super(text, num_documents, num_mentions, createdBy, id);
    }

    public static List<Integer> listOfLocationEntityIds(List<LocationEntity> entityList) {
        List<Integer> idList = new ArrayList<Integer>(entityList.size());
        for (LocationEntity e: entityList) {
            idList.add(e.getId());
        }
        return idList;
    }

    @Override
    public String getType()
    {
        return getTypeForClass();
    }

    public static String getTypeForClass() {
        return E_LOCATION;
    }
}