package edu.mit.ll.vizlincdb.graph;

import com.google.common.primitives.Doubles;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import edu.mit.ll.vizlincdb.geo.GeoBoundingBox;
import edu.mit.ll.vizlincdb.geo.GeoLocation;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * An API to hide the details of Blueprints graph manipulation for the VizLinc graph. The terms "node" and "vertex",
 * capitalized or not, are used interchangeably.
 */
public class VizLincDB {

    public enum Implementation {
        TINKERGRAPH(TinkerGraph.class, new String[] {"tg", "tinker"}),
        NEO4J(Neo4jGraph.class, new String[] {"neo4j"});

        public static Implementation DEFAULT = NEO4J;

        public final Class cls;
        public final String[] tags;

        Implementation(Class cls, String[] tags) {
            this.cls = cls;
            this.tags = tags;
        }

        /**
         * Return an Implementation that matches a given database path, e.g. NEO4J for "100docs-2013-01-29.neo4j.per".
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

    public KeyIndexableGraph graph;
    /** The same object as graph, but will be null if graph is not transactional. Then commit becomes a no-op and rollback is not implemented. */
    TransactionalGraph transactionalGraph;

    /**
     * Open an existing VizLinc database or create a new one at the path specified by the File (which will be a directory).
     *
     * @param databasePath specifies a directory which does not need to exist already (though the containing directory
     * should exist).
     * @param newDB true if a new database should be created. Will throw IllegalArgument exception if database exists
     * and newDB is true, or database is not present and newDB is false.
     */
    public VizLincDB(File databasePath, boolean newDB) {
        this(databasePath.getPath(), newDB);
    }

    /**
     * Open an existing VizLinc database specified at the path specified by the File (which will be a directory).
     *
     * @param databasePath
     */
    public VizLincDB(File databasePath) {
        this(databasePath.getPath(), false);
    }

     /**
     * Open an existing VizLinc database or create a new one at the path specified (which will be a directory).
     *
     * @param databasePath specifies a directory for the database. The implementation chosen is based on what's in the path
     * e.g., "abc.neo4j.foo" will open a Neo4j database.
     * @param newDB true if a new database should be created. Will throw IllegalArgument exception if a non-empty directory exists
     * and newDB is true, or if directory does not exists or is empty and newDB is false.
     * should exist).
     */
    public VizLincDB(String databasePath, boolean newDB) {
        this(databasePath, newDB, Implementation.implementationForPath(databasePath));
    }

     /**
     * Open an existing VizLinc database or create a new one at the path specified (which will be a directory).
     *
     * @param databasePath specifies a directory for the database.
     * @param newDB true if a new database should be created. Will throw IllegalArgument exception if a non-empty directory exists
     * and newDB is true, or if directory does not exists or is empty and newDB is false.
     * should exist).
     * @param impl which graphdb is used to implement this VizLincDB.
     */
    public VizLincDB(File databasePath, boolean newDB, Implementation impl) {
        this(databasePath.getPath(), newDB, impl);
    }

     /**
     * Open an existing VizLinc database or create a new one at the path specified (which will be a directory).
     *
     * @param databasePath specifies a directory for the database.
     * @param newDB true if a new database should be created. Will throw IllegalArgument exception if a non-empty directory exists
     * and newDB is true, or if directory does not exists or is empty and newDB is false.
     * should exist).
     * @param impl which graphdb is used to implement this VizLincDB.
     */
    public VizLincDB(String databasePath, boolean newDB, Implementation impl) {
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
            case TINKERGRAPH:
                // TinkerGraph assumes there is data if the directory exists, so remove it if it's supposed to be new.
                if (newDB & dir.isDirectory()) {
                    dir.delete();
                }
                TinkerGraph tinkerGraph = new TinkerGraph(databasePath);
                graph = tinkerGraph;
                transactionalGraph = null;
                break;

            case NEO4J:
                Neo4jGraph neo4jGraph = new Neo4jGraph(databasePath);
                graph = neo4jGraph;
                transactionalGraph = neo4jGraph;
                break;
        }

