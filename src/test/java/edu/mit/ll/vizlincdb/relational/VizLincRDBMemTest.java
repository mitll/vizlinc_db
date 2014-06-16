package edu.mit.ll.vizlincdb.relational;

import edu.mit.ll.vizlincdb.document.Document;
import edu.mit.ll.vizlincdb.geo.GeoBoundingBox;
import edu.mit.ll.vizlincdb.entity.EntityCounts;
import edu.mit.ll.vizlincdb.entity.OrganizationEntity;
import edu.mit.ll.vizlincdb.geo.GeoPoint;
import edu.mit.ll.vizlincdb.entity.DateEntity;
import edu.mit.ll.vizlincdb.entity.LocationEntity;
import edu.mit.ll.vizlincdb.geo.GeoLocation;
import edu.mit.ll.vizlincdb.entity.PersonEntity;
import edu.mit.ll.vizlincdb.entity.Mention;
import edu.mit.ll.vizlincdb.entity.EntitySet;
import edu.mit.ll.vizlincdb.entity.Entity;
import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import static java.sql.Types.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 */
public class VizLincRDBMemTest {

    // Saved Vertex objects and their ids, for testing comparisons.
    public static VizLincRDBMem db;
    public static int document1Id;
    public static String document1Name;
    public static String document1Path;
    public static String document1Text;
    public static int document2Id;
    public static String document2Text;
    public static int mentionDoc1AliceId;
    public static int mentionDoc1Bob1Id;
    public static int mentionDoc1Bob2Id;
    public static int mentionDoc1LexingtonId;
    public static int mentionDoc2AliceId;
    public static int mentionDoc2BobId;
    public static int entityAliceId;
    public static int entityBobId;
    public static PersonEntity entityBob;
    public static int entityLexingtonId;
    public static GeoLocation entityLexingtonGeoLocation1;
    public static GeoLocation entityLexingtonGeoLocation2;
    public static GeoPoint entityLexingtonGeoPoint1;
    public static GeoPoint entityLexingtonGeoPoint2;
    public static int mentionDoc2LincolnLabId;
    public static OrganizationEntity entityLincolnLab;
    public static int entityLincolnLabId;
    public static int mentionDoc2Date2012Id;
    public static DateEntity entityDate2012;
    public static int entityDate2012Id;

    public static Set<Integer> documentIds;
    public static Set<Integer> mentionIds;
    public static Set<Integer> document1MentionIds;
    public static Set<Integer> document2MentionIds;
    public static Set<Integer> personMentionIds;
    public static Set<Integer> personRelativeMentionIds;
    public static Set<Integer> aliceMentionIds;
    public static Set<Integer> documentToMentionEdgeIds;
    public static Set<Integer> entityIds;
    public static Set<Integer> dateEntityIds;
    public static Set<Integer> locationEntityIds;
    public static Set<Integer> organizationEntityIds;
    public static Set<Integer> personEntityIds;
    public static Set<Integer> createdBy2EntityIds;
    public static Set<Integer> document1EntityIds;
    public static Set<Integer> document2EntityIds;

    public VizLincRDBMemTest() {
    }

