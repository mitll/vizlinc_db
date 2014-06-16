package edu.mit.ll.vizlincdb.relational;

import edu.mit.ll.vizlincdb.document.Document;
import edu.mit.ll.vizlincdb.geo.GeoBoundingBox;
import edu.mit.ll.vizlincdb.geo.GeoLocation;
import edu.mit.ll.vizlincdb.geo.GeoPoint;
import edu.mit.ll.vizlincdb.entity.Mention;
import edu.mit.ll.vizlincdb.entity.MentionLocation;
import edu.mit.ll.vizlincdb.entity.Entity;
import edu.mit.ll.vizlincdb.entity.LocationEntity;
import edu.mit.ll.vizlincdb.entity.OrganizationEntity;
import edu.mit.ll.vizlincdb.entity.EntitySet;
import edu.mit.ll.vizlincdb.entity.PersonEntity;
import edu.mit.ll.vizlincdb.entity.DateEntity;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An API to hide the details of an RDB version of the  VizLinc graph.
 */
public class VizLincRDB {

    public enum Implementation {
        H2(new String[] {"h2"});

        public static Implementation DEFAULT = H2;

        public final String[] tags;

        Implementation(String[] tags) {
            this.tags = tags;
        }

        /**
         * Return an Implementation that matches a given database path, e.g. H2 for "100docs-2013-01-29.h2.per".
         * Return DEFAULT if there's no match.
         * @param path
         * @return  an Implementation
         */
        public static Implementation implementationForPath(String path) {
            for (Implementation impl : Implementation.values()) {
                for (String tag : impl.tags) {
                    if (path.endsWith("." + tag) || path.contains("." + tag + ".")) {
                        return impl;
                    }
                }
            }
            return DEFAULT;
        }
    }

    public Connection conn;

    /**
     * Open an existing VizLinc database or create a new one at the path specified by the File (which will be a directory).
     *
     * @param databasePath specifies a directory which does not need to exist already (though the containing directory
     * should exist).
     * @param newDB true if a new database should be created. Will throw IllegalArgument exception if database exists
     * and newDB is true, or database is not present and newDB is false.
     */
    public VizLincRDB(File databasePath, boolean newDB) throws SQLException {
        this(databasePath.getPath(), newDB);
    }

    /**
     * Open an existing VizLinc database specified at the path specified by the File (which will be a directory).
     *
     * @param databasePath
     */
    public VizLincRDB(File databasePath) throws SQLException {
        this(databasePath.getPath(), false);
    }

     /**
     * Open an existing VizLinc database or create a new one at the path specified (which will be a directory).
     *
     * @param databasePath specifies a directory for the database. The implementation chosen is based on what's in the path
     * e.g., "abc.h2.foo" will open an H2 database.
     * @param newDB true if a new database should be created. Will throw IllegalArgument exception if a non-empty directory exists
     * and newDB is true, or if directory does not exists or is empty and newDB is false.
     * should exist).
     */
    public VizLincRDB(String databasePath, boolean newDB) throws SQLException {
        this(databasePath, newDB, Implementation.implementationForPath(databasePath));
    }

     /**
     * Open an existing VizLinc database or create a new one at the path specified (which will be a directory).
     *
     * @param databasePath specifies a directory for the database.
     * @param newDB true if a new database should be created. Will throw IllegalArgument exception if a non-empty directory exists
     * and newDB is true, or if directory does not exists or is empty and newDB is false.
     * should exist).
     * @param impl which relational db is used to implement this VizLincRDB.
     */
    public VizLincRDB(File databasePath, boolean newDB, Implementation impl) throws SQLException {
        this(databasePath.getPath(), newDB, impl);
    }

