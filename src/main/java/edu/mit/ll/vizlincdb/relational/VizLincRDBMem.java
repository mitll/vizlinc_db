package edu.mit.ll.vizlincdb.relational;

import edu.mit.ll.vizlincdb.entity.EntityCounts;
import edu.mit.ll.vizlincdb.entity.Entity;
import edu.mit.ll.vizlincdb.entity.LocationEntity;
import edu.mit.ll.vizlincdb.entity.OrganizationEntity;
import edu.mit.ll.vizlincdb.entity.EntitySet;
import edu.mit.ll.vizlincdb.entity.PersonEntity;
import edu.mit.ll.vizlincdb.entity.DateEntity;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import edu.mit.ll.vizlincdb.document.Document;
import edu.mit.ll.vizlincdb.util.ElapsedTime;
import edu.mit.ll.vizlincdb.geo.GeoPoint;
import edu.mit.ll.vizlincdb.entity.Mention;
import edu.mit.ll.vizlincdb.entity.MentionLocation;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An API to hide the details of an RDB version of the VizLinc graph. Wraps a
 VizLincRDB object and prefetches most of the database for speed reasons.
 */
public class VizLincRDBMem {

    public VizLincRDB rdb;
    Kryo kryo;
    private EntitySet allEntities;
    private List<Document> allDocuments;
    private Map<Integer, Entity> entityIdToEntity;
    private Map<Integer, Document> documentIdToDocument;
    private Map<Integer, GeoPoint> entityIdToGeoPoint;
    // entityIdToDocumentIds could be subsumbed by entityIdToDocumentIdToMentionCount, but include it anyway in case the mention counts don'timer exist.
    private Map<Integer, HashSet<Integer>> entityIdToDocumentIds;
    private Map<Integer, HashSet<Integer>> documentIdToEntityIds;
    private Map<Integer, HashMap<Integer, Integer>> entityIdToDocumentIdToMentionCount;
    private Map<Integer, ArrayList<MentionLocation>> documentIdToMentionLocations;
    private ElapsedTime timer = new ElapsedTime();

    /**
     * Open an existing VizLinc database specified at the path specified by the
     * File (which will be a directory).
     *
     * @param databasePath
     */
    public VizLincRDBMem(File databasePath) throws SQLException {
        this(databasePath.getPath());
    }

    /**
     * Open an existing VizLinc database at the path specified (which will be a
     * directory).
     *
     * @param databasePath specifies a directory for the database.
     */
    public VizLincRDBMem(String databasePath) throws SQLException {
        rdb = new VizLincRDB(databasePath);
        kryo = new Kryo();

        // Prefetch data from the database.
        fetchDocuments(databasePath);
        fetchEntities(databasePath);
        fetchMentionLocations(databasePath);
        fetchGeoPoints(databasePath);
        fetchDocumentEntityAndMentionMaps(databasePath);
    }

    private void fetchDocuments(String databasePath) throws SQLException {
        timer.reset();
        File allDocumentsKyroFile = new File(databasePath, "allDocuments.kryo");
        allDocuments = (List<Document>) fetchKryo(allDocumentsKyroFile, ArrayList.class);
        if (allDocuments != null) {
            timer.done("fetch all documents from kryo");
        } else {
            allDocuments = rdb.getDocuments();
            timer.done("no kryo: fetch all documents from db");
            saveKryo(allDocumentsKyroFile, allDocuments);
            timer.done("save documents to kryo");
        }

        documentIdToDocument = new HashMap<Integer, Document>(allDocuments.size());
        for (Document doc : allDocuments) {
            documentIdToDocument.put(doc.getId(), doc);
        }
        timer.done("build document index");
    }