        // Make sure the database gets written out when the JVM quits.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (graph != null) {
                    graph.shutdown();
                }
            }
        });
    }

     /**
     * Open an existing VizLinc database at the path specified (which will be a directory).
     *
     * @param databasePath
     */
    public VizLincDB(String databasePath) {
        this(databasePath, false);
    }

    /** True if the graph in use is transactional. */
    public boolean isTransactional() {
        return transactionalGraph != null;
    }


    /**
     * Commit a transaction in progress.
     */
    public void commit() {
        if (isTransactional()) {
            transactionalGraph.commit();
        }
    }

    /**
     * Roll back a transaction in progress, discarding any changes made.
     */
    public void rollback() {
        if (!isTransactional()) {
            throw new UnsupportedOperationException("graph type being used does not support rollback");
        }
        transactionalGraph.rollback();
    }

    /**
     * Provide an explicit shutdown operation in case it needs to be done before the JVM shuts down.
     */
    public void shutdown() {
        commit();
        graph.shutdown();
        graph = null;
    }

    /**
     * Delete the given node (vertex) from the graph. All the connecting edges are removed automatically.
     *
     * @param node node, could be a mention, an entity, etc.
     */
    public void deleteNode(Vertex node) {
        graph.removeVertex(node);
    }

    /**
     * Return the node (Vertex) with the given Id.
     *
     * @param Id an Object (Neo4j uses integers)
     * @return the node
     */
    public Vertex getNodeWithId(Object Id) {
        return graph.getVertex(Id);
    }

    /**
     * Return the edge with the given Id.
     *
     * @param Id an Object (Neo4j uses integers)
     * @return the edge
     */
    public Edge getEdgeWithId(Object Id) {
        return graph.getEdge(Id);
    }

    /**
     * Return all the document nodes in the graph.
     *
     * @return all the document nodes
     */
    public Iterable<Vertex> getDocuments() {
        return graph.getVertices(P_NODE_TYPE, NODE_TYPE_DOCUMENT);
    }

    /**
     * Return all the mention nodes in the graph.
     *
     * @return all the mention nodes
     */
    public Iterable<Vertex> getMentions() {
        return graph.getVertices(P_NODE_TYPE, NODE_TYPE_MENTION);
    }

    /**
     * Return all the mention nodes of the given entity type in the graph.
     *
     * @param mention_type String representing the type: PERSON, LOCATION, etc.
     * @return the selected mention nodes
     */
    public Iterable<Vertex> getMentionsOfType(String mention_type) {
        return graph.getVertices(P_MENTION_TYPE, mention_type);
    }

    /**
     * Return all the edges with the given label.
     *
     * @return all the "mention" edges
     */
    public Iterable<Edge> getEdges(String label) {
        // TODO: getEdges("label", label) doesn't work yet for Neo4jGraph or TinkerGraph, so build a pipeline.
        // See https://github.com/tinkerpop/blueprints/issues/328.
        // return graph.getEdges("label", label);
        return new GremlinPipeline(graph.getEdges()).has("label", label);
    }

    /**
     * Return true if an out edge with the given label exists from vertex v1 to v2.
     * @param v1
     * @param v2
     * @param label  with label
     * @return  true if an out edge exists from v1 to v2 with the given label
     */
    public boolean outEdgeExists(Vertex v1, Vertex v2, String label) {
        return new GremlinPipeline(v1).outE(label).inV().retain(Collections.singletonList(v2)).hasNext();
    }

    /**
     * Return true if edge with the given label exists from vertex v1 to v2, in either direction
     * @param v1
     * @param v2
     * @param label  with label
     * @return  true if an edge exists from v1 to v2 with the given label
     */
    public boolean edgeExists(Vertex v1, Vertex v2, String label) {
        return new GremlinPipeline(v1).bothE(label).bothV().retain(Collections.singletonList(v2)).hasNext();
    }

    /**
     * Return true if v1 connects with an in edge to all nodes in v2Iterable with edges of the given label.
     * @param v1   start here
     * @param v2Iterable   v1 must be pointed to by all of these
     * @param label   edges must be of label
     * @return   true if v1 points to all of v2Iterable with edges of the given label.
     */
    public boolean allInEdgesExist(Vertex v1, Iterable<Vertex>v2Iterable, String label) {
        List<Vertex> v2List = makeList(v2Iterable);
        return new GremlinPipeline(v1).inE(label).outV().retain(v2List).dedup().count() == v2List.size();
     }

    /**
     * Return true if v1 connects with an out edge to all nodes in v2Iterable with edges of the given label.
     * @param v1   start here
     * @param v2Iterable   v1 must point to all of these
     * @param label   edges must be of label
     * @return   true if v1 points to all of v2Iterable with edges of the given label.
     */
    public boolean allOutEdgesExist(Vertex v1, Iterable<Vertex>v2Iterable, String label) {
        List<Vertex> v2List = makeList(v2Iterable);
        return new GremlinPipeline(v1).outE(label).inV().retain(v2List).dedup().count() == v2List.size();
     }

    /**
     * Return true if v1 connects with any edge to all nodes in v2Iterable with edges of the given label.
     * @param v1   start here
     * @param v2Iterable   v1 must point to all of these
     * @param label   edges must be of label
     * @return   true if v1 points to all of v2Iterable with edges of the given label.
     */
    public boolean allEdgesExist(Vertex v1, Iterable<Vertex>v2Iterable, String label) {
        List<Vertex> v2List = makeList(v2Iterable);
        return new GremlinPipeline(v1).bothE(label).bothV().retain(v2List).dedup().count() == v2List.size();
     }

    /**
     * Return all the nodes in the graph that are named entities.
     *
     * @return all the named-entity nodes.
     */
    public Iterable<Vertex> getEntities() {
        return graph.getVertices(P_NODE_TYPE, NODE_TYPE_ENTITY);
    }

    /**
     * Return all the nodes in the graph that are named entities of the given type.
     *
     * @param entity_type String representing the entity_type: PERSON, LOCATION, etc.
     * @return the selected nodes
     */
    public Iterable<Vertex> getEntitiesOfType(String entity_type) {
        return graph.getVertices(P_ENTITY_TYPE, entity_type);
    }

    /**
     * Return all the entity nodes with the given createdBy property value.
     *
     * @param createdBy P_CREATED_BY must be equal to this value
     * @return the entity nodes with the P_CREATED_BY property == createdBy
     */
    public Iterable<Vertex> getEntitiesCreatedBy(String createdBy) {
        return graph.getVertices(P_CREATED_BY, createdBy);
    }

    /**
     * Return the document in which a mention occurs.
     *
     * @param mention Vertex
     * @return the document
     * @throws VizLincDBException if the mention is not connected to exactly one document
     */
    public Vertex getDocumentForMention(Vertex mention) throws VizLincDBException {
        List<Vertex> documents = new GremlinPipeline(mention).in(L_DOCUMENT_TO_MENTION).toList();
        if (documents.size() != 1) {
            throw new VizLincDBException("a mention should be connected to exactly one document");
        }
        return documents.get(0);
    }

    /**
     * Return all the mentions corresponding to a given entity.
     *
     * @param entity the entity representing the mentions
     * @return all the mentions represented by the entity
     */
    public Iterable<Vertex> getMentionsForEntity(Vertex entity) {
        return entity.getVertices(Direction.IN, L_MENTION_TO_ENTITY);
    }

    /**
     * Return the documents that contain any of the given mentions.
     *
     * @param mentions an arbitrary set of mention nodes
     * @return an Iterable for the document nodes.
     */
    public Iterable<Vertex> getDocumentsWithMentions(Iterable<Vertex> mentions) {
        // dedup() because there may be multiple mentions in the same document.
        return new GremlinPipeline(mentions).inE(L_DOCUMENT_TO_MENTION).outV().dedup();
    }

    /**
     * Return all the mentions in (referred to) by the given document.
     *
     * @param documentNode the document containing the mentions
     * @return the mention nodes
     */
    public Iterable<Vertex> getMentionsInDocument(Vertex documentNode) {
        return documentNode.getVertices(Direction.OUT, L_DOCUMENT_TO_MENTION);
    }


    /**
     * Return all the mentions with the give type in the document (e.g., all the PERSON mentions).
     * @param documentNode  the document containing the mentions
     * @param mention_type  the type the mentions must match.
     * @return  the matching mention nodes.
     */
    public Iterable<Vertex> getMentionsInDocumentOfType(Vertex documentNode, String mention_type) {
        return new GremlinPipeline(documentNode).out(L_DOCUMENT_TO_MENTION).has(P_MENTION_TYPE, mention_type);
    }

    /**
     * Return all the entities referred to by the given document
     *
     * @param documentNode the document containing the entities
     * @return the entity nodes
     */
    public Iterable<Vertex> getEntitiesInDocument(Vertex documentNode) {
        return documentNode.getVertices(Direction.OUT, L_DOCUMENT_TO_ENTITY);
    }

    /**
     * Return all the documents which mention a given entity.
     *
     * @param entityNode the entity referred to by the documents
     * @return the document nodes
     */
    public Iterable<Vertex> getDocumentsForEntity(Vertex entityNode) {
        return entityNode.getVertices(Direction.IN, L_DOCUMENT_TO_ENTITY);
    }

    /**
     * Return the set of  documents that contain any of the given entities.
     *
     * @param entities an arbitrary set of entity nodes
     * @return an Iterable for the document nodes.
     */
    public Iterable<Vertex> getDocumentsForAnyEntities(Iterable<Vertex> entities) {
        return new GremlinPipeline(entities).inE(L_DOCUMENT_TO_ENTITY).outV().dedup();
    }

     /**
     * Return the documents that contain all of the given mentions.
     *
     * @param entities an arbitrary set of entity nodes
     * @return an Iterable for the document nodes.
     */
    public Iterable<Vertex> getDocumentsWithAllOfTheseEntities(Iterable<Vertex> entities) {
        List<Vertex> entitiesList = makeList(entities);
        List<Vertex> docsWithAllEntities = new ArrayList<Vertex>();
        for (Vertex doc : getDocumentsForAnyEntities(entitiesList)) {
            if (allOutEdgesExist(doc, entitiesList, L_DOCUMENT_TO_ENTITY)) {
                docsWithAllEntities.add(doc);
            }
        }
        return docsWithAllEntities;
    }

    /**
     * Return all the entities mentioned in any of the given documents
     *
     * @param documents an arbitrary set of document nodes
     * @return an Iterable for the entity nodes.
     */
    public Iterable<Vertex> getEntitiesInAnyOfTheseDocuments(Iterable<Vertex> documents) {
        return new GremlinPipeline(documents).outE(L_DOCUMENT_TO_ENTITY).inV().dedup();
    }

    /**
     * Return the documents that contain all of the given entities.
     *
     * @param documents an arbitrary set of entity nodes
     * @return an Iterable for the document nodes.
     */
    public Iterable<Vertex> getEntitiesInAllOfTheseDocuments(Iterable<Vertex> documents) {
        List<Vertex> documentsList = makeList(documents);
        List<Vertex> entitiesForAllDocuments = new ArrayList<Vertex>();
        for (Vertex entity : getEntitiesInAnyOfTheseDocuments(documentsList)) {
            if (allInEdgesExist(entity, documentsList, L_DOCUMENT_TO_ENTITY)) {
                entitiesForAllDocuments.add(entity);
            }
        }
        return entitiesForAllDocuments;
    }

    /**
     * Return all the mentions in a given set of documents for a particular entity.
     * @param entity
     * @param documents
     * @return  mentions
     * @throws VizLincDBException
     */
     public Iterable<Vertex> getMentionsInDocumentsForEntity(Iterable<Vertex> documents, Vertex entity) throws VizLincDBException {
        Set<Vertex> documentsSet = makeSet(documents);
        List<Vertex> mentions = new ArrayList<Vertex>();
        for (Vertex mention : getMentionsForEntity(entity)) {
            if (documentsSet.contains(getDocumentForMention(mention))) {
                mentions.add(mention);
            }
        }
        return mentions;
    }

    /**
     * Add a document to the graph, with the given properties.
     *
     * @param documentName a name for the document that is unique across all documents
     * @param documentPath an absolute path to the document file
     * @param text the plaintext contents of the document file. Will be added as a property.
     * @return the Vertex designating the new document
     */
    public Vertex newDocument(String documentName, String documentPath, String text) {
        Vertex documentNode = graph.addVertex(null);
        documentNode.setProperty(P_NODE_TYPE, NODE_TYPE_DOCUMENT);
        documentNode.setProperty(P_DOCUMENT_NAME, documentName);
        documentNode.setProperty(P_DOCUMENT_PATH, documentPath);
        documentNode.setProperty(P_DOCUMENT_TEXT, text);
        return documentNode;
    }

    /**
     * Add a named entity mention to the graph. The mention is in a specific document.
     *
     * @param documentNode the Vertex of the document in which the mention occurs
     * @param type the mention type: PERSON, LOCATION, etc.
     * @param text the plain text of the entity
     * @param index Nth entity in the given document, starting at 0
     * @param globalId if not null, a global id (not per document) for this: topic id, well-known entity id, etc.
     * @param textStart beginning character offset of mention
     * @param textStop ending character offset of mention
     * @return the Vertex designating the new mention
     */
    public Vertex newMention(Vertex documentNode, String type, String text, int index, Integer globalId, int textStart, int textStop) {
        Vertex mentionNode = graph.addVertex(null);
        mentionNode.setProperty(P_NODE_TYPE, NODE_TYPE_MENTION);
        mentionNode.setProperty(P_MENTION_TYPE, type);
        mentionNode.setProperty(P_MENTION_TEXT, text);
        mentionNode.setProperty(P_MENTION_INDEX, index);
        if (globalId != null) {
            mentionNode.setProperty(P_MENTION_GLOBAL_ID, globalId);
        }
        mentionNode.setProperty(P_MENTION_TEXT_START, textStart);
        mentionNode.setProperty(P_MENTION_TEXT_STOP, textStop);

        Edge e = graph.addEdge(null, documentNode, mentionNode, L_DOCUMENT_TO_MENTION);
        return mentionNode;
    }

    /**
     * Create a new named entity node and connect it to a bunch of existing mention nodes and their corresponding
     * document nodes.
     *
     * @param mentionNodes the mention nodes that refer to this entity
     * @param type the entity type: PERSON, LOCATION, etc.
     * @param text the canonical text for this entity. May not match the text used in the mentions.
     * @param createdBy a String designating by what algorithm, when, etc. this entity was created
     * @return the new entity node
     */
    public Vertex newEntity(Iterable<Vertex> mentionNodes, String type, String text, String createdBy) {
        Vertex entityNode = newEntity(type, text, createdBy);
        connectEntityToMentionsAndDocuments(entityNode, mentionNodes);
        return entityNode;
    }

    /**
     * Create a new named entity node.
     *
     * @param type the entity type: PERSON, LOCATION, etc.
     * @param text the canonical text for this entity. May not match the text used in the mentions.
     * @param createdBy a String designating by what algorithm, when, etc. this entity was created
     * @return the entity node, unconnected to anything
     */
    public Vertex newEntity(String type, String text, String createdBy) {
        Vertex entityNode = graph.addVertex(null);
        entityNode.setProperty(P_NODE_TYPE, NODE_TYPE_ENTITY);
        entityNode.setProperty(P_ENTITY_TYPE, type);
        entityNode.setProperty(P_ENTITY_TEXT, text);
        entityNode.setProperty(P_CREATED_BY, createdBy);
        return entityNode;
    }

    /**
     * Associate a single entity with a bunch of mentions and their corresponding documents;
     * connect the the mention nodes and the documents to the entity.
     *
     * @param entityNode the named entity
     * @param mentionNodes the mentions that correspond to the entity
     */
    public void connectEntityToMentionsAndDocuments(Vertex entityNode, Iterable<Vertex> mentionNodes) {
        for (Vertex documentNode : getDocumentsWithMentions(mentionNodes)) {
            graph.addEdge(null, documentNode, entityNode, L_DOCUMENT_TO_ENTITY);
        }
        for (Vertex mentionNode : mentionNodes) {
            graph.addEdge(null, mentionNode, entityNode, L_MENTION_TO_ENTITY);
        }
    }

    /**
     * Get the plain text stored with the document.
     *
     * @param documentNode if not a document, will return null
     * @return null if not a document, otherwise the text as a String
     */
    public String getDocumentText(Vertex documentNode) {
        Object textObject = documentNode.getProperty(P_DOCUMENT_TEXT);
        if (textObject == null) {
            return null;
        }
        return (String) textObject;
    }

    /**
     * Store a ranked list of geoLocations in graph.
     * @param locationNode
     * @param geoLocations
     */
    public void setGeoLocations(Vertex locationNode, GeoLocation[] geoLocations) {
        if (geoLocations == null) {
            // Remove any existing location property values.
            locationNode.removeProperty(P_GEO_LATITUDE_LIST);
            locationNode.removeProperty(P_GEO_LONGITUDE_LIST);
            locationNode.removeProperty(P_GEO_LATITUDE_SOUTH_LIST);
            locationNode.removeProperty(P_GEO_LATITUDE_NORTH_LIST);
            locationNode.removeProperty(P_GEO_LONGITUDE_WEST_LIST);
            locationNode.removeProperty(P_GEO_LONGITUDE_EAST_LIST);
            locationNode.removeProperty(P_GEO_NAME_LIST);
            locationNode.removeProperty(P_GEO_OSM_TYPE_LIST);
            locationNode.removeProperty(P_GEO_NGA_DESIGNATION_LIST);
            locationNode.removeProperty(P_GEO_COUNTRY_LIST);
            locationNode.removeProperty(P_GEO_SOURCE_LIST);
            return;
        }

        boolean hasBoundingBoxes = false;
        for (GeoLocation loc : geoLocations) {
            if (loc.hasBoundingBox()) {
                hasBoundingBoxes = true;
                break;
            }
        }

        // Must store geolocation info in parallel arrays because some graph dbs don't store complex objects
        // in properties.
        int n = geoLocations.length;
        double[] latitudeArray = new double[n];
        double[] longitudeArray = new double[n];
        double[] latitudeSouthArray = hasBoundingBoxes ? new double[n] : null;
        double[] latitudeNorthArray = hasBoundingBoxes ? new double[n] : null;
        double[] longitudeWestArray = hasBoundingBoxes ? new double[n] : null;
        double[] longitudeEastArray = hasBoundingBoxes ? new double[n] : null;
        String[] nameArray = new String[n];
        String[] osmTypeArray = new  String[n];
        String[] ngaDesignationArray = new String[n];
        String[] countryArray = new String[n];
        String[] sourceArray = new String[n];

        int i = -1;
        for (GeoLocation loc : geoLocations) {
            i++;
            latitudeArray[i] = loc.latitude;
            longitudeArray[i] = loc.longitude;

            // If there's one good bounding box, we have to store entries for all, since the parallel arrays
            // must all be of the same length.
            if (hasBoundingBoxes) {
                if (loc.hasBoundingBox()) {
                    latitudeSouthArray[i] = loc.boundingBox.latitudeSouth;
                    latitudeNorthArray[i] = loc.boundingBox.latitudeNorth;
                    longitudeWestArray[i] = loc.boundingBox.longitudeWest;
                    longitudeEastArray[i] = loc.boundingBox.longitudeEast;
                } else {
                    latitudeSouthArray[i] = INVALID_LAT_LON;
                    latitudeNorthArray[i] = INVALID_LAT_LON;
                    longitudeWestArray[i] = INVALID_LAT_LON;
                    longitudeEastArray[i] = INVALID_LAT_LON;
                }
            }
            nameArray[i] = loc.name;
            osmTypeArray[i] = (loc.osmType == null) ? "" : loc.osmType;
            ngaDesignationArray[i] = (loc.ngaDesignation == null) ? "" : loc.ngaDesignation;
            countryArray[i] = loc.country;
            sourceArray[i] = loc.source;
        }

        locationNode.setProperty(P_GEO_LATITUDE_LIST, latitudeArray);
        locationNode.setProperty(P_GEO_LONGITUDE_LIST, longitudeArray);
        if (hasBoundingBoxes) {
            locationNode.setProperty(P_GEO_LATITUDE_SOUTH_LIST, latitudeSouthArray);
            locationNode.setProperty(P_GEO_LATITUDE_NORTH_LIST, latitudeNorthArray);
            locationNode.setProperty(P_GEO_LONGITUDE_WEST_LIST, longitudeWestArray);
            locationNode.setProperty(P_GEO_LONGITUDE_EAST_LIST, longitudeEastArray);
        }
        locationNode.setProperty(P_GEO_NAME_LIST, nameArray);
        locationNode.setProperty(P_GEO_OSM_TYPE_LIST, osmTypeArray);
        locationNode.setProperty(P_GEO_NGA_DESIGNATION_LIST, ngaDesignationArray);
        locationNode.setProperty(P_GEO_COUNTRY_LIST, countryArray);
        locationNode.setProperty(P_GEO_SOURCE_LIST, sourceArray);
    }

    /**
     * Fetch the ranked list of geolocations associated with the given location node.
     * Return null if there are no locations.
     * @param locationNode
     * @return the list or null if none
     */
    public GeoLocation[] getGeoLocations(Vertex locationNode) {
        Object testLatitudeList = locationNode.getProperty(P_GEO_LATITUDE_LIST);
        if (testLatitudeList == null) {
            // There's no geo info at all.
            return null;
        }

       boolean hasBoundingBoxes = locationNode.getProperty(P_GEO_LATITUDE_SOUTH_LIST) != null;


        // Blueprints now converts plain arrays to ArrayLists on getProperty fetches from neo4j. This changed from 2.2.0 to 2.4.0.
        // See https://github.com/tinkerpop/blueprints/issues/389 and https://groups.google.com/d/topic/gremlin-users/JCYnk8gekv8/discussion.
        // So we have to undo this.

        double[] latitudeArray,longitudeArray;
        double[] latitudeSouthArray = null, latitudeNorthArray = null, longitudeWestArray = null, longitudeEastArray = null;
        String[] nameArray, osmTypeArray, ngaDesignationArray, countryArray, sourceArray;

        if (testLatitudeList.getClass().isArray()) {
            latitudeArray = locationNode.getProperty(P_GEO_LATITUDE_LIST);
            longitudeArray = (double[]) locationNode.getProperty(P_GEO_LONGITUDE_LIST);
            if (hasBoundingBoxes) {
                latitudeSouthArray = (double[]) locationNode.getProperty(P_GEO_LATITUDE_SOUTH_LIST);
                latitudeNorthArray = (double[]) locationNode.getProperty(P_GEO_LATITUDE_NORTH_LIST);
                longitudeWestArray = (double[]) locationNode.getProperty(P_GEO_LONGITUDE_WEST_LIST);
                longitudeEastArray = (double[]) locationNode.getProperty(P_GEO_LONGITUDE_EAST_LIST);
            }
            nameArray = (String[]) locationNode.getProperty(P_GEO_NAME_LIST);
            osmTypeArray = (String[]) locationNode.getProperty(P_GEO_OSM_TYPE_LIST);
            ngaDesignationArray = (String[]) locationNode.getProperty(P_GEO_NGA_DESIGNATION_LIST);
            countryArray = (String[]) locationNode.getProperty(P_GEO_COUNTRY_LIST);
            sourceArray = (String[]) locationNode.getProperty(P_GEO_SOURCE_LIST);
        } else {
            final String[] protoStringArray = {};
            latitudeArray = Doubles.toArray((ArrayList<Double>) locationNode.getProperty(P_GEO_LATITUDE_LIST));
            longitudeArray = Doubles.toArray((ArrayList<Double>) locationNode.getProperty(P_GEO_LONGITUDE_LIST));
            if (hasBoundingBoxes) {
                latitudeSouthArray = Doubles.toArray((ArrayList<Double>) locationNode.getProperty(P_GEO_LATITUDE_SOUTH_LIST));
                latitudeNorthArray = Doubles.toArray((ArrayList<Double>) locationNode.getProperty(P_GEO_LATITUDE_NORTH_LIST));
                longitudeWestArray = Doubles.toArray((ArrayList<Double>) locationNode.getProperty(P_GEO_LONGITUDE_WEST_LIST));
                longitudeEastArray = Doubles.toArray((ArrayList<Double>) locationNode.getProperty(P_GEO_LONGITUDE_EAST_LIST));
            }
            nameArray = ((ArrayList<String>) locationNode.getProperty(P_GEO_NAME_LIST)).toArray(protoStringArray);
            osmTypeArray = ((ArrayList<String>) locationNode.getProperty(P_GEO_OSM_TYPE_LIST)).toArray(protoStringArray);
            ngaDesignationArray = ((ArrayList<String>) locationNode.getProperty(P_GEO_NGA_DESIGNATION_LIST)).toArray(protoStringArray);
            countryArray = ((ArrayList<String>) locationNode.getProperty(P_GEO_COUNTRY_LIST)).toArray(protoStringArray);
            sourceArray = ((ArrayList<String>) locationNode.getProperty(P_GEO_SOURCE_LIST)).toArray(protoStringArray);
        }

        int n = latitudeArray.length;
        GeoLocation[] locations = new GeoLocation[n];
        for (int i = 0; i < latitudeArray.length; i++) {
            locations[i] = new GeoLocation(
                    latitudeArray[i],
                    longitudeArray[i],
                    (hasBoundingBoxes && latitudeSouthArray[i] != INVALID_LAT_LON)
                    ? new GeoBoundingBox(latitudeSouthArray[i], latitudeNorthArray[i], longitudeWestArray[i], longitudeEastArray[i])
                    : null,
                    nameArray[i],
                    osmTypeArray[i].isEmpty() ? null : osmTypeArray[i],
                    ngaDesignationArray[i].isEmpty() ? null : ngaDesignationArray[i],
                    countryArray[i],
                    sourceArray[i]);
        }
        return locations;
    }

    /**
     * Fetch all the items in the Iterable and put them in a Set.
     *
     * @param <T> type of the elements of the iterable
     * @param iter the iterable
     * @return the Set<T> of all the elements
     */
    public static <T> Set<T> makeSet(Iterable<T> iter) {
        Set<T> set = new HashSet<T>();
        for (T item : iter) {
            set.add(item);
        }
        return set;
    }

    /**
     * Fetch all the items in the Iterable and put them in a List.
     *
     * @param <T> type of the elements of the iterable
     * @param iter the iterable
     * @return the List<T> of all the elements
     */
    public static <T> List<T> makeList(Iterable<T> iter) {
        List<T> list = new ArrayList<T>();
        for (T item : iter) {
            list.add(item);
        }
        return list;
    }

    /**
     * Fetch all the Elements (Vertex or Edge) in the Iterable and put their ids in a set.
     *
     * @param <E> type of the elements of the iterable
     * @param iter the iterable over the Elements
     * @return the set of all the ids of the Elements
     */
    public static <E extends Element> Set<Object> makeSetOfIds(Iterable<E> iter) {
        Set<Object> set = new HashSet<Object>();
        for (E element : iter) {
            set.add(element.getId());
        }
        return set;
    }

    /**
     * Fetch all the Elements (Vertex or Edge) in the Iterable and put their ids in a list.
     *
     * @param <E> type of the elements of the iterable
     * @param iter the iterable over the Elements
     * @return the list of all the ids of the Elements
     */
    public static <E extends Element> List<Object> makeListOfIds(Iterable<E> iter) {
        List<Object> list = new ArrayList<Object>();
        for (E element : iter) {
            list.add(element.getId());
        }
        return list;
    }

    /**
     * Count how many objects an Iterable returns. The Iterable is assumed to terminate.
     * @param objects  the Iterable
     * @return  count of objects in the Iterable.
     */
    public static int count(Iterable<? extends Object> objects) {
        int count = 0;
        Iterator<? extends Object> iter = objects.iterator();
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        return count;
    }
}