     /**
     * Open an existing VizLinc database or create a new one at the path specified (which will be a directory).
     *
     * @param databasePath specifies a directory for the database.
     * @param newDB true if a new database should be created. Will throw IllegalArgument exception if a non-empty directory exists
     * and newDB is true, or if directory does not exists or is empty and newDB is false.
     * should exist).
     * @param impl which relational db is used to implement this VizLincRDB.
     */
    public VizLincRDB(String databasePath, boolean newDB, Implementation impl) throws SQLException {
        File dir = new File(databasePath);
        if (dir.isDirectory() && dir.list().length > 0) {
            if (newDB) {
                throw new IllegalArgumentException("Non-empty directory already exists: cannot create a new database");
            }
        } else {
            if (!newDB) {
                throw new IllegalArgumentException("No database present: directory does not exist or is empty.");
            }
        }

        switch (impl) {
            case H2:
                conn = DriverManager.getConnection("jdbc:h2:" + databasePath + "/data" + ";CACHE_SIZE=131072", "sa", "");

                if (newDB) {
                    Statement stmt = conn.createStatement();
                    stmt.execute(H2_SCHEMA);

                }
                break;
        }
    }

     /**
     * Open an existing VizLinc database at the path specified (which will be a directory).
     *
     * @param databasePath
     */
    public VizLincRDB(String databasePath) throws SQLException {
        this(databasePath, false);
    }

    /**
     * Commit a transaction in progress.
     */
    public void commit() throws SQLException {
        conn.commit();
    }

    /**
     * Roll back a transaction in progress, discarding any changes made.
     */
    public void rollback() throws SQLException {
        conn.rollback();
    }

    /**
     * Provide an explicit shutdown operation in case it needs to be done before the JVM shuts down.
     */
    public void shutdown() throws SQLException {
        conn.close();
    }


    /**
     * Return all the document nodes in the graph.
     *
     * @return all the document nodes
     */
    public List<Document> getDocuments() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT document_id, name, path FROM document");
        List<Document> documents = new ArrayList<Document>();
        while (rs.next()) {
            documents.add(new Document(rs.getString("name"), rs.getString("path"), rs.getInt("document_id")));
        }
        return documents;
    }

    /**
     * Return the single document with the given id; return null if not found.
     *
     * @param documentId
     * @return Document or null
     * @throws SQLException
     */
    public Document getDocumentWithId(int documentId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT name, path FROM document WHERE document_id = ?");
        stmt.setInt(1, documentId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? new Document(rs.getString("name"), rs.getString("path"), documentId) : null;
    }

    /**
     * Return a List of all the documents with the given ids.
     *
     * @param documentIds List of document ids
     * @return List of Documents
     * @throws SQLException
     */
    public List<Document> getDocumentsWithIds(List<Integer> documentIds) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT document_id, name, path FROM document, TABLE(id INT = ?) ids" +
            " WHERE document.document_id = ids.id");
        stmt.setObject(1, documentIds.toArray());
        ResultSet rs = stmt.executeQuery();
        List<Document> documents = new ArrayList<Document>();
        while (rs.next()) {
            documents.add(new Document(rs.getString("name"), rs.getString("path"), rs.getInt("document_id")));
        }
        return documents;
   }

    /**
     * Fetch the full text for the document with the given id. Return null if not found.
     * @param documentId
     * @return String
     */
    public String getDocumentText(int documentId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT text FROM document WHERE document_id = ?");
        stmt.setInt(1, documentId);
        ResultSet rs = stmt.executeQuery();
        return rs.next() ? rs.getString("text") : null;
    }



    /**
     * Return the single entity with the given id; return null if not found.
     *
     * @param entityId
     * @return a subclass of Entity or null
     * @throws SQLException
     */
    public Entity getEntityWithId(int entityId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT entity_id, type, text, created_by, num_documents, num_mentions FROM entity WHERE entity_id = ?");
        stmt.setInt(1, entityId);
        ResultSet rs = stmt.executeQuery();
        return rs.next()
            ? Entity.create(rs.getString("type"), rs.getString("text"),
                rs.getInt("num_documents"), rs.getInt("num_mentions"), rs.getString("created_by"), entityId)
            : null;
    }


    /**
     * Return all the entities.
     *
     * @return EntitySet
     * @throws SQLException
     */
    public EntitySet getEntities() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT type, text, num_documents, num_mentions, created_by, entity_id FROM entity");
        EntitySet entities = new EntitySet();
        while (rs.next()) {
            entities.add(Entity.create(rs.getString(1), rs.getString(2),
                rs.getInt(3), rs.getInt(4), rs.getString(5), rs.getInt(6)));
        }
        return entities;
   }

    /**
     * Return all the entities with the given ids.
     *
     * @param entityIds List of entity ids
     * @return EntitySet
     * @throws SQLException
     */
    public EntitySet getEntitiesWithIds(List<Integer> entityIds) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT type, text, num_documents, num_mentions, created_by, entity_id FROM entity, TABLE (id INT = ?) ids" +
            " WHERE entity.entity_id = ids.id");
        stmt.setObject(1, entityIds.toArray());
        ResultSet rs = stmt.executeQuery();
        EntitySet entities = new EntitySet();
        while (rs.next()) {
            entities.add(Entity.create(rs.getString(1), rs.getString(2),
                rs.getInt(3), rs.getInt(4), rs.getString(5), rs.getInt(6)));
        }
        return entities;
   }

    /**
     * Return the mention with the given mentionId.
     * @param mentionId
     * @return
     */
    Mention getMentionWithId(int mentionId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT document_id, entity_id, type, text, index, global_id, text_start, text_stop FROM mention where mention_id = ?");
        stmt.setInt(1, mentionId);
        ResultSet rs = stmt.executeQuery();
        return rs.next()
            ? new Mention(mentionId, rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getInt(5), (Integer) rs.getObject(6), rs.getInt(7), rs.getInt(8))
            : null;
    }

    /**
     * Return all the mentions.
     *
     * @return EntitySet
     * @throws SQLException
     */
    public List<Mention> getMentions() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT mention_id, document_id, entity_id, type, text, index, global_id, text_start, text_stop FROM mention");
        List<Mention> mentions = new ArrayList<Mention>();
        while (rs.next()) {
            mentions.add(new Mention(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4), rs.getString(5), rs.getInt(6), (Integer) rs.getObject(7), rs.getInt(8), rs.getInt(9)));
        }
        return mentions;
   }

    /**
     * Return all the mentions, but don't fetch the mention texts. They are left as null.
     *
     * @return EntitySet
     * @throws SQLException
     */
    public List<Mention> getMentionsWithoutText() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT mention_id, document_id, entity_id, type, index, global_id, text_start, text_stop FROM mention");
        List<Mention> mentions = new ArrayList<Mention>();
        while (rs.next()) {
            mentions.add(new Mention(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4), null, rs.getInt(5), (Integer) rs.getObject(6), rs.getInt(7), rs.getInt(8)));
        }
        return mentions;
   }

    /**
     * Return all the mentions as MentionLocations.
     *
     * @return EntitySet
     * @throws SQLException
     */
    public List<MentionLocation> getMentionLocations() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT document_id, entity_id, index, text_start, text_stop, type FROM mention");
        List<MentionLocation> mentionLocations = new ArrayList<MentionLocation>();
        while (rs.next()) {
            mentionLocations.add(new MentionLocation(rs.getInt(1), (Integer)rs.getObject(2), rs.getInt(3), rs.getInt(4), rs.getInt(5),rs.getString(6)));
        }
        return mentionLocations;
   }


    /**
     * Extract the text from the document node's text property.
     * entities.
     *
     * @param doc a Document
     * @return null if not a document, otherwise the text as a String
     */
    public String getDocumentText(Document doc) throws SQLException {
        return doc.getText(this);
    }

   /**
     * Fetch the ranked list of geolocations associated with the given location node.
     * Return null if there are no locations.
     * @param locationEntityId
     * @return the list or null if none
     */
    public List<GeoLocation> getGeoLocations(int locationEntityId) throws SQLException {
        return getFirstNGeoLocations(locationEntityId, -1);
    }

    /**
     * Fetch the first n of the ranked list of geolocations associated with the given location node.
     * If n is = -1, there is no limit
     * Return null if there are no locations.
     * @param locationEntityId
     * @param n
     * @return the list or null if none
     */
    public List<GeoLocation> getFirstNGeoLocations(int locationEntityId, int n) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT latitude, longitude, latitude_south, latitude_north, longitude_west, longitude_east," +
                " name, osm_type, nga_designation, country, source FROM geolocation WHERE entity_id = ? ORDER BY rank" + ((n != -1) ? " LIMIT ?" : ""));
        stmt.setInt(1, locationEntityId);
        if (n != -1) {
             stmt.setInt(2, n);
        }
        ResultSet rs = stmt.executeQuery();
        List<GeoLocation> geoLocations = new ArrayList<GeoLocation>();
        while (rs.next()) {
            double latitudeSouth = rs.getDouble("latitude_south");
            boolean hasBoundingBox = !rs.wasNull();

            geoLocations.add(new GeoLocation(
                    rs.getDouble("latitude"),
                    rs.getDouble("longitude"),
                    hasBoundingBox ? new GeoBoundingBox(latitudeSouth, rs.getDouble("latitude_north"), rs.getDouble("longitude_west"), rs.getDouble("longitude_east"))
                        : null,
                    rs.getString("name"),
                    rs.getString("osm_type"),
                    rs.getString("nga_designation"),
                    rs.getString("country"),
                    rs.getString("source")));
        }
        return geoLocations;
    }

   /**
     * Fetch the geopints only from ranked list of geolocations associated with the given location node.
     * Return null if there are no locations.
     * @param locationEntityId
     * @return the list or null if none
     */
    public List<GeoPoint> getGeoPoints(int locationEntityId) throws SQLException {
        return getFirstNGeoPoints(locationEntityId, -1);
    }

    /**
     * Fetch the first n geopoints from the ranked list of geolocations associated with the given location node.
     * If n is = -1, there is no limit
     * Return null if there are no locations.
     * @param locationEntityId
     * @return the list or null if none
     */
    public List<GeoPoint> getFirstNGeoPoints(int locationEntityId, int n) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT latitude, longitude FROM geolocation WHERE entity_id = ? ORDER BY rank" + ((n != -1) ? " LIMIT ?" : ""));
        stmt.setInt(1, locationEntityId);
        if (n != -1) {
             stmt.setInt(2, n);
        }
        ResultSet rs = stmt.executeQuery();
        List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
        while (rs.next()) {
            geoPoints.add(new GeoPoint(rs.getDouble("latitude"), rs.getDouble("longitude"), locationEntityId));
        }
        return geoPoints;
    }

    /**
     * Return the rank 0 geoPoint for each given location entity.
     * @param locationEntityIds
     * @return a List of GeoPoints, in one-to-one correspondence with the locationEntityIds
     * @throws SQLException
     */
    public List<GeoPoint> getTopGeoPointsForLocationEntityIds(List<Integer> locationEntityIds) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
            "SELECT latitude, longitude, entity_id FROM geolocation, TABLE(id INT = ?) ids WHERE geolocation.entity_id = ids.id AND geolocation.rank = 0");
        stmt.setObject(1, locationEntityIds.toArray());
        ResultSet rs = stmt.executeQuery();
        List<GeoPoint> geoPoints = new ArrayList<GeoPoint>();
        while (rs.next()) {
            geoPoints.add(new GeoPoint(rs.getDouble("latitude"), rs.getDouble("longitude"), rs.getInt("entity_id")));
        }
        return geoPoints;

    }

    /**
     * Return a List of all the person entities in the database.
     * @return person entities
     * @throws SQLException
     */
    public List<PersonEntity> getPersonEntities() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT entity_id, text, created_by, num_documents, num_mentions FROM entity WHERE type = ?");
        stmt.setString(1, E_PERSON);
        ResultSet rs = stmt.executeQuery();
        List<PersonEntity> entities = new ArrayList<PersonEntity>();
        while (rs.next()) {
            entities.add(new PersonEntity(rs.getString("text"),
                    rs.getInt("num_documents"), rs.getInt("num_mentions"), rs.getString("created_by"), rs.getInt("entity_id")));
        }
        return entities;
   }

    /**
     * Return a List of all the location entities in the database.
     * @return location entities
     * @throws SQLException
     */
     public List<LocationEntity> getLocationEntities() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT entity_id, text, created_by, num_documents, num_mentions FROM entity WHERE type = ?");
        stmt.setString(1, E_LOCATION);
        ResultSet rs = stmt.executeQuery();
        List<LocationEntity> entities = new ArrayList<LocationEntity>();
        while (rs.next()) {
            entities.add(new LocationEntity(rs.getString("text"),
                    rs.getInt("num_documents"), rs.getInt("num_mentions"), rs.getString("created_by"), rs.getInt("entity_id")));
        }
        return entities;
   }

    /**
     * Return a List of all the date entities in the database.
     * @return date entities
     * @throws SQLException
     */
     public List<DateEntity> getDateEntities() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT entity_id, text, created_by, num_documents, num_mentions FROM entity WHERE type = ?");
        stmt.setString(1, E_DATE);
        ResultSet rs = stmt.executeQuery();
        List<DateEntity> entities = new ArrayList<DateEntity>();
        while (rs.next()) {
            entities.add(new DateEntity(rs.getString("text"),
                    rs.getInt("num_documents"), rs.getInt("num_mentions"), rs.getString("created_by"), rs.getInt("entity_id")));
        }
        return entities;
   }

    /**
     * Return a List of all the organization entities in the database.
     * @return the organization entities
     * @throws SQLException
     */
     public List<OrganizationEntity> getOrganizationEntities() throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT entity_id, text, created_by, num_documents, num_mentions FROM entity WHERE type = ?");
        stmt.setString(1, E_ORGANIZATION);
        ResultSet rs = stmt.executeQuery();
        List<OrganizationEntity> entities = new ArrayList<OrganizationEntity>();
        while (rs.next()) {
            entities.add(new OrganizationEntity(rs.getString("text"),
                    rs.getInt("num_documents"), rs.getInt("num_mentions"), rs.getString("created_by"), rs.getInt("entity_id")));
        }
        return entities;
   }


     /**
      * Return all the documents (designated by ids) which refer to ALL of the given entities (designated by id).
      * @param entityIds
      * @return  List of document ids
      * @throws SQLException
      */
     public List<Integer> getDocumentIdsWithAllOfTheseEntityIds(List<Integer> entityIds) throws SQLException {
        // http://stackoverflow.com/questions/7364969/how-to-filter-sql-results-in-a-has-many-through-relation/
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT de.document_id FROM document_entity de, TABLE(id INT = ?) ids" +
                " WHERE de.entity_id = ids.id" +
                " GROUP BY de.document_id HAVING COUNT(de.document_id) = ?");
        stmt.setObject(1, entityIds.toArray());
        stmt.setInt(2, entityIds.size());
        ResultSet rs = stmt.executeQuery();
        List<Integer> documentIds = new ArrayList<Integer>();
        while (rs.next()) {
            documentIds.add(rs.getInt(1));

        }
        return documentIds;
    }

   /**
    * Return all the unique mention texts for a given entity.
    * @param entityId
    * @return List of unique strings
    * @throws SQLException
    */
    public List<String> getDistinctMentionTextsForEntityId(int entityId) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT(text) FROM mention WHERE entity_id = ?");
        stmt.setInt(1, entityId);
        ResultSet rs = stmt.executeQuery();
        List<String> entities = new ArrayList<String>();
        while (rs.next()) {
            entities.add(rs.getString(1));
        }
        return entities;
    }

   /**
    * Return all the unique mention texts in a given document for a given entity
    * @param entityId
    * @return List of unique strings
    * @throws SQLException
    */
    public List<String> getDistinctMentionTextsForEntityIdInDocument(int entityId, int documentId) throws SQLException
    {
        // PreparedStatement stmt = conn.prepareStatement("SELECT DISTINCT(text) FROM mention WHERE document_id = ? AND entity_id = ?");
        // This is faster because it forces the use of the document_id index. Ugh.
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT(text) FROM mention WHERE mention_id IN (SELECT mention_id FROM mention WHERE document_id = ?) AND entity_id = ?");
        stmt.setInt(1, documentId);
        stmt.setInt(2, entityId);
        ResultSet rs = stmt.executeQuery();
        List<String> entities = new ArrayList<String>();
        while (rs.next()) {
            entities.add(rs.getString(1));
        }
        return entities;
    }

    /**
     * Return the ids for all the entities in the given set of documents (designated by document id)
     * @param docIds
     * @return List of entity ids
     * @throws SQLException
     */
    public List<Integer> getEntityIdsInAnyofTheseDocumentIds(List<Integer> docIds) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT(document_entity.entity_id) FROM document_entity, TABLE (id INT = ?) ids" +
                " WHERE document_entity.document_id = ids.id");
        stmt.setObject(1, docIds.toArray());
        ResultSet rs = stmt.executeQuery();
        List<Integer> entityIds = new ArrayList<Integer>();
        while (rs.next()) {
            entityIds.add(rs.getInt(1));
        }
        return entityIds;
    }

    /**
     * Return the number of mentions in the given documents for each of the given entities, given a subset of documents.
     * @param entityIds
     * @param docIds
     * @return map containing the entity id as key and the mention count for the
     * set of documents as value
     */
    public Map<Integer,Integer> getMentionCountsForEntitiesInDocuments(Collection<Integer> entityIds, Collection<Integer> docIds) throws SQLException
    {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT SUM(num_mentions), entity_id FROM document_entity, TABLE(id INT = ?) entity_ids, TABLE(id INT = ?) document_ids" +
                " WHERE document_entity.document_id = document_ids.id AND document_entity.entity_id = entity_ids.id" +
                " GROUP BY entity_id");
        stmt.setObject(1, entityIds.toArray());
        stmt.setObject(2, docIds.toArray());
        ResultSet rs = stmt.executeQuery();
        HashMap<Integer, Integer> entityIdToMentionCount = new HashMap<Integer, Integer>();
        while (rs.next()) {
            int count = rs.getInt(1);
            int entity_id = rs.getInt(2);
            entityIdToMentionCount.put(entity_id, count);
        }
        // If a document,entity pair was not in document_entity, it will not be added to entityIdToMentionCount.
        // So add any missing entities, and set their mentionCount to 0.
        for (int entityId : entityIds) {
            if (!entityIdToMentionCount.containsKey(entityId)) {
                entityIdToMentionCount.put(entityId, 0);
            }
        }
        return entityIdToMentionCount;
    }

    /**
     * Return the number of documents that have each of the given entities, given a subset of documents.
     * @param docIds
     * @param entityIds
     * @return map containing the entity id as key and the documents count for the given set of documents
     */
    public Map<Integer, Integer> getDocumentCountsForEntitiesInDocuments(Collection<Integer> entityIds,
            Collection<Integer> docIds) throws SQLException
    {
        PreparedStatement stmt  = conn.prepareStatement(
                "SELECT COUNT(de.document_id), de.entity_id FROM document_entity de, TABLE(id INT = ?) entity_ids, TABLE(id INT = ?) doc_ids" +
                " WHERE de.document_id = doc_ids.id" +
                " AND de.entity_id = entity_ids.id" +
                " GROUP BY de.entity_id");
        stmt.setObject(1, entityIds.toArray());
        stmt.setObject(2, docIds.toArray());
        ResultSet rs = stmt.executeQuery();
        HashMap<Integer, Integer> entityIdToDocumentCount = new HashMap<Integer, Integer>();
        while (rs.next()) {
            int count = rs.getInt(1);
            int entity_id = rs.getInt(2);
            entityIdToDocumentCount.put(entity_id, count);
        }
        return entityIdToDocumentCount;
   }



    /**
     * Schema of H2 database partially corresponding to graph schema.
     */
    public static String H2_SCHEMA =
"DROP ALL OBJECTS;\n" +
"\n" +
"CREATE TABLE entity (\n" +
"	entity_id INT,\n" +
"	type VARCHAR,\n" +
"	text VARCHAR,\n" +
"	created_by VARCHAR,\n" +
"	num_documents INT,\n" +
"	num_mentions INT,\n" +
"	PRIMARY KEY (entity_id)\n" +
"	);\n" +
"\n" +
"CREATE INDEX on entity(type);\n" +
"CREATE INDEX on entity(created_by);\n" +
"CREATE INDEX on entity(num_documents);\n" +
"\n" +
"\n" +
"CREATE TABLE geolocation (\n" +
"	entity_id INT,\n" +
"	rank INT,\n" +
"	latitude DOUBLE,\n" +
"	longitude DOUBLE,\n" +
"	latitude_south DOUBLE,\n" +
"	latitude_north DOUBLE,\n" +
"	longitude_west DOUBLE,\n" +
"	longitude_east DOUBLE,\n" +
"	name VARCHAR,\n" +
"	osm_type VARCHAR,\n" +
"	nga_designation VARCHAR,\n" +
"	country VARCHAR,   -- Normally two characters, but some have multiple countries separated by commas.\n" +
"	source VARCHAR,\n" +
"	PRIMARY KEY(entity_id, rank)\n" +
"	);\n" +
"\n" +
"CREATE INDEX on geolocation(rank);\n" +
"\n" +
"CREATE TABLE date (\n" +
"	entity_id INT,\n" +
"	canonical_text VARCHAR,\n" +
"	canonical_date DATE,\n" +
"	PRIMARY KEY (entity_id),\n" +
"	FOREIGN KEY(entity_id) REFERENCES entity(entity_id) ON DELETE CASCADE ON UPDATE CASCADE\n" +
"	);\n" +
"\n" +
"\n" +
"CREATE TABLE document (\n" +
"	document_id INT,\n" +
"	name VARCHAR,\n" +
"	path VARCHAR,\n" +
"	text CLOB,\n" +
"	PRIMARY KEY (document_id)\n" +
"	);\n" +
"\n" +
"CREATE INDEX on document(name);\n" +
"\n" +
"\n" +
"CREATE TABLE mention (\n" +
"	mention_id INT,\n" +
"	document_id INT NOT NULL,\n" +
"	entity_id INT,\n" +
"	type VARCHAR,\n" +
"	text VARCHAR,\n" +
"	index INT,\n" +
"       global_id INT,\n" +
"	text_start INT,\n" +
"	text_stop INT,\n" +
"	PRIMARY KEY (mention_id),\n" +
"	FOREIGN KEY (document_id) REFERENCES document(document_id) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
"	FOREIGN KEY (entity_id) REFERENCES entity(entity_id) ON DELETE CASCADE ON UPDATE CASCADE\n" +
"	);\n" +
"\n" +
"CREATE INDEX on mention(type);\n" +
"CREATE INDEX on mention(document_id);\n" +
"CREATE INDEX on mention(entity_id);\n" +
"\n" +
"\n" +
"CREATE TABLE document_entity (\n" +
"       document_id INT NOT NULL,\n" +
"       entity_id INT NOT NULL,\n" +
"       num_mentions INT,\n" +
"       PRIMARY KEY (document_id, entity_id),\n" +
"       FOREIGN KEY (document_id) REFERENCES document(document_id) ON DELETE CASCADE ON UPDATE CASCADE,\n" +
"       FOREIGN KEY (entity_id) REFERENCES entity(entity_id) ON DELETE CASCADE ON UPDATE CASCADE\n" +
"       );\n" +
"\n" +
"CREATE INDEX on document_entity(entity_id);\n" +
"";

}