    private void fetchEntities(String databasePath) throws SQLException {
        timer.reset();
        File allEntitiesKryoFile = new File(databasePath, "allEntities.kryo");
        allEntities = (EntitySet) fetchKryo(allEntitiesKryoFile, EntitySet.class);
        if (allEntities != null) {
            timer.done("fetch all entities from kryo");
        } else {
            allEntities = rdb.getEntities();
            timer.done("no kryo: fetch all entities from db");
            saveKryo(allEntitiesKryoFile, allEntities);
            timer.done("save entities to kryo");
        }

        entityIdToEntity = new HashMap<Integer, Entity>(allEntities.size());

        for (Entity e : allEntities.getDateEntities()) {
            entityIdToEntity.put(e.getId(), e);
        }

        for (Entity e : allEntities.getLocationEntities()) {
            entityIdToEntity.put(e.getId(), e);
        }

        for (Entity e : allEntities.getOrganizationEntities()) {
            entityIdToEntity.put(e.getId(), e);
        }

        for (Entity e : allEntities.getPersonEntities()) {
            entityIdToEntity.put(e.getId(), e);
        }
        timer.done("index entities");
    }

    private void fetchMentionLocations(String databasePath) throws SQLException {
        timer.reset();
        File documentIdToMentionLocationsKryoFile = new File(databasePath, "documentIdToMentionLocations.kryo");
        File mentionTypeCodeTableKryoFile = new File(databasePath, "mentionTypeCodeTable.kryo");
        documentIdToMentionLocations = (HashMap<Integer, ArrayList<MentionLocation>>) fetchKryo(documentIdToMentionLocationsKryoFile, HashMap.class);
        Map<Short,String> mentionTypeCodeTable = (HashMap<Short,String>) fetchKryo(mentionTypeCodeTableKryoFile, HashMap.class);
        
        if (documentIdToMentionLocations != null && mentionTypeCodeTable != null) 
        {
            MentionLocation.setTypeCodeTable(mentionTypeCodeTable);
            timer.done("fetch docId->mentionLocations map from kryo");
        } else {
            // Get the mention locations from the db.
            List<MentionLocation> allMentionLocations = rdb.getMentionLocations();
            timer.done("fetch mention locations from db");

            // Count up how many mentions for each document, so that we may create ArrayLists of exactly the right size.
            Map<Integer, Integer> mentionCounts = new HashMap<Integer, Integer>(allMentionLocations.size());
            for (MentionLocation ml : allMentionLocations) {
                int documentId = ml.getDocumentId();
                if (!mentionCounts.containsKey(documentId)) {
                    mentionCounts.put(documentId, 0);
                }
                mentionCounts.put(documentId, mentionCounts.get(documentId) + 1);
            }

            // Set up the map of ArrayLists of the right size.
            documentIdToMentionLocations = new HashMap<Integer, ArrayList<MentionLocation>>(mentionCounts.size());
            for (int documentId : mentionCounts.keySet()) {
                documentIdToMentionLocations.put(documentId, new ArrayList<MentionLocation>(mentionCounts.get(documentId)));
            }

            // Now run through all the mention locations again, and save them by doc id.
            for (MentionLocation ml : allMentionLocations) {
                documentIdToMentionLocations.get(ml.getDocumentId()).add(ml);
            }
            timer.done("build docId->mentionLocations map");

            saveKryo(documentIdToMentionLocationsKryoFile, documentIdToMentionLocations);
            saveKryo(mentionTypeCodeTableKryoFile, MentionLocation.getTypeCodeTable());
            timer.done("save docId->mentionLocations map to kryo");

        }
    }

    private void fetchGeoPoints(String databasePath) throws SQLException {
        Statement stmt = rdb.conn.createStatement();
        ResultSet rs;
        File entityIdToGeoPointKryoFile = new File(databasePath, "entityIdToGeoPoint.kryo");
        timer.reset();
        entityIdToGeoPoint = (Map<Integer, GeoPoint>) fetchKryo(entityIdToGeoPointKryoFile, HashMap.class);
        if (entityIdToGeoPoint != null) {
            timer.done("fetch rank 0 geopoints for all location entities from kryo");
        } else {
            rs = stmt.executeQuery("SELECT latitude, longitude, entity_id FROM geolocation WHERE geolocation.rank = 0");
            entityIdToGeoPoint = new HashMap<Integer, GeoPoint>(allEntities.getLocationEntities().size());
            while (rs.next()) {
                GeoPoint gp = new GeoPoint(rs.getDouble(1), rs.getDouble(2), rs.getInt(3));
                entityIdToGeoPoint.put(gp.locationEntityId, gp);
            }
            timer.done("no kryo: fetch rank 0 geopoints for all location entities from db");
            saveKryo(entityIdToGeoPointKryoFile, entityIdToGeoPoint);
            timer.done("save rank 0 geopoints to kryo");
        }
    }

