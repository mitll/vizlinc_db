package edu.mit.ll.vizlincdb.util;

import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.util.Version;

/**
 * String constants and other values for graph property keys and edge labels, Lucene field names, etc.
 *
 * P_* strings are names of property keys.
 *
 * L_* strings are edge labels.
 *
 * E_* strings are P_ENTITY_TYPE and P_MENTION_TYPE values.
 *
 * P_GEO_* properties store lists of (lat,lon) pairs and (lat,lat,lon,lon) bounding boxes representing ranked possible
 * geolocations for a location entity. They form a set of parallel arrays, so for example, the third entries of
 * P_GEO_LAT_LIST and P_GEO_LON_LIST are the third-ranked values for the location. We have to use parallel arrays
 * because some graph databases won't store complex objects as property values.
 */

public final class VizLincProperties {

    // Not to be instantiated.
    private VizLincProperties() {
    }


    /**
     * Node (vertex type). Useful for iterating over all nodes of a particular type.
     */
    public final static String P_NODE_TYPE = "node_type";

    /**
     * Document nodes have properties: P_DOCUMENT_NAME, P_DOCUMENT_PATH, P_DOCUMENT_TEXT.
     * They may have out edges labeled  L_DOCUMENT_TO_MENTION that point to mention nodes.
     */
    public final static String NODE_TYPE_DOCUMENT = "document";

    /**
     * Mention nodes have properties: P_MENTION_TYPE, P_MENTION_INDEX, P_MENTION_GLOBAL_ID, P_MENTION_TEXT, P_MENTION_TEXT_START, P_MENTION_TEXT_STOP.
     * They may have in edges labeled  L_DOCUMENT_TO_MENTION that come from document nodes.
     */
    public final static String NODE_TYPE_MENTION = "mention";

    /**
     * Entity nodes have properties: P_ENTITY_TYPE, P_MENTION_TEXT.
     * The P_MENTION_TEXT value is the canonical value, which may be different among different mentions (e.g., "John"
     * and "John Smith" in mentions, "John Smith" in entity).
     */
    public final static String NODE_TYPE_ENTITY = "entity";

    /**
     * Use to look up a single unique value of a NODE_TYPE, to save string space.
     */
    public final static Map<String, String> NODE_TYPES = new HashMap<String, String>();
    static {
        NODE_TYPES.put(NODE_TYPE_DOCUMENT, NODE_TYPE_DOCUMENT);
        NODE_TYPES.put(NODE_TYPE_MENTION, NODE_TYPE_MENTION);
        NODE_TYPES.put(NODE_TYPE_ENTITY, NODE_TYPE_ENTITY);
    }

    /**
     * Unique name of a document. The name might be represented by a partial path, like "dirabc/document_x.txt".
     */
    public final static String P_DOCUMENT_NAME = "document_name";
    /**
     * Full path to document file.
     */
    public final static String P_DOCUMENT_PATH = "document_path";
    /**
     * The full document content, in plain text.
     */
    public final static String P_DOCUMENT_TEXT = "document_text";
    /**
     * The text for a mention.
     */
    public final static String P_MENTION_TEXT = "mention_text";
    /**
     * The text for a named entity.
     */
    public final static String P_ENTITY_TEXT = "entity_text";
    /**
     * The text for the global id of an entity.
     */
    public final static String P_ENTITY_GLOBAL_ID = "global_id";
    /**
     * The main type of a named entity: "PERSON", "ORGANIZATION", "LOCATION", "DATE".
     */
    public final static String P_ENTITY_TYPE = "entity_type";
    /**
     * The type of a mention: "PERSON", "ORGANIZATION", "LOCATION", "DATE".
     * We could use P_ENTITY_TYPE for this, but then a search on that key would return both mentions and entities.
     */
    public final static String P_MENTION_TYPE = "mention_type";
    /**
     * The index (0-based) of the mention of a named entity in a document.
     */
    public final static String P_MENTION_INDEX = "mention_index";
    /**
     * If this mention represents something well-known across documents (e.g. a topic, or a well-known entity),
     * this is the id to use to identify the mention as being a particular global entity.
     */
    public final static String P_MENTION_GLOBAL_ID = "global_id";
    /**
     * The character (not byte) offset of the beginning of a mention of a named entity in a document.
     */
    public final static String P_MENTION_TEXT_START = "mention_text_start";
    /**
     * The character (not byte) offset of the end of a mention of a named entity in a document.
     */
    public final static String P_MENTION_TEXT_STOP = "mention_text_stop";
    /**
     * A string labeling how this entity was created (by what algorithm, date of run, etc.)
     */
    public final static String P_CREATED_BY = "created_by";
    /**
     * The probability of a relation between the two entities; used on L_RELATED_ENTITY edges.
     */
    public final static String P_RELATED_ENTITY_PROBABILITY = "related_entity_probability";
    /**
     * Count of the number of co-occurring named entities between two documents.
     */
    public final static String P_SHARED_ENTITY_COUNT = "shared_entity_count";
    /**
     * Number of documents associated with this entity or mention.
     */
    public final static String P_NUM_DOCUMENTS = "num_docs";
    /**
     * Number of mentions associated with this entity.
     */
    public final static String P_NUM_MENTIONS = "num_mentions";