    @ClassRule
    public static TemporaryFolder tempDir = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws SQLException, IOException {
        // Create a new database with a few nodes. Remember sets of interesting objects fo testing comparisons.

        File dbFolder = tempDir.newFolder();
        VizLincRDB rdb = new VizLincRDB(dbFolder, true);
        int id = 0;
        int i;
        PreparedStatement docInsert = rdb.conn.prepareStatement("INSERT INTO document (document_id, name, path, text) " +
                "VALUES(?, ?, ?, ?)");

        i = 0;
        document1Id = ++id;
        docInsert.setInt(++i, document1Id);
        document1Name = "doc1";
        docInsert.setString(++i, document1Name);
        document1Path = "dir1/doc1";
        docInsert.setString(++i, document1Path);
        document1Text = "abc def ghi jkl";
        docInsert.setString(++i, document1Text);
        docInsert.execute();

        i = 0;
        document2Id = ++id;
        docInsert.setInt(++i, document2Id);
        docInsert.setString(++i, "doc1");
        docInsert.setString(++i, "dir1/doc1");
        docInsert.setString(++i, "mno pqr stu");
        docInsert.execute();

        PreparedStatement entityInsert = rdb.conn.prepareStatement("INSERT INTO entity (entity_id, type, text, created_by, num_documents, num_mentions) " +
                "VALUES(?, ?, ?, ?, ?, ?)");

        i = 0;
        entityAliceId = ++id;
        entityInsert.setInt(++i, entityAliceId);
        entityInsert.setString(++i, "PERSON");
        entityInsert.setString(++i, "Alice Ailey");
        entityInsert.setString(++i, "createdby1");
        entityInsert.setInt(++i, 2);
        entityInsert.setInt(++i, 2);
        entityInsert.executeUpdate();

        i = 0;
        entityBobId = ++id;
        entityBob = new PersonEntity("Bob", 2, 3, "createdby2", entityBobId);
        entityInsert.setInt(++i, entityBobId);
        entityInsert.setString(++i, entityBob.getType());
        entityInsert.setString(++i, entityBob.getText());
        entityInsert.setString(++i, entityBob.getCreatedBy());
        entityInsert.setInt(++i, entityBob.getNumDocuments());
        entityInsert.setInt(++i, entityBob.getNumMentions());
        entityInsert.executeUpdate();

        i = 0;
        entityLexingtonId = ++id;
        entityInsert.setInt(++i, entityLexingtonId);
        entityInsert.setString(++i, "LOCATION");
        entityInsert.setString(++i, "Lexington");
        entityInsert.setString(++i, "createdby2");
        entityInsert.setInt(++i, 1);
        entityInsert.setInt(++i, 1);
        entityInsert.executeUpdate();

        i= 0;
        entityLincolnLabId = ++id;
        entityLincolnLab = new OrganizationEntity("Lincoln Lab", 1, 1, "createdby2", entityLincolnLabId);
        entityInsert.setInt(++i, entityLincolnLabId);
        entityInsert.setString(++i, entityLincolnLab.getType());
        entityInsert.setString(++i, entityLincolnLab.getText());
        entityInsert.setString(++i, entityLincolnLab.getCreatedBy());
        entityInsert.setInt(++i, entityLincolnLab.getNumDocuments());
        entityInsert.setInt(++i, entityLincolnLab.getNumMentions());
        entityInsert.executeUpdate();

        i = 0;
        entityDate2012Id = ++id;
        entityDate2012 = new DateEntity("2012", 1, 1, "createdby3", entityDate2012Id);
        entityInsert.setInt(++i, entityDate2012Id);
        entityInsert.setString(++i, entityDate2012.getType());
        entityInsert.setString(++i, entityDate2012.getText());
        entityInsert.setString(++i, entityDate2012.getCreatedBy());
        entityInsert.setInt(++i, entityDate2012.getNumDocuments());
        entityInsert.setInt(++i, entityDate2012.getNumMentions());
        entityInsert.executeUpdate();

        PreparedStatement mentionInsert = rdb.conn.prepareStatement("INSERT INTO mention (mention_id, document_id, entity_id, type, text, index, global_id, text_start, text_stop) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)");

        i = 0;
        mentionDoc1AliceId = ++id;
        mentionInsert.setInt(++i, mentionDoc1AliceId);
        mentionInsert.setInt(++i, document1Id);
        mentionInsert.setInt(++i, entityAliceId);
        mentionInsert.setString(++i, "PERSON");
        mentionInsert.setString(++i, "Alice");
        mentionInsert.setInt(++i, 0);  // index
        mentionInsert.setNull(++i, INTEGER);  // NULL global_id
        mentionInsert.setInt(++i, 10);  // start
        mentionInsert.setInt(++i, 15);  // stop
        mentionInsert.executeUpdate();

        i = 0;
        mentionDoc2AliceId = ++id;
        mentionInsert.setInt(++i, mentionDoc2AliceId);
        mentionInsert.setInt(++i, document2Id);
        mentionInsert.setInt(++i, entityAliceId);
        mentionInsert.setString(++i, "PERSON");
        mentionInsert.setString(++i, "Alice Ailey");
        mentionInsert.setInt(++i, 0);  // index
        mentionInsert.setNull(++i, INTEGER);  // NULL global_id
        mentionInsert.setInt(++i, 10);  // start
        mentionInsert.setInt(++i, 21);  // stop
        mentionInsert.executeUpdate();

        i = 0;
        mentionDoc1Bob1Id = ++id;
        mentionInsert.setInt(++i, mentionDoc1Bob1Id);
        mentionInsert.setInt(++i, document1Id);
        mentionInsert.setInt(++i, entityBobId);
        mentionInsert.setString(++i, "PERSON");
        mentionInsert.setString(++i, "Bob");
        mentionInsert.setInt(++i, 1);  // index
        mentionInsert.setNull(++i, INTEGER);  // NULL global_id
        mentionInsert.setInt(++i, 20);  // start
        mentionInsert.setInt(++i, 23);  // stop
        mentionInsert.executeUpdate();

        i = 0;
        mentionDoc1Bob2Id = ++id;
        mentionInsert.setInt(++i, mentionDoc1Bob2Id);
        mentionInsert.setInt(++i, document1Id);
        mentionInsert.setInt(++i, entityBobId);
        mentionInsert.setString(++i, "PERSON");
        mentionInsert.setString(++i, "Bob");
        mentionInsert.setInt(++i, 3);  // index
        mentionInsert.setNull(++i, INTEGER);  // NULL global_id
        mentionInsert.setInt(++i, 30);  // start
        mentionInsert.setInt(++i, 33);  // st0p
        mentionInsert.executeUpdate();

        i = 0;
        mentionDoc2BobId = ++id;
        mentionInsert.setInt(++i, mentionDoc2BobId);
        mentionInsert.setInt(++i, document2Id);
        mentionInsert.setInt(++i, entityBobId);
        mentionInsert.setString(++i, "PERSON");
        mentionInsert.setString(++i, "Bob Bean");
        mentionInsert.setInt(++i, 0);  // index
        mentionInsert.setNull(++i, INTEGER);  // NULL global_id
        mentionInsert.setInt(++i, 20);  // start
        mentionInsert.setInt(++i, 28);  // stop
        mentionInsert.executeUpdate();

        i = 0;
        mentionDoc1LexingtonId = ++id;
        mentionInsert.setInt(++i, mentionDoc1LexingtonId);
        mentionInsert.setInt(++i, document1Id);
        mentionInsert.setInt(++i, entityLexingtonId);
        mentionInsert.setString(++i, "LOCATION");
        mentionInsert.setString(++i, "Lexington");
        mentionInsert.setInt(++i, 4);  // index
        mentionInsert.setNull(++i, 33);  // non-NULL global_id (e.g., well-known location)
        mentionInsert.setInt(++i, 44);
        mentionInsert.setInt(++i, 53);
        mentionInsert.executeUpdate();

        i = 0;
        mentionDoc2LincolnLabId = ++id;
        mentionInsert.setInt(++i, mentionDoc2LincolnLabId);
        mentionInsert.setInt(++i, document2Id);
        mentionInsert.setInt(++i, entityLincolnLabId);
        mentionInsert.setString(++i, "ORGANIZATION");
        mentionInsert.setString(++i, "MITLL");
        mentionInsert.setInt(++i, 0);  // index
        mentionInsert.setNull(++i, INTEGER);  // NULL global_id
        mentionInsert.setInt(++i, 20);  // start
        mentionInsert.setInt(++i, 25);  // stop
        mentionInsert.executeUpdate();

        i = 0;
        mentionDoc2Date2012Id = ++id;
        mentionInsert.setInt(++i, mentionDoc2Date2012Id);
        mentionInsert.setInt(++i, document2Id);
        mentionInsert.setInt(++i, entityDate2012Id);
        mentionInsert.setString(++i, "DATE");
        mentionInsert.setString(++i, "2012");
        mentionInsert.setInt(++i, 0);  // index
        mentionInsert.setNull(++i, INTEGER);  // NULL global_id
        mentionInsert.setInt(++i, 20);  // start
        mentionInsert.setInt(++i, 24);  // stop
        mentionInsert.executeUpdate();


        documentIds = new HashSet<Integer>(Arrays.asList(document1Id, document2Id));
        document1MentionIds = new HashSet<Integer>(Arrays.asList(mentionDoc1AliceId, mentionDoc1Bob1Id, mentionDoc1Bob2Id, mentionDoc1LexingtonId));
        document2MentionIds = new HashSet<Integer>(Arrays.asList(mentionDoc2AliceId, mentionDoc2BobId));
        mentionIds = new HashSet<Integer>(Arrays.asList(mentionDoc1AliceId, mentionDoc1Bob1Id, mentionDoc1Bob2Id, mentionDoc1LexingtonId, mentionDoc2AliceId, mentionDoc2BobId));
        personMentionIds = new HashSet<Integer>(Arrays.asList(mentionDoc1AliceId, mentionDoc1Bob1Id, mentionDoc1Bob2Id, mentionDoc2AliceId, mentionDoc2BobId));
        personRelativeMentionIds = new HashSet<Integer>(Arrays.asList(mentionDoc1AliceId, mentionDoc2BobId));
        aliceMentionIds = new HashSet<Integer>(Arrays.asList(mentionDoc1AliceId, mentionDoc2AliceId));

        PreparedStatement documentEntityInsert = rdb.conn.prepareStatement("INSERT INTO document_entity (document_id, entity_id, num_mentions) " +
                "VALUES(?, ?, ?)");

        i = 0; documentEntityInsert.setInt(++i, document1Id);  documentEntityInsert.setInt(++i, entityAliceId); documentEntityInsert.setInt(++i, 1);
        documentEntityInsert.executeUpdate();
        i = 0; documentEntityInsert.setInt(++i, document1Id);  documentEntityInsert.setInt(++i, entityBobId); documentEntityInsert.setInt(++i, 2);
        documentEntityInsert.executeUpdate();
        i = 0; documentEntityInsert.setInt(++i, document1Id);  documentEntityInsert.setInt(++i, entityLexingtonId); documentEntityInsert.setInt(++i, 1);
        documentEntityInsert.executeUpdate();

        i = 0; documentEntityInsert.setInt(++i, document2Id);  documentEntityInsert.setInt(++i, entityAliceId); documentEntityInsert.setInt(++i, 1);
        documentEntityInsert.executeUpdate();
        i = 0; documentEntityInsert.setInt(++i, document2Id);  documentEntityInsert.setInt(++i, entityBobId); documentEntityInsert.setInt(++i, 1);
        documentEntityInsert.executeUpdate();
        i = 0; documentEntityInsert.setInt(++i, document2Id);  documentEntityInsert.setInt(++i, entityDate2012Id); documentEntityInsert.setInt(++i, 1);
        documentEntityInsert.executeUpdate();
        i = 0; documentEntityInsert.setInt(++i, document2Id);  documentEntityInsert.setInt(++i, entityLincolnLabId); documentEntityInsert.setInt(++i, 1);
        documentEntityInsert.executeUpdate();

        entityIds = new HashSet<Integer>(Arrays.asList(entityAliceId, entityBobId, entityLexingtonId, entityDate2012Id, entityLincolnLabId));
        dateEntityIds = new HashSet<Integer>(Arrays.asList(entityDate2012Id));
        locationEntityIds = new HashSet<Integer>(Arrays.asList(entityLexingtonId));
        organizationEntityIds = new HashSet<Integer>(Arrays.asList(entityLincolnLabId));
        personEntityIds = new HashSet<Integer>(Arrays.asList(entityAliceId, entityBobId));
        createdBy2EntityIds = new HashSet<Integer>(Arrays.asList(entityBobId, entityLexingtonId));
        document1EntityIds = new HashSet<Integer>(Arrays.asList(entityAliceId, entityBobId, entityLexingtonId));
        document2EntityIds = new HashSet<Integer>(Arrays.asList(entityAliceId, entityBobId, entityLincolnLabId));

        PreparedStatement geolocationInsert = rdb.conn.prepareStatement(
                "INSERT INTO geolocation (entity_id, rank, " +
                "latitude, longitude, latitude_south, latitude_north, longitude_west, longitude_east, " +
                "name, osm_type, nga_designation, country, source) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        entityLexingtonGeoLocation1 = new GeoLocation(38.0, -84.0, null, "Lexington KY", "city", null, "US", "osm");
        entityLexingtonGeoPoint1 = new GeoPoint(entityLexingtonGeoLocation1.latitude, entityLexingtonGeoLocation1.longitude, entityLexingtonId);
        i = 0;
        geolocationInsert.setInt(++i, entityLexingtonId);
        geolocationInsert.setInt(++i, 0); // rank
        geolocationInsert.setDouble(++i, entityLexingtonGeoLocation1.latitude);
        geolocationInsert.setDouble(++i, entityLexingtonGeoLocation1.longitude);
        geolocationInsert.setNull(++i, DOUBLE);
        geolocationInsert.setNull(++i, DOUBLE);
        geolocationInsert.setNull(++i, DOUBLE);
        geolocationInsert.setNull(++i, DOUBLE);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation1.name);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation1.osmType);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation1.ngaDesignation);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation1.country);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation1.source);
        geolocationInsert.executeUpdate();

        entityLexingtonGeoLocation2 = new GeoLocation(42.0, -71.0, new GeoBoundingBox(41.9, 42.1, -71.1, -70.9), "Lexington MA", "city", null, "US", "osm");
        entityLexingtonGeoPoint2 = new GeoPoint(entityLexingtonGeoLocation2.latitude, entityLexingtonGeoLocation2.longitude, entityLexingtonId);
        i = 0;
        geolocationInsert.setInt(++i, entityLexingtonId);
        geolocationInsert.setInt(++i, 1); // rank
        geolocationInsert.setDouble(++i, entityLexingtonGeoLocation2.latitude);
        geolocationInsert.setDouble(++i, entityLexingtonGeoLocation2.longitude);
        geolocationInsert.setDouble(++i, entityLexingtonGeoLocation2.boundingBox.latitudeSouth);
        geolocationInsert.setDouble(++i, entityLexingtonGeoLocation2.boundingBox.latitudeNorth);
        geolocationInsert.setDouble(++i, entityLexingtonGeoLocation2.boundingBox.longitudeWest);
        geolocationInsert.setDouble(++i, entityLexingtonGeoLocation2.boundingBox.longitudeEast);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation2.name);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation2.osmType);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation2.ngaDesignation);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation2.country);
        geolocationInsert.setString(++i, entityLexingtonGeoLocation2.source);
        geolocationInsert.executeUpdate();

        // Shutdown the backing VizLincRDB and reopen it as a VizLincRDBMem.
        rdb.shutdown();
        db = new VizLincRDBMem(dbFolder);
        // Loading the VizLincRDBMem the first time will create the prefetched serialization files. Load it again to make use of them.
        db.shutdown();
        db = new VizLincRDBMem(dbFolder);


    }

    @AfterClass
    public static void tearDownClass() throws SQLException {
        db.shutdown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of shutdown method, of class VizLincRDBMem.
     */
    @Test
    public void testShutdown() throws Exception {
        // TODO
    }

    /**
     * Test of getDocuments method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDocuments() throws Exception {
        assertEquals(documentIds, new HashSet<Integer>(Document.listOfDocumentIds(db.getDocuments())));
    }

    /**
     * Test of getDocumentWithId method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDocumentWithId() throws Exception {
        Document doc = db.getDocumentWithId(document1Id);
        assertEquals(document1Name, doc.getName());
        assertEquals(document1Path, doc.getPath());
        assertEquals(document1Id, doc.getId());
    }

    /**
     * Test of getEntityWithId method, of class VizLincRDBMem.
     */
    @Test
    public void testGetEntityWithId() throws Exception {
        Entity entity = db.getEntityWithId(entityBobId);
        assertEquals(entityBob, entity);
    }

    /**
     * Test of getDocumentText method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDocumentText() throws Exception {
        assertEquals(document1Text, db.getDocumentText(db.getDocumentWithId(document1Id)));
    }


    /**
     * Test of getTopGeoPointsForLocationEntityIds method, of class VizLincRDBMem.
     */
    @Test
    public void getTopGeoPointsForLocationEntityIds() throws Exception {
        List<GeoPoint> geopoints = db.getTopGeoPointsForLocationEntityIds(Arrays.asList(entityLexingtonId));
        assertEquals(1, geopoints.size());
        assertEquals(entityLexingtonGeoPoint1, geopoints.get(0));
    }

    /**
     * Test of getPersonEntities method, of class VizLincRDBMem.
     */
    @Test
    public void testGetPersonEntities() throws Exception {
        assertEquals(personEntityIds, new HashSet<Integer>(PersonEntity.listOfPersonEntityIds(db.getPersonEntities())));
     }

    /**
     * Test of getLocationEntities method, of class VizLincRDBMem.
     */
    @Test
    public void testGetLocationEntities() throws Exception {
        assertEquals(locationEntityIds, new HashSet<Integer>(LocationEntity.listOfLocationEntityIds(db.getLocationEntities())));
    }

    /**
     * Test of getDateEntities method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDateEntities() throws Exception {
        assertEquals(dateEntityIds, new HashSet<Integer>(DateEntity.listOfDateEntityIds(db.getDateEntities())));
    }

    /**
     * Test of getOrganizationEntities method, of class VizLincRDBMem.
     */
    @Test
    public void testGetOrganizationEntities() throws Exception {
        assertEquals(organizationEntityIds, new HashSet<Integer>(OrganizationEntity.listOfOrganizationEntityIds(db.getOrganizationEntities())));
    }

    /**
     * Test of testGetDocumentIdsWithAllOfTheseEntityIds method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDocumentIdsWithAllOfTheseEntityIds() throws Exception {
        assertEquals(new HashSet<Integer>(Arrays.asList(document1Id)),
                new HashSet<Integer>(db.getDocumentIdsWithAllOfTheseEntityIds(Arrays.asList(entityAliceId, entityBobId, entityLexingtonId))));
        assertEquals(new HashSet<Integer>(Arrays.asList(document1Id, document2Id)),
                new HashSet<Integer>(db.getDocumentIdsWithAllOfTheseEntityIds(Arrays.asList(entityAliceId, entityBobId))));
    }

    /**
     * Test of getDistinctMentionTextsForEntityId method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDistinctMentionTextsForEntityId() throws Exception {
        assertEquals(new HashSet<String>(Arrays.asList("Bob", "Bob Bean")),
                new HashSet<String>(db.getDistinctMentionTextsForEntityId(entityBobId)));
     }

    /**
     * Test of getDistinctMentionTextsForEntityIdInDocument method, of class VizLincRDB.
     */
    @Test
    public void testGetDistinctMentionTextsForEntityIdInDocument() throws Exception {
        assertEquals(new HashSet<String>(Arrays.asList("Bob Bean")),
                new HashSet<String>(db.getDistinctMentionTextsForEntityIdInDocument(entityBobId, document2Id)));
     }

    /**
     * Test of testGetEntityIdsInAnyofTheseDocumentIds method, of class VizLincRDBMem.
     */
    @Test
    public void testGetEntityIdsInAnyofTheseDocumentIds() throws Exception {
        EntitySet es1 = db.getEntitiesWithIds(db.getEntityIdsInAnyofTheseDocumentIds(Arrays.asList(document1Id)));
        assertEquals(new HashSet<Integer>(), new HashSet<Integer>(DateEntity.listOfDateEntityIds(es1.getDateEntities())));
        assertEquals(new HashSet<Integer>(Arrays.asList(entityLexingtonId)), new HashSet<Integer>(LocationEntity.listOfLocationEntityIds(es1.getLocationEntities())));
        assertEquals(new HashSet<Integer>(), new HashSet<Integer>(OrganizationEntity.listOfOrganizationEntityIds(es1.getOrganizationEntities())));
        assertEquals(new HashSet<Integer>(Arrays.asList(entityAliceId, entityBobId)), new HashSet<Integer>(PersonEntity.listOfPersonEntityIds(es1.getPersonEntities())));

        EntitySet es2 = db.getEntitiesWithIds(db.getEntityIdsInAnyofTheseDocumentIds(Arrays.asList(document2Id)));
        assertEquals(new HashSet<Integer>(Arrays.asList(entityDate2012Id)), new HashSet<Integer>(DateEntity.listOfDateEntityIds(es2.getDateEntities())));
        assertEquals(new HashSet<Integer>(), new HashSet<Integer>(LocationEntity.listOfLocationEntityIds(es2.getLocationEntities())));
        assertEquals(new HashSet<Integer>(Arrays.asList(entityLincolnLabId)), new HashSet<Integer>(OrganizationEntity.listOfOrganizationEntityIds(es2.getOrganizationEntities())));
        assertEquals(new HashSet<Integer>(Arrays.asList(entityAliceId, entityBobId)), new HashSet<Integer>(PersonEntity.listOfPersonEntityIds(es2.getPersonEntities())));
    }

    /**
     * Test of getMentionCountsForEntitiesInDocuments method, of class VizLincRDBMem.
     */
    @Test
    public void testGetMentionCountsForEntitiesInDocuments() throws Exception {
        Map<Integer, Integer> entityIdToMentionCount = new HashMap<Integer, Integer>();
        entityIdToMentionCount.put(entityBobId, 2);
        entityIdToMentionCount.put(entityLexingtonId, 1);
        entityIdToMentionCount.put(entityLincolnLabId, 0);
        assertEquals(entityIdToMentionCount,
                db.getMentionCountsForEntitiesInDocuments(Arrays.asList(entityBobId, entityLexingtonId, entityLincolnLabId),
                    Arrays.asList(document1Id)));
    }

    /**
     * Test of getDocumentCountsForEntitiesInDocuments method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDocumentCountsForEntitiesInDocuments() throws Exception {
        Map<Integer, Integer> entityIdToDocumentCount = new HashMap<Integer, Integer>();
        entityIdToDocumentCount.put(entityBobId, 2);
        entityIdToDocumentCount.put(entityLexingtonId, 1);
        entityIdToDocumentCount.put(entityLincolnLabId, 1);
        assertEquals(entityIdToDocumentCount,
                db.getDocumentCountsForEntitiesInDocuments(
                    Arrays.asList(entityBobId, entityLexingtonId, entityLincolnLabId),
                    Arrays.asList(document1Id, document2Id)));
    }

    /**
     * Test of getDocumentsWithIds method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDocumentsWithIds() throws Exception {
        assertEquals(Arrays.asList(document1Id, document2Id),
                Document.listOfDocumentIds(db.getDocumentsWithIds(Arrays.asList(document1Id, document2Id))));
    }

    /**
     * Test of getEntitiesWithIds method, of class VizLincRDBMem.
     */
    @Test
    public void testGetEntitiesWithIds() throws Exception {
        EntitySet es = db.getEntitiesWithIds(Arrays.asList(entityAliceId, entityLexingtonId));
        assertEquals(0, es.getDateEntities().size());
        assertEquals(Arrays.asList(db.getEntityWithId(entityLexingtonId)), es.getLocationEntities());
        assertEquals(0, es.getOrganizationEntities().size());
        assertEquals(Arrays.asList(db.getEntityWithId(entityAliceId)), es.getPersonEntities());
    }

    /**
     * Test of getEntities method, of class VizLincRDBMem.
     */
    @Test
    public void testGetEntities() {
        EntitySet es = db.getEntities();
        assertEquals(entityIds, new HashSet(es.getEntityIds()));
    }

    /**
     * Test of getTopGeoPointsForLocationEntityIds method, of class VizLincRDBMem.
     */
    @Test
    public void testGetTopGeoPointsForLocationEntityIds() throws Exception {
        System.out.println("getTopGeoPointsForLocationEntityIds TEST NOT IMPLEMENTED");
    }

    /**
     * Test of getMentionWithId method, of class VizLincRDBMem.
     */
    @Test
    public void testGetMentionWithId() throws Exception {
        assertEquals(
                new Mention(mentionDoc1AliceId, document1Id, entityAliceId, "PERSON", "Alice", 0, null, 10, 15),
                db.getMentionWithId(mentionDoc1AliceId));
    }

    /**
     * Test of testGetEntitiesMentionedNearEntitiesByIndex method, of class VizLincRDBMem.
     */
    @Test
    public void testGetEntitiesMentionedNearEntitiesByIndex() {
        Map<Integer, EntityCounts> result1 =
                db.getEntitiesMentionedNearEntitiesByIndex(
                    Arrays.asList(entityAliceId),
                    Arrays.asList(entityBobId, entityLexingtonId),
                    Arrays.asList(document1Id),
                    1);
        Map<Integer, EntityCounts> expectedResult1 = new HashMap();
        expectedResult1.put(entityBobId, new EntityCounts(1, 1));
        assertEquals(expectedResult1, result1);

        Map<Integer, EntityCounts> result2 =
                db.getEntitiesMentionedNearEntitiesByIndex(
                    Arrays.asList(entityBobId),
                    Arrays.asList(entityAliceId, entityLexingtonId),
                    Arrays.asList(document1Id),
                    2);
        Map<Integer, EntityCounts> expectedResult2 = new HashMap();
        expectedResult2.put(entityAliceId, new EntityCounts(1, 1));
        expectedResult2.put(entityLexingtonId, new EntityCounts(1, 1));
        assertEquals(expectedResult2, result2);

        Map<Integer, EntityCounts> result3 =
                db.getEntitiesMentionedNearEntitiesByIndex(
                    Arrays.asList(entityAliceId),
                    Arrays.asList(entityAliceId, entityBobId, entityLexingtonId),
                    Arrays.asList(document1Id),
                    5);
        Map<Integer, EntityCounts> expectedResult3 = new HashMap();
        expectedResult3.put(entityAliceId, new EntityCounts(1, 1));
        expectedResult3.put(entityBobId, new EntityCounts(2, 1));
        expectedResult3.put(entityLexingtonId, new EntityCounts(1, 1));
        assertEquals(expectedResult3, result3);

        Map<Integer, EntityCounts> result4 =
                db.getEntitiesMentionedNearEntitiesByIndex(
                    Arrays.asList(entityAliceId),
                    Arrays.asList(entityAliceId, entityBobId, entityLexingtonId),
                    Arrays.asList(document1Id, document2Id),
                    5);
        Map<Integer, EntityCounts> expectedResult4 = new HashMap();
        expectedResult4.put(entityAliceId, new EntityCounts(2, 2));
        expectedResult4.put(entityBobId, new EntityCounts(3, 2));
        expectedResult4.put(entityLexingtonId, new EntityCounts(1, 1));
        assertEquals(expectedResult4, result4);

    }


    /**
     * Test of testGetEntitiesMentionedNearEntitiesByTextOffset method, of class VizLincRDBMem.
     */
    @Test
    public void testGetEntitiesMentionedNearEntitiesByTextOffset() {
        Map<Integer, EntityCounts> result1 =
                db.getEntitiesMentionedNearEntitiesByTextOffset(
                    Arrays.asList(entityAliceId),
                    Arrays.asList(entityBobId, entityLexingtonId),
                    Arrays.asList(document1Id),
                    10);
        Map<Integer, EntityCounts> expectedResult1 = new HashMap();
        expectedResult1.put(entityBobId, new EntityCounts(1, 1));
        assertEquals(expectedResult1, result1);

        Map<Integer, EntityCounts> result2 =
                db.getEntitiesMentionedNearEntitiesByTextOffset(
                    Arrays.asList(entityBobId),
                    Arrays.asList(entityAliceId, entityLexingtonId),
                    Arrays.asList(document1Id),
                    20);
        Map<Integer, EntityCounts> expectedResult2 = new HashMap();
        expectedResult2.put(entityAliceId, new EntityCounts(1, 1));
        expectedResult2.put(entityLexingtonId, new EntityCounts(1, 1));
        assertEquals(expectedResult2, result2);

        Map<Integer, EntityCounts> result3 =
                db.getEntitiesMentionedNearEntitiesByTextOffset(
                    Arrays.asList(entityAliceId),
                    Arrays.asList(entityAliceId, entityBobId, entityLexingtonId),
                    Arrays.asList(document1Id),
                    40);
        Map<Integer, EntityCounts> expectedResult3 = new HashMap();
        expectedResult3.put(entityAliceId, new EntityCounts(1, 1));
        expectedResult3.put(entityBobId, new EntityCounts(2, 1));
        expectedResult3.put(entityLexingtonId, new EntityCounts(1, 1));
        assertEquals(expectedResult3, result3);

        Map<Integer, EntityCounts> result4 =
                db.getEntitiesMentionedNearEntitiesByTextOffset(
                    Arrays.asList(entityAliceId),
                    Arrays.asList(entityAliceId, entityBobId, entityLexingtonId),
                    Arrays.asList(document1Id, document2Id),
                    40);
        Map<Integer, EntityCounts> expectedResult4 = new HashMap();
        expectedResult4.put(entityAliceId, new EntityCounts(2, 2));
        expectedResult4.put(entityBobId, new EntityCounts(3, 2));
        expectedResult4.put(entityLexingtonId, new EntityCounts(1, 1));
        assertEquals(expectedResult4, result4);
    }


    /**
     * Test of getDocumentsForEntitiesMentionedNearEntityByIndex method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDocumentsForEntitiesMentionedNearEntityByIndex() {
        Set<Integer> result1 =
                db.getDocumentsForEntitiesMentionedNearEntityByIndex(
                    Arrays.asList(entityAliceId, entityLexingtonId),
                    entityBobId,
                    Arrays.asList(document1Id, document2Id),
                   5);
        Set<Integer> expectedResult1 = new HashSet();
        expectedResult1.add(document1Id);
        expectedResult1.add(document2Id);
        assertEquals(expectedResult1, result1);

        Set<Integer> result2 =
                db.getDocumentsForEntitiesMentionedNearEntityByIndex(
                    Arrays.asList(entityLexingtonId),
                    entityBobId,
                    Arrays.asList(document1Id, document2Id),
                    1);
        Set<Integer> expectedResult2 = new HashSet();
        expectedResult2.add(document1Id);
        assertEquals(expectedResult2, result2);

        Set<Integer> result3 =
                db.getDocumentsForEntitiesMentionedNearEntityByIndex(
                Arrays.asList(entityLexingtonId),
                entityAliceId,
                Arrays.asList(document1Id, document2Id),
                1);
        Set<Integer> expectedResult3 = new HashSet();
        assertEquals(expectedResult3, result3);
    }

    /**
     * Test of getDocumentsForEntitiesMentionedNearEntityByTextOffset method, of class VizLincRDBMem.
     */
    @Test
    public void testGetDocumentsForEntitiesMentionedNearEntityByTextOffset() {
        Set<Integer> result1 =
                db.getDocumentsForEntitiesMentionedNearEntityByTextOffset(
                    Arrays.asList(entityAliceId, entityLexingtonId),
                    entityBobId,
                    Arrays.asList(document1Id, document2Id),
                   10);
        Set<Integer> expectedResult1 = new HashSet();
        expectedResult1.add(document1Id);
        expectedResult1.add(document2Id);
        assertEquals(expectedResult1, result1);

        Set<Integer> result2 =
                db.getDocumentsForEntitiesMentionedNearEntityByTextOffset(
                    Arrays.asList(entityLexingtonId),
                    entityBobId,
                    Arrays.asList(document1Id, document2Id),
                    30);
        Set<Integer> expectedResult2 = new HashSet();
        expectedResult2.add(document1Id);
        assertEquals(expectedResult2, result2);

        Set<Integer> result3 =
                db.getDocumentsForEntitiesMentionedNearEntityByTextOffset(
                Arrays.asList(entityLexingtonId),
                entityAliceId,
                Arrays.asList(document1Id, document2Id),
                10);
        Set<Integer> expectedResult3 = new HashSet();
        assertEquals(expectedResult3, result3);

    }

}