    private void fetchDocumentEntityAndMentionMaps(String databasePath) throws SQLException {
        ResultSet rs;

        timer.reset();
        Statement stmt = rdb.conn.createStatement();
        File documentIdToEntityIdsKryoFile = new File(databasePath, "documentIdToEntityIds.kryo");
        documentIdToEntityIds = (Map<Integer, HashSet<Integer>>) fetchKryo(documentIdToEntityIdsKryoFile, HashMap.class);
        File entityIdToDocumentIdsKryoFile = new File(databasePath, "entityIdToDocumentIds.kryo");
        entityIdToDocumentIds = (Map<Integer, HashSet<Integer>>) fetchKryo(entityIdToDocumentIdsKryoFile, HashMap.class);
        File entityIdToDocumentIdToMentionCountKryoFile = new File(databasePath, "entityIdToDocumentIdToMentionCount.kryo");
        entityIdToDocumentIdToMentionCount = (Map<Integer, HashMap<Integer, Integer>>) fetchKryo(entityIdToDocumentIdToMentionCountKryoFile, HashMap.class);
        if (documentIdToEntityIds != null && entityIdToDocumentIds != null && entityIdToDocumentIdToMentionCount != null) {
            timer.done("fetch document<->entity and document->entity->mention_count maps from kryo");
        } else {
            documentIdToEntityIds = new HashMap<Integer, HashSet<Integer>>();
            entityIdToDocumentIds = new HashMap<Integer, HashSet<Integer>>();
            entityIdToDocumentIdToMentionCount = new HashMap<Integer, HashMap<Integer, Integer>>();
            rs = stmt.executeQuery("SELECT document_id, entity_id, num_mentions FROM document_entity");
            while (rs.next()) {
                int documentId = rs.getInt(1);
                int entityId = rs.getInt(2);
                int numMentionLocations = rs.getInt(3);

                HashSet<Integer> entityIdSet = documentIdToEntityIds.get(documentId);
                if (entityIdSet == null) {
                    entityIdSet = new HashSet<Integer>();
                }
                entityIdSet.add(entityId);
                documentIdToEntityIds.put(documentId, entityIdSet);

                HashSet<Integer> documentIdSet = entityIdToDocumentIds.get(entityId);
                if (documentIdSet == null) {
                    documentIdSet = new HashSet<Integer>();
                }
                documentIdSet.add(documentId);
                entityIdToDocumentIds.put(entityId, documentIdSet);

                HashMap<Integer, Integer> documentIdToMentionCount = entityIdToDocumentIdToMentionCount.get(entityId);
                if (documentIdToMentionCount == null) {
                    documentIdToMentionCount = new HashMap<Integer, Integer>();
                }
                documentIdToMentionCount.put(documentId, numMentionLocations);
                entityIdToDocumentIdToMentionCount.put(entityId, documentIdToMentionCount);
            }

            timer.done("build document<->entity and document->entity->mention_count maps from db");
            saveKryo(documentIdToEntityIdsKryoFile, documentIdToEntityIds);
            saveKryo(entityIdToDocumentIdsKryoFile, entityIdToDocumentIds);
            timer.done("save kryo document<->entity maps");

            saveKryo(entityIdToDocumentIdToMentionCountKryoFile, entityIdToDocumentIdToMentionCount);
            timer.done("save kryo ocument->entity->mention_count map");
        }
    }