    /**
     * List of latitudes for possible locations for a location entity, in rank order.
     */
    public final static String P_GEO_LATITUDE_LIST = "geo_latitude_list";
    /**
     * List of longitudes for possible locations for a location entity, in rank order.
     */
    public final static String P_GEO_LONGITUDE_LIST = "geo_longitude_list";
    /**
     * List of southerly latitude boundaries for bounding boxes for a location entity, in rank order.
     */
    public final static String P_GEO_LATITUDE_SOUTH_LIST = "geo_latitude_south_list";
    /**
      * List of southerly latitude boundaries for bounding boxes for a location entity, in rank order.
     */
    public final static String P_GEO_LATITUDE_NORTH_LIST = "geo_latitude_north_list";
    /**
     * List of westerly longitude boundaries for bounding boxes for a location entity, in rank order.
     */
    public final static String P_GEO_LONGITUDE_WEST_LIST = "geo_longitude_west_list";
    /**
     * List of easterly longitude boundaries for bounding boxes for a location entity, in rank order.
     */
    public final static String P_GEO_LONGITUDE_EAST_LIST = "geo_longitude_east_list";
    /**
     * List of location names returned from a geolocation lookup; may not be the same as P_ENTITY_TEXT for a location entity.
     */
    public final static String P_GEO_NAME_LIST = "geo_name_list";
    /**
     * List of location types returned by openstreetmaps.
     */
    public final static String P_GEO_OSM_TYPE_LIST = "geo_osm_type_list";
    /**
     * List of location designations returned from the NGA database.
     */
    public final static String P_GEO_NGA_DESIGNATION_LIST = "geo_nga_designation_list";
    /**
     * List of country codes (two-letter all caps).
     */
    public final static String P_GEO_COUNTRY_LIST = "geo_country_list";
    /**
     * List of geocoding sources (GEO_SOURCES_* values).
     */
    public final static String P_GEO_SOURCE_LIST = "geo_source_list";


    /**
     * Edge label: document -> named entity mention.
     */
    public final static String L_DOCUMENT_TO_MENTION = "document_to_mention";
    /**
     * Edge label: document -> named entity.
     */
    public final static String L_DOCUMENT_TO_ENTITY = "document_to_entity";
    /**
     * Edge label: named entity -> mention.
     */
    public final static String L_MENTION_TO_ENTITY = "mention_to_entity";
    /**
     * Edge label: related entities (bidirectional).
     */
    public final static String L_RELATED_ENTITY = "related_entity";
    /**
     * Edge label: related documents (bidirectional).
     */
    public final static String L_RELATED_DOCUMENT = "related_document";

    /**
     * Entity and mention type "PERSON".
     */
    public final static String E_PERSON = "PERSON";
    /**
     * Entity and mention type "LOCATION".
     */
    public final static String E_LOCATION = "LOCATION";
    /**
     * Entity and mention type "ORGANIZATION".
     */
    public final static String E_ORGANIZATION = "ORGANIZATION";
    /**
     * Entity and mention type "DATE".
     */
    public final static String E_DATE = "DATE";

    /**
     * Use to look up a single unique value of an entity type, to save string space.
     */
    public final static Map<String, String> ENTITY_TYPES = new HashMap<String, String>();
    static {
        ENTITY_TYPES.put(E_PERSON, E_PERSON);
        ENTITY_TYPES.put(E_LOCATION, E_LOCATION);
        ENTITY_TYPES.put(E_ORGANIZATION, E_ORGANIZATION);
        ENTITY_TYPES.put(E_DATE, E_DATE);
    }

    /**
     * Version of Lucene used by VizLincDocumentIndexer.
     */
    public final static Version VIZLINCDB_LUCENE_VERSION = Version.LUCENE_36;

    /**
     * Value that represents a missing latitude or longitude.
     * We can't use null because some graph databases don't store null as a property value.
     */
    public final static double INVALID_LAT_LON = 999.0;
    /**
     * Geocoding from NGA database.
     */
    public final static String GEO_SOURCE_NGA = "nga";
    /**
     * Geocoding from openstreetmaps.
     */
    public final static String GEO_SOURCE_OSM = "osm";
    /**
     * Geocoding from a CSV file version of the NGA database.
     */
    public final static String GEO_SOURCE_NGACSV = "ngacsv";
    /**
     * Geocoding from a CSV file version of the OSM database.
     */
    public final static String GEO_SOURCE_OSMCSV = "osmcsv";
    /**
     * Geocoding from parsed latitude-longitude or equivalent coordinates.
     */
    public final static String GEO_SOURCE_COORDINATES = "coordinates";
    /**
     * Geocoding from a list of country names.
     */
    public final static String GEO_SOURCE_COUNTRIES = "countries";
    /**
     * {@link #P_CREATED_BY} value: for weakly disambiguated person entities
     */
    public final static String P_CREATED_BY_WEAK_ACROSS_DOC = "weak_across_doc_person_coref";
}