    /**
     * Fetch a serialized object from a file, if it exists; if not, return null
     *
     * @param serFile
     * @return null or the object
     */
    private Object fetchSerialized(File serFile) {
        if (serFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(serFile);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                Object obj = in.readObject();
                in.close();
                fileIn.close();
                return obj;
            } catch (Exception ex) {
                System.err.println("unexpected fetchSerialized error: " + ex.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Fetch a serialized object from a file, if it exists; if not, return null
     *
     * @param kryoFile
     * @return null or the object
     */
    private Object fetchKryo(File kryoFile, Class cls) {
        if (kryoFile.exists()) {
            try {
                Input input = new Input(new FileInputStream(kryoFile));
                Object obj = kryo.readObject(input, cls);
                input.close();
                return obj;
            } catch (Exception ex) {
                System.err.println("unexpected fetchKryo error: " + ex.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Save a serialized object into a file.
     *
     * @param serFile
     */
    private void saveSerialized(File serFile, Object obj) {
        try {
            FileOutputStream fileOut = new FileOutputStream(serFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(obj);
            out.close();
            fileOut.close();
        } catch (Exception ex) {
            System.err.println("unexpected saveSerialized error: " + ex);
        }
    }

    /**
     * Save a serialized object into a file.
     *
     * @param kryoFile
     */
    private void saveKryo(File kryoFile, Object obj) {
        try {
            Output output = new Output(new FileOutputStream(kryoFile));
            kryo.writeObject(output, obj);
            output.close();
        } catch (Exception ex) {
            System.err.println("unexpected saveKryo error: " + ex);
        }
    }

    /**
     * Provide an explicit shutdown operation in case it needs to be done before
     * the JVM shuts down.
     */
    public void shutdown() throws SQLException {
        rdb.shutdown();
    }

    /**
     * Return all the document nodes in the graph.
     *
     * @return all the document nodes
     */
    public List<Document> getDocuments() {
        return allDocuments;
    }

    /**
     * Return the single document with the given id; return null if not found.
     *
     * @param documentId
     * @return Document or null
     */
    public Document getDocumentWithId(int documentId) {
        return documentIdToDocument.get(documentId);
    }

    /**
     * Return a List of all the documents with the given ids.
     *
     * @param documentIds List of document ids
     * @return List of Documents
     */
    public List<Document> getDocumentsWithIds(List<Integer> documentIds) {
        List<Document> documents = new ArrayList<Document>();
        for (int id : documentIds) {
            documents.add(getDocumentWithId(id));
        }
        return documents;
    }

    /**
     * Return the single entity with the given id; return null if not found.
     *
     * @param entityId
     * @return a subclass of Entity or null
     */
    public Entity getEntityWithId(int entityId) {
        return entityIdToEntity.get(entityId);
    }

    /**
     * Return all the entities.
     *
     * @return EntitySet
     */
    public EntitySet getEntities() {
        return allEntities;
    }

    /**
     * Return a List of all the entities with the given ids.
     *
     * @param entityIds List of entity ids
     * @return EntitySet
     * @throws SQLException
     */
    public EntitySet getEntitiesWithIds(List<Integer> entityIds) throws SQLException {
        EntitySet entities = new EntitySet();
        for (int id : entityIds) {
            entities.add(getEntityWithId(id));
        }
        return entities;
    }

    /**
     * Return the mention with the given mentionId
     *
     * @param mentionId
     * @return
     */
    Mention getMentionWithId(int mentionId) throws SQLException {
        return rdb.getMentionWithId(mentionId);
    }

    /**
     * Return the document's text
     *
     * @param doc a Document
     * @return null if not a document, otherwise the text as a String
     * @throws SQLException
     */
    public String getDocumentText(Document doc) throws SQLException {
        return rdb.getDocumentText(doc);
    }
    
    /**
     * Return the document's text, given the document id.
     * @param documentId
     * @return null if not a document, otherwise the text as a String
     * @throws SQLException 
     */
    public String getDocumentText(int documentId) throws SQLException {
        return rdb.getDocumentText(documentId);
    }

    /**
     * Return the rank 0 geoPoint for each given location entity.
     *
     * @param locationEntityIds
     * @return a List of GeoPoints, in one-to-one correspondence with the
     * locationEntityIds
     * @throws SQLException
     */
    public List<GeoPoint> getTopGeoPointsForLocationEntityIds(List<Integer> locationEntityIds) throws SQLException {
        List<GeoPoint> geoPoints = new ArrayList<GeoPoint>(locationEntityIds.size());
        for (int id : locationEntityIds) {
            geoPoints.add(entityIdToGeoPoint.get(id));
        }
        return geoPoints;
    }

    /**
     * Return a List of all the person entities in the database.
     *
     * @return person entities
     */
    public List<PersonEntity> getPersonEntities() {
        return allEntities.getPersonEntities();
    }

    /**
     * Return a List of all the location entities in the database.
     *
     * @return location entities
     */
    public List<LocationEntity> getLocationEntities() {
        return allEntities.getLocationEntities();
    }

    /**
     * Return a List of all the date entities in the database.
     *
     * @return date entities
     */
    public List<DateEntity> getDateEntities() {
        return allEntities.getDateEntities();
    }

    /**
     * Return a List of all the organization entities in the database.
     *
     * @return the organization entities
     */
    public List<OrganizationEntity> getOrganizationEntities() {
        return allEntities.getOrganizationEntities();
    }

    /**
     * Return all the documents (designated by ids) which refer to ALL of the
     * given entities (designated by id).
     *
     * @param entityIds
     * @return List of document ids
     */
    public List<Integer> getDocumentIdsWithAllOfTheseEntityIds(List<Integer> entityIds) {
        // First get all the documents that have any of these ids.
        Set<Integer> possibleDocumentIds = new HashSet<Integer>();
        for (int entityId : entityIds) {
            possibleDocumentIds.addAll(entityIdToDocumentIds.get(entityId));
        }

        // Now revisit all the documents and choose the ones that have all the given entity ids.
        Set<Integer> entityIdsSet = new HashSet(entityIds);
        List<Integer> matchingDocumentIds = new ArrayList<Integer>();
        for (int docId : possibleDocumentIds) {
            if (documentIdToEntityIds.get(docId).containsAll(entityIdsSet)) {
                matchingDocumentIds.add(docId);
            }
        }

        return matchingDocumentIds;
    }

    /**
     * Return all the unique mention texts for a given entity.
     *
     * @param entityId
     * @return List of unique strings
     * @throws SQLException
     */
    public List<String> getDistinctMentionTextsForEntityId(int entityId) throws SQLException {
        return rdb.getDistinctMentionTextsForEntityId(entityId);
    }

   /**
    * Return all the unique mention texts in a given document for a given entity
    * @param entityId
    * @return List of unique strings
    * @throws SQLException
    */
    public List<String> getDistinctMentionTextsForEntityIdInDocument(int entityId, int documentId) throws SQLException {
        return rdb.getDistinctMentionTextsForEntityIdInDocument(entityId, documentId);
    }

            /**
     * Return the ids for all the entities in the given set of documents
     * (designated by document id)
     *
     * @param docIds
     * @return List of entity ids
     * @throws SQLException
     */
    public List<Integer> getEntityIdsInAnyofTheseDocumentIds(List<Integer> docIds) throws SQLException {
        Set<Integer> entityIdsSet = new HashSet<Integer>();
        // Take the union of all the entities for the given docs.
        for (int docId : docIds) {
            
            Set<Integer> eIds = documentIdToEntityIds.get(docId);
            if(eIds != null)
            {
                entityIdsSet.addAll(eIds);
            }
        }
        return new ArrayList<Integer>(entityIdsSet);
    }

    /**
     * Return the number of mentions in the given documents for each of the
     * given entities, given a subset of documents.
     *
     * @param entityIds
     * @param docIds
     * @return map containing the entity id as key and the mention count for the
     * set of documents as value
     */
    public Map<Integer, Integer> getMentionCountsForEntitiesInDocuments(Collection<Integer> entityIds, Collection<Integer> docIds) throws SQLException {
        // Put the docIds in a set for fast lookup.
        Set<Integer> docIdsSet = new HashSet<Integer>(docIds);

        // Create map for returned values.
        Map<Integer, Integer> entityIdToMentionCount = new HashMap<Integer, Integer>();

        // For all the given entities, get the document->mention_count maps and add up all the mention counts for the given docs.
        for (int entityId : entityIds) {
            int totalMentionCountForEntity = 0;
            Map<Integer, Integer> documentIdToMentionCount = entityIdToDocumentIdToMentionCount.get(entityId);
            for (int docId : documentIdToMentionCount.keySet()) {
                if (docIdsSet.contains(docId)) {
                    totalMentionCountForEntity += documentIdToMentionCount.get(docId);
                }
            }
            entityIdToMentionCount.put(entityId, totalMentionCountForEntity);
        }
        return entityIdToMentionCount;
    }

    /**
     * Return the number of documents that have each of the given entities,
     * given a subset of documents.
     *
     * @param docIds
     * @param entityIds
     * @return map containing the entity id as key and the documents count for
     * the given set of documents
     */
    public Map<Integer, Integer> getDocumentCountsForEntitiesInDocuments(Collection<Integer> entityIds, Collection<Integer> docIds) {
        // Put the docIds in a set for fast lookup.
        Set<Integer> docIdsSet = new HashSet<Integer>(docIds);

        // Create map for returned values.
        Map<Integer, Integer> entityIdToDocumentCount = new HashMap<Integer, Integer>();

        for (int entityId : entityIds) {
            int docCount = 0;
            for (int docId : entityIdToDocumentIds.get(entityId)) {
                if (docIdsSet.contains(docId)) {
                    docCount++;
                }
            }
            entityIdToDocumentCount.put(entityId, docCount);
        }
        return entityIdToDocumentCount;
    }

    /**
     * Choose which kind of neighborhood to use for
     * getEntitiesMentionedNearEntitiesBy*().
     */
    protected enum NeighborhoodType {

        BY_INDEX, BY_TEXT_START
    }

    /**
     * Given a (usually small) set of query or "target" entity ids, look up all
     * the mentions of those entities in the given set of doc ids, and return a
     * a histogram of all the entities whose mentions are within plus or minus
     * the given distance. Restrict the set of entities return to those in
     * wantedEntityIds. So for instance, the distance is 5, and if entity A is
     * mentioned at positions 10 and 22 in a document, return all the entities
     * mentioned within positions [5,15] and [17,27] in that document. Use
     * MentionLocation#index for the distance measure.
     *
     * @param queryEntityIds
     * @param wantedEntityIds
     * @param docIds
     * @param distance
     * @return Map<entity id, EntityCounts counts>
     */
    public Map<Integer, EntityCounts> getEntitiesMentionedNearEntitiesByIndex(Collection<Integer> queryEntityIds, Collection<Integer> wantedEntityIds, Collection<Integer> docIds, int distance) {
        return getEntitiesMentionedNearEntities(queryEntityIds, wantedEntityIds, docIds, distance, NeighborhoodType.BY_INDEX);
    }

    /**
     * Given a (usually small) set of query or "target" entity ids, look up all
     * the mentions of those entities in the given set of doc ids, and return a
     * a histogram of all the entities whose mentions are within plus or minus
     * the given distance. Restrict the set of entities return to those in
     * wantedEntityIds. So for instance, the distance is 5, and if entity A is
     * mentioned at positions 10 and 22 in a document, return all the entities
     * mentioned within positions [5,15] and [17,27] in that document. Use
     * MentionLocation#textOffset for the distance measure.
     *
     * @param queryEntityIds
     * @param wantedEntityIds
     * @param docIds
     * @param distance
     * @return Map<entity id, EntityCounts counts>
     */
    public Map<Integer, EntityCounts> getEntitiesMentionedNearEntitiesByTextOffset(Collection<Integer> queryEntityIds, Collection<Integer> wantedEntityIds, Collection<Integer> docIds, int distance) {
        return getEntitiesMentionedNearEntities(queryEntityIds, wantedEntityIds, docIds, distance, NeighborhoodType.BY_TEXT_START);
    }

    /**
     * Common routine to implement getEntitiesMentionedNearEntitiesByIndex and
     * getEntitiesMentionedNearEntitiesByTextOffset.
     */
    protected Map<Integer, EntityCounts> getEntitiesMentionedNearEntities(Collection<Integer> queryEntityIds, Collection<Integer> wantedEntityIds, Collection<Integer> docIds, int distance, NeighborhoodType neighborhoodType) {
        // Make sets for fast lookups.
        Set<Integer> queryEntityIdsSet = new HashSet<Integer>(queryEntityIds);
        Set<Integer> wantedEntityIdsSet = new HashSet<Integer>(wantedEntityIds);
        Map<Integer, RangeSet<Integer>> docIdToMentionRanges = new HashMap<Integer, RangeSet<Integer>>(docIds.size());

        // The map for the return value.
        Map<Integer, EntityCounts> entityIdToCounts = new HashMap<Integer, EntityCounts>();

        // Set up docIdToMentionRanges with empty TreeRangeSets.
        for (int docId : docIds) {
            RangeSet<Integer> mentionRanges = TreeRangeSet.create();
            // Compiler complains if TreeRangeSet.create() is second arg. Odd.
            docIdToMentionRanges.put(docId, mentionRanges);
        }

        // Build up the range map of mention locations based on the given set of doc ids and query entity ids.
        for (int docId : docIds) {
            for (MentionLocation ml : documentIdToMentionLocations.get(docId)) {
                if (queryEntityIdsSet.contains(ml.getEntityId())) {
                    int pos = 0;
                    switch (neighborhoodType) {
                        case BY_INDEX:
                            pos = ml.getIndex();
                            break;
                        case BY_TEXT_START:
                            pos = ml.getTextStart();
                            break;
                    }
                    docIdToMentionRanges.get(docId).add(Range.closed(pos - distance, pos + distance).canonical(DiscreteDomain.integers()));
                }
            }
        }

        // Now look at all the mentions for each document and see if their positions are inside the range set. Cull out any mentions for entities we're not interested in.
        for (int docId : docIds) {
            // Accumulate the mention counts for the current document.
            Map<Integer, Integer> entityIdToMentionCount = new HashMap<Integer, Integer>();
            for (MentionLocation ml : documentIdToMentionLocations.get(docId)) {
                int entityId = ml.getEntityId();
                int pos = 0;
                switch (neighborhoodType) {
                    case BY_INDEX:
                        pos = ml.getIndex();
                        break;
                    case BY_TEXT_START:
                        pos = ml.getTextStart();
                        break;
                }
                if (wantedEntityIdsSet.contains(entityId) && docIdToMentionRanges.get(docId).contains(pos)) {
                    // This is a mention in the desired entity set, within range. Remember it and increment its count.
                    Integer mentionCount = entityIdToMentionCount.get(entityId);
                    if (mentionCount == null) {
                        entityIdToMentionCount.put(entityId, 1);
                    } else {
                        entityIdToMentionCount.put(entityId, mentionCount + 1);
                    }
                }
            }

            // Now accumulate the mention counts for this doc in the counts for all docs.
            for (int entityId : entityIdToMentionCount.keySet()) {
                int mentionCount = entityIdToMentionCount.get(entityId);
                EntityCounts counts = entityIdToCounts.get(entityId);
                if (counts == null) {
                    entityIdToCounts.put(entityId, new EntityCounts(mentionCount, 1));  /* 1 document */
                } else {
                    counts.mentionCount += mentionCount;
                    counts.documentCount++;
                }
            }
        }

        return entityIdToCounts;
    }


    /**
     * Given a (usually small) set of query or "target" entity ids, return all
     * the documents which have mentions of those entities near another given
     * entity. Start with a given set of documents. Use MentionLocation#index
     * for the distance measure.
     *
     * @param queryEntityIds
     * @param wantedEntityIds
     * @param docIds
     * @param distance
     * @return Set<Integer> a set of document ids
     */
    public Set<Integer> getDocumentsForEntitiesMentionedNearEntityByIndex(Collection<Integer> queryEntityIds, int wantedEntityId, Collection<Integer> docIds, int distance) {
        return getDocumentsForEntitiesMentionedNearEntity(queryEntityIds, wantedEntityId, docIds, distance, NeighborhoodType.BY_INDEX);
    }

    /**
     * Given a (usually small) set of query or "target" entity ids, return all
     * the documents which have mentions of those entities near another given
     * entity. Start with a given set of documents. Use
     * MentionLocation#textOffset for the distance measure.
     *
     * @param queryEntityIds
     * @param wantedEntityIds
     * @param docIds
     * @param distance
     * @return Set<Integer> a set of document ids
     */
    public Set<Integer> getDocumentsForEntitiesMentionedNearEntityByTextOffset(Collection<Integer> queryEntityIds, int wantedEntityId, Collection<Integer> docIds, int distance) {
        return getDocumentsForEntitiesMentionedNearEntity(queryEntityIds, wantedEntityId, docIds, distance, NeighborhoodType.BY_TEXT_START);
    }

    /**
     * Common routine to implement getDocumentsForForEntitiesMentionedNearEntityByIndex and
     * getDocumentsForEntitiesMentionedNearEntityByTextOffset.
     */
    protected Set<Integer> getDocumentsForEntitiesMentionedNearEntity(Collection<Integer> queryEntityIds, int wantedEntityId, Collection<Integer> docIds, int distance, NeighborhoodType neighborhoodType) {
        // Make sets for fast lookups.
        Set<Integer> queryEntityIdsSet = new HashSet<Integer>(queryEntityIds);
        Map<Integer, RangeSet<Integer>> docIdToMentionRanges = new HashMap<Integer, RangeSet<Integer>>(docIds.size());

        // Return value.
        Set<Integer> matchingDocIds = new HashSet<Integer>();

        // Set up docIdToMentionRanges with empty TreeRangeSets.
        for (int docId : docIds) {
            RangeSet<Integer> mentionRanges = TreeRangeSet.create();
            // Compiler complains if TreeRangeSet.create() is second arg. Odd.
            docIdToMentionRanges.put(docId, mentionRanges);
        }

        // Build up the range map of mention locations based on the given set of doc ids and query entity ids.
        for (int docId : docIds) {
            for (MentionLocation ml : documentIdToMentionLocations.get(docId)) {
                if (queryEntityIdsSet.contains(ml.getEntityId())) {
                    int pos = 0;
                    switch (neighborhoodType) {
                        case BY_INDEX:
                            pos = ml.getIndex();
                            break;
                        case BY_TEXT_START:
                            pos = ml.getTextStart();
                            break;
                    }
                    docIdToMentionRanges.get(docId).add(Range.closed(pos - distance, pos + distance).canonical(DiscreteDomain.integers()));
                }
            }
        }

        // Now look at all the mentions for each document and see if their positions are inside the range set. Cull out any mentions for entities we're not interested in.
        for (int docId : docIds) {
            // Accumulate the mention counts for the current document.
            Map<Integer, Integer> entityIdToMentionCount = new HashMap<Integer, Integer>();
            for (MentionLocation ml : documentIdToMentionLocations.get(docId)) {
                int entityId = ml.getEntityId();
                int pos = 0;
                switch (neighborhoodType) {
                    case BY_INDEX:
                        pos = ml.getIndex();
                        break;
                    case BY_TEXT_START:
                        pos = ml.getTextStart();
                        break;
                }
                if (wantedEntityId == entityId && docIdToMentionRanges.get(docId).contains(pos)) {
                    // Remember that this document contains an mention for anentity in queryEntityIdsSet that's near a mention of wantedEntityId.
                    matchingDocIds.add(docId);
                    // Once is enough. Skip to the next doc.
                    break;
                }
            }
        }

        return matchingDocIds;
    }
    
    public List<MentionLocation> getMentionLocationsForDocument(int docId)
    {
        List<MentionLocation> mLocs = this.documentIdToMentionLocations.get(docId);
        if(mLocs == null)
        {
            return new LinkedList<>();
        }
        return mLocs;
    }
    
    public List<MentionLocation> getMentionLocationsForEntitiesIdInDocument(List<Integer> entityIds, int documentId)
    {
        List<MentionLocation> result = new LinkedList<>();
        List<MentionLocation> allMLocs = this.documentIdToMentionLocations.get(documentId);
        
        //Create entity hash
        Set<Integer> entitySet = new HashSet<>();
        for(Integer id: entityIds)
        {
            entitySet.add(id);
        }
        
        //Make a pass over all locations and extract those that correspond to the entities of interest
        for(MentionLocation mL : allMLocs)
        {
            int id = mL.getEntityId();
            if(entitySet.contains(id))
            {
                result.add(mL);
            }
        }
        
        return result;
    }
}