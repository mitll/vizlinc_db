package edu.mit.ll.vizlincdb.graph;

import edu.mit.ll.vizlincdb.geo.GeoBoundingBox;
import edu.mit.ll.vizlincdb.geo.GeoLocation;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
public class VizLincDBTest {

    // Saved Vertex objects and their ids, for testing comparisons.
    public static VizLincDB vizlincDB;
    public static Vertex document1;
    public static Object document1Id;
    public static String document1Text;
    public static Vertex document2;
    public static Object document2Id;
    public static String document2Text;
    public static Vertex mention1Alice;
    public static Object mention1AliceId;
    public static Vertex mention1Bob1;
    public static Vertex mention1Bob2;
    public static Object mention1Bob1Id;
    public static Object mention1Bob2Id;
    public static Vertex mention1Lexington;
    public static Object mention1LexingtonId;
    public static Vertex mention2Alice;
    public static Object mention2AliceId;
    public static Vertex mention2Bob;
    public static Object mention2BobId;
    public static Vertex entityAlice;
    public static Object entityAliceId;
    public static Vertex entityBob;
    public static Object entityBobId;
    public static Vertex entityLexington;
    public static Object entityLexingtonId;

    // Sets of Element ids for testing comparisons.
    public static Set<Object> documentIds;
    public static Set<Object> mentionIds;
    public static Set<Object> document1MentionIds;
    public static Set<Object> document2MentionIds;
    public static Set<Object> personMentionIds;
    public static Set<Object> personRelativeMentionIds;
    public static Set<Object> aliceMentionIds;
    public static Set<Object> documentToMentionEdgeIds;
    public static Set<Object> entityIds;
    public static Set<Object> personEntityIds;
    public static Set<Object> locationEntityIds;
    public static Set<Object> createdBy2EntityIds;
    public static Set<Object> document1EntityIds;
    public static Set<Object> document2EntityIds;

    public VizLincDBTest() {
    }
    @ClassRule
    public static TemporaryFolder tempDir = new TemporaryFolder();

    @BeforeClass
    public static void setUpClass() throws IOException {
        // Create a new database with a few nodes. Remember sets of interesting objects fo testing comparisons.

        vizlincDB = new VizLincDB(tempDir.newFolder(), true);
        document1Text = "document 1 text";
        document1 = vizlincDB.newDocument("doc1", "dir1/doc1", document1Text);
        document2Text = "document 2 text";
        document2 = vizlincDB.newDocument("doc2", "dir2/doc2", document2Text);

        mention1Alice = vizlincDB.newMention(document1, "PERSON", "Alice", 0, null, 10, 10 + "Alice".length());
        mention1Bob1 = vizlincDB.newMention(document1, "PERSON", "Bob", 0, null, 20, 20 + "Bob".length());
        mention1Bob2 = vizlincDB.newMention(document1, "PERSON", "Bob", 0, null, 20, 20 + "Bob".length());
        mention1Lexington = vizlincDB.newMention(document1, "LOCATION", "Lexington", 0, 999, 20, 20 + "Lexington".length());
        mention1LexingtonId = mention1Lexington.getId();
        mention2Alice = vizlincDB.newMention(document2, "PERSON", "Alice Ailey", 0, null, 10, 10 + "Alice Ailey".length());
        mention2Bob = vizlincDB.newMention(document2, "PERSON", "Bob Bean", 0, null, 20, 20 + "Bob Bean".length());

        entityAlice = vizlincDB.newEntity(Arrays.asList(mention1Alice, mention2Alice), "PERSON", "Alice Ailey", "createdby1");
        entityBob = vizlincDB.newEntity(Arrays.asList(mention1Bob1, mention1Bob2, mention2Bob), "PERSON", "Bob Bean", "createdby2");
        entityLexington = vizlincDB.newEntity(Arrays.asList(mention1Lexington), "LOCATION", "Lexington", "createdby2");

        vizlincDB.commit();

        document1Id = document1.getId();
        document2Id = document2.getId();
        mention1AliceId = mention1Alice.getId();
        mention1Bob1Id = mention1Bob1.getId();
        mention1Bob2Id = mention1Bob2.getId();
        mention2AliceId = mention2Alice.getId();
        mention2BobId = mention2Bob.getId();

        documentIds = new HashSet<Object>(Arrays.asList(document1Id, document2Id));
        document1MentionIds = new HashSet<Object>(Arrays.asList(mention1AliceId, mention1Bob1Id, mention1Bob2Id, mention1LexingtonId));
        document2MentionIds = new HashSet<Object>(Arrays.asList(mention2AliceId, mention2BobId));
        mentionIds = new HashSet<Object>(Arrays.asList(mention1AliceId, mention1Bob1Id, mention1Bob2Id, mention1LexingtonId, mention2AliceId, mention2BobId));
        personMentionIds = new HashSet<Object>(Arrays.asList(mention1AliceId, mention1Bob1Id, mention1Bob2Id, mention2AliceId, mention2BobId));
        personRelativeMentionIds = new HashSet<Object>(Arrays.asList(mention1AliceId, mention2BobId));
        aliceMentionIds = new HashSet<Object>(Arrays.asList(mention1AliceId, mention2AliceId));

        entityAliceId = entityAlice.getId();
        entityBobId = entityBob.getId();
        entityLexingtonId = entityLexington.getId();

        documentToMentionEdgeIds = new HashSet<Object>();
        for (Object id: documentIds) {
            Vertex node = vizlincDB.getNodeWithId(id);
            for (Edge e: node.getEdges(Direction.OUT, L_DOCUMENT_TO_MENTION)) {
                documentToMentionEdgeIds.add(e.getId());
            }
        }

        entityIds = new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId, entityLexingtonId));
        personEntityIds = new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId));
        locationEntityIds = new HashSet<Object>(Arrays.asList(entityLexingtonId));
        createdBy2EntityIds = new HashSet<Object>(Arrays.asList(entityBobId, entityLexingtonId));
        document1EntityIds = new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId, entityLexingtonId));
        document2EntityIds = new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId));

    }

    @AfterClass
    public static void tearDownClass() throws InterruptedException {
        boolean ex = false;
        vizlincDB.shutdown();
        //  Try to do something after shutdown that should not work.
        try {
            vizlincDB.getEntitiesOfType("FOO");
        } catch (NullPointerException e) {
            ex = true;
        } finally {
            // The exception should have been thrown.
            assertTrue(ex);
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of constructors
     */
    @Test
    public void testConstructors() throws IOException {
        File dir = tempDir.newFolder();
        VizLincDB db = new VizLincDB(dir, true);   // newDB = true
        db.shutdown();
        db = new VizLincDB(dir, false);    // newDB = false
        db.shutdown();
    }

    /**
     * Test of commit method, of class VizLincDB.
     */
    @Test
    public void testCommit() {
        Vertex test = vizlincDB.newEntity("TESTCOMMIT", "abc", "testing");
        // Some dbs (e.g., Titan) do transactional scope, so remember id.
        Object testId = test.getId();
        vizlincDB.commit();
        Iterator<Vertex> it = vizlincDB.getEntitiesOfType("TESTCOMMIT").iterator();
        assertTrue(it.hasNext());
        assertEquals(testId, it.next().getId());
        vizlincDB.deleteNode(vizlincDB.getNodeWithId(testId));
        vizlincDB.commit();
        it = vizlincDB.getEntitiesOfType("TESTCOMMIT").iterator();
        assertFalse(it.hasNext());
    }

    /**
     * Test of rollback method, of class VizLincDB.
     */
    @Test
    public void testRollback() {
        if (!vizlincDB.isTransactional()) {
            return;
        }
        vizlincDB.newEntity("TESTROLLBACK", "abc", "testing");
        vizlincDB.rollback();
        Iterator<Vertex> it = vizlincDB.getEntitiesOfType("TESTROLLBACK").iterator();
        assertFalse(it.hasNext());
    }

    /**
     * Test of deleteNode method, of class VizLincDB.
     */
    @Test
    public void testDeleteNode() {
        Vertex test = vizlincDB.newEntity("TESTDELETE", "abc", "testing");
        vizlincDB.deleteNode(test);
        vizlincDB.commit();
        Iterator<Vertex> it = vizlincDB.getEntitiesOfType("TESTDELETE").iterator();
        assertFalse(it.hasNext());
    }

    /**
     * Test of getDocuments method, of class VizLincDB.
     */
    @Test
    public void testGetDocuments() {
        assertEquals(documentIds, VizLincDB.makeSetOfIds(vizlincDB.getDocuments()));
    }

    /**
     * Test of getMentions method, of class VizLincDB.
     */
    @Test
    public void testGetMentions() {
        assertEquals(mentionIds, VizLincDB.makeSetOfIds(vizlincDB.getMentions()));
    }

    /**
     * Test of getMentionsOfType method, of class VizLincDB.
     */
    @Test
    public void testGetMentionsOfType() {
        assertEquals(personMentionIds, VizLincDB.makeSetOfIds(vizlincDB.getMentionsOfType("PERSON")));
    }

    /**
     * Test of getMentionsForEntity method, of class VizLincDB.
     */
    @Test
    public void testGetMentionsForEntity() {
        assertEquals(aliceMentionIds, VizLincDB.makeSetOfIds(vizlincDB.getMentionsForEntity(entityAlice)));
    }

    /**
     * Test of getEdges method, of class VizLincDB.
     */
    @Test
    public void testGetEdges() {
        assertEquals(documentToMentionEdgeIds, VizLincDB.makeSetOfIds(vizlincDB.getEdges(L_DOCUMENT_TO_MENTION)));
    }

    /**
     * Test of getEntities method, of class VizLincDB.
     */
    @Test
    public void testGetEntities() {
       assertEquals(entityIds, VizLincDB.makeSetOfIds(vizlincDB.getEntities()));
    }

    /**
     * Test of getEntitiesOfType method, of class VizLincDB.
     */
    @Test
    public void testGetEntitiesOfType() {
        assertEquals(locationEntityIds, VizLincDB.makeSetOfIds(vizlincDB.getEntitiesOfType("LOCATION")));
        assertEquals(personEntityIds, VizLincDB.makeSetOfIds(vizlincDB.getEntitiesOfType("PERSON")));
    }

    /**
     * Test of getEntitiesCreatedBy method, of class VizLincDB.
     */
    @Test
    public void testGetEntitiesCreatedBy() {
        assertEquals(createdBy2EntityIds, VizLincDB.makeSetOfIds(vizlincDB.getEntitiesCreatedBy("createdby2")));
    }

    /**
     * Test of getDocumentForMention method, of class VizLincDB.
     */
    @Test
    public void testGetDocumentForMention() throws Exception {
        assertEquals(document1Id, vizlincDB.getDocumentForMention(mention1Lexington).getId());
    }

    /**
     * Test of getDocumentsWithMentions method, of class VizLincDB.
     */
    @Test
    public void testGetDocumentsForAnyMentions() {
       assertEquals(new HashSet<Object>(Arrays.asList(document1Id)),
                     VizLincDB.makeSetOfIds(vizlincDB.getDocumentsWithMentions(Arrays.asList(mention1Alice, mention1Bob1))));
        assertEquals(new HashSet<Object>(Arrays.asList(document1Id, document2Id)),
                VizLincDB.makeSetOfIds(vizlincDB.getDocumentsWithMentions(Arrays.asList(mention1Alice, mention2Bob))));
    }

    /**
     * Test of getMentionsInDocument method, of class VizLincDB.
     */
    @Test
    public void testGetMentionsInDocument() {
        assertEquals(document1MentionIds, VizLincDB.makeSetOfIds(vizlincDB.getMentionsInDocument(document1)));
    }

    /**
     * Test of getEntitiesInDocument method, of class VizLincDB.
     */
    @Test
    public void testGetEntitiesInDocument() {
       assertEquals(document1EntityIds, VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInDocument(document1)));
       assertEquals(document2EntityIds, VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInDocument(document2)));
}

    /**
     * Test of newDocument method, of class VizLincDB.
     */
    @Test
    public void testNewDocument() {
        Vertex document = vizlincDB.newDocument("documentname", "documentpath", "documenttext");
        assertEquals("documentname", document.getProperty(P_DOCUMENT_NAME));
        assertEquals("documentpath", document.getProperty(P_DOCUMENT_PATH));
        assertEquals("documenttext", document.getProperty(P_DOCUMENT_TEXT));
        vizlincDB.deleteNode(document);
        vizlincDB.commit();
    }

    /**
     * Test of newMention method, of class VizLincDB.
     */
    @Test
    public void testNewMention_6args() {
        Vertex mention = vizlincDB.newMention(document1, "TYPE", "text", 6, null, 50, 60);
        assertEquals("TYPE", mention.getProperty(P_MENTION_TYPE));
        assertEquals("text", mention.getProperty(P_MENTION_TEXT));
        assertEquals(6, mention.getProperty(P_MENTION_INDEX));
        assertEquals(50, mention.getProperty(P_MENTION_TEXT_START));
        assertEquals(60, mention.getProperty(P_MENTION_TEXT_STOP));
        boolean found = false;
        for (Vertex mentionInDoc : vizlincDB.getMentionsInDocument(document1)) {
            if (mentionInDoc.getId() == mention.getId()) {
                found = true;
                break;
            }
        }
        assertTrue("mention not found in document", found);

        vizlincDB.deleteNode(mention);
        vizlincDB.commit();
   }

    /**
     * Test of newEntity method, of class VizLincDB.
     */
    @Test
    public void testNewEntity_5args() {
        Vertex entity = vizlincDB.newEntity(Arrays.asList(mention1Alice, mention1Bob1), "ETYPE", "alicebob", "ecreatedby5");
        assertEquals("ETYPE", entity.getProperty(P_ENTITY_TYPE));
        assertEquals("alicebob", entity.getProperty(P_ENTITY_TEXT));
        assertEquals("ecreatedby5", entity.getProperty(P_CREATED_BY));

        Set<Object> fetchedMentionIds = VizLincDB.makeSetOfIds(vizlincDB.getMentionsForEntity(entity));
        assertEquals(new HashSet<Object>(Arrays.asList(mention1AliceId, mention1Bob1Id)), fetchedMentionIds);
        vizlincDB.deleteNode(entity);
        vizlincDB.commit();
    }

    /**
     * Test of newEntity method, of class VizLincDB.
     */
    @Test
    public void testNewEntity_4args() {
        Vertex entity = vizlincDB.newEntity("ETYPE", "etext", "ecreatedby4");
        assertEquals(entity.getProperty(P_ENTITY_TYPE), "ETYPE");
        assertEquals(entity.getProperty(P_ENTITY_TEXT), "etext");
        assertEquals(entity.getProperty(P_CREATED_BY), "ecreatedby4");
        vizlincDB.deleteNode(entity);
        vizlincDB.commit();
    }

    /**
     * Test of connectEntityToMentionsAndDocuments method, of class VizLincDB.
     */
    @Test
    public void testConnectEntityToMentionsAndDocuments() {
        Vertex entity = vizlincDB.newEntity("ETYPE", "alicebob", "ecreatedby5");
        vizlincDB.connectEntityToMentionsAndDocuments(entity, Arrays.asList(mention1Alice, mention1Bob1));
        assertEquals(new HashSet<Object>(Arrays.asList(mention1AliceId, mention1Bob1Id)),
                     VizLincDB.makeSetOfIds(vizlincDB.getMentionsForEntity(entity)));
        vizlincDB.deleteNode(entity);
        vizlincDB.commit();
    }

    /**
     * Test of shutdown method, of class VizLincDB.
     */
    @Test
    public void testShutdown() {
        // Dummy. This is tested in tearDownClass().
    }

    /**
     * Test of getNodeWithId method, of class VizLincDB.
     */
    @Test
    public void testGetNodeWithId() {
        for (Object nodeId: mentionIds) {
            assertEquals(vizlincDB.getNodeWithId(nodeId).getId(), nodeId);
        }
    }

    /**
     * Test of getEdgeWithId method, of class VizLincDB.
     */
    @Test
    public void testGetEdgeWithId() {
        for (Object edgeId: documentToMentionEdgeIds) {
            assertEquals(vizlincDB.getEdgeWithId(edgeId).getId(), edgeId);
        }
    }

    /**
     * Test of getDocumentTextFromText method, of class VizLincDB.
     */
    @Test
    public void testGetDocumentText() throws Exception {
        assertEquals(document1Text, document1.getProperty(P_DOCUMENT_TEXT));
        assertEquals(document2Text, document2.getProperty(P_DOCUMENT_TEXT));
    }

    /**
     * Test of getDocumentsForEntity method, of class VizLincDB.
     */
    @Test
    public void testGetDocumentsForEntity() {
        assertEquals(documentIds, VizLincDB.makeSetOfIds(vizlincDB.getDocumentsForEntity(entityAlice)));
        assertEquals(new HashSet<Object>(Arrays.asList(document1Id)),
                     VizLincDB.makeSetOfIds(vizlincDB.getDocumentsForEntity(entityLexington)));

    }

    /**
     * Test of getDocumentsForAnyEntities method, of class VizLincDB.
     */
    @Test
    public void testGetDocumentsForAnyEntities() {
        assertEquals(documentIds,
                     VizLincDB.makeSetOfIds(vizlincDB.getDocumentsForAnyEntities(Arrays.asList(entityAlice, entityBob))));
        assertEquals(new HashSet<Object>(Arrays.asList(document1Id)),
                     VizLincDB.makeSetOfIds(vizlincDB.getDocumentsForAnyEntities(Arrays.asList(entityLexington))));
    }

    /**
     * Test of makeSet method, of class VizLincDB.
     */
    @Test
    public void testMakeSet() {
        Set<Integer> set = new HashSet<Integer>(Arrays.asList(1,2,3));
        Iterable<Integer> iter = set;
        assertEquals(set, VizLincDB.makeSet(iter));
    }

    /**
     * Test of makeList method, of class VizLincDB.
     */
    @Test
    public void testMakeList() {
        List<Integer> list = Arrays.asList(1,2,3);
        Iterable<Integer> iter = Arrays.asList(1,2,3);
        assertEquals(list, VizLincDB.makeList(iter));
    }

    /**
     * Test of makeSetOfIds method, of class VizLincDB.
     */
    @Test
    public void testMakeSetOfIds() {
        Iterable<Vertex> iter = Arrays.asList(document1, document2);
        assertEquals(documentIds, VizLincDB.makeSetOfIds(iter));
    }

    /**
     * Test of makeListOfIds method, of class VizLincDB.
     */
    @Test
    public void testMakeListOfIds() {
        Iterable<Vertex> iter = Arrays.asList(document1, document2);
        assertEquals(Arrays.asList(document1Id, document2Id), VizLincDB.makeListOfIds(iter));
    }

    /**
     * Test of getMentionsInDocumentOfType method, of class VizLincDB.
     */
    @Test
    public void testGetMentionsInDocumentOfType() {
        assertEquals(new HashSet<Object>(Arrays.asList(mention1AliceId, mention1Bob1Id, mention1Bob2Id)),
                VizLincDB.makeSetOfIds(vizlincDB.getMentionsInDocumentOfType(document1, "PERSON")));
    }

    /**
     * Test of outEdgeExists method, of class VizLincDB.
     */
    @Test
    public void testOutEdgeExists() {
        assertTrue(vizlincDB.outEdgeExists(document1, mention1Alice, L_DOCUMENT_TO_MENTION));
        assertFalse(vizlincDB.outEdgeExists(document1, mention2Bob, L_DOCUMENT_TO_MENTION));
    }

    /**
     * Test of edgeExists method, of class VizLincDB.
     */
    @Test
    public void testEdgeExists() {
        assertTrue(vizlincDB.edgeExists(document1, mention1Alice, L_DOCUMENT_TO_MENTION));
        assertTrue(vizlincDB.edgeExists(mention1Alice, document1, L_DOCUMENT_TO_MENTION));
        assertFalse(vizlincDB.edgeExists(document1, mention2Bob, L_DOCUMENT_TO_MENTION));
    }

    /**
     * Test of allInEdgesExist method, of class VizLincDB.
     */
    @Test
    public void testAllInEdgesExist() {
        assertTrue(vizlincDB.allInEdgesExist(entityAlice, Arrays.asList(document1, document2), L_DOCUMENT_TO_ENTITY));
        assertFalse(vizlincDB.allInEdgesExist(entityLexington, Arrays.asList(document1, document2), L_DOCUMENT_TO_ENTITY));
    }


    /**
     * Test of allOutEdgesExist method, of class VizLincDB.
     */
    @Test
    public void testAllOutEdgesExist() {
        assertTrue(vizlincDB.allOutEdgesExist(document1, Arrays.asList(mention1Alice, mention1Bob1), L_DOCUMENT_TO_MENTION));
        assertFalse(vizlincDB.allOutEdgesExist(document1, Arrays.asList(mention1Alice, mention2Alice), L_DOCUMENT_TO_MENTION));
    }

    /**
     * Test of allEdgesExist method, of class VizLincDB.
     */
    @Test
    public void testAllEdgesExist() {
        assertTrue(vizlincDB.allEdgesExist(document1, Arrays.asList(mention1Alice, mention1Bob1), L_DOCUMENT_TO_MENTION));
        assertTrue(vizlincDB.allEdgesExist(mention1Alice, Arrays.asList(document1), L_DOCUMENT_TO_MENTION));
        assertFalse(vizlincDB.allEdgesExist(document1, Arrays.asList(mention1Alice, mention2Alice), L_DOCUMENT_TO_MENTION));
        assertFalse(vizlincDB.allEdgesExist(mention1Alice, Arrays.asList(document1, document2), L_DOCUMENT_TO_MENTION));
    }

    /**
     * Test of getDocumentsWithAllOfTheseEntities method, of class VizLincDB.
     */
    @Test
    public void testGetDocumentsWithAllOfTheseEntities() {
        assertEquals(new HashSet<Object>(Arrays.asList(document1Id)),
                VizLincDB.makeSetOfIds(vizlincDB.getDocumentsWithAllOfTheseEntities(Arrays.asList(entityAlice, entityBob, entityLexington))));
        assertEquals(new HashSet<Object>(Arrays.asList(document1Id, document2Id)),
                VizLincDB.makeSetOfIds(vizlincDB.getDocumentsWithAllOfTheseEntities(Arrays.asList(entityAlice, entityBob))));
    }

    /**
     * Test of getDocumentsWithMentions method, of class VizLincDB.
     */
    @Test
    public void testGetDocumentsWithMentions() {
        // Test for returning too many (may be duplicates).
        assertEquals(1,
                VizLincDB.makeListOfIds(vizlincDB.getDocumentsWithMentions(Arrays.asList(mention1Alice, mention1Bob1, mention1Bob2))).size());
        assertEquals(2,
                VizLincDB.makeListOfIds(vizlincDB.getDocumentsWithMentions(Arrays.asList(mention1Alice, mention1Bob1, mention1Bob2, mention2Alice, mention2Bob))).size());
        assertEquals(new HashSet<Object>(Arrays.asList(document1Id)),
                VizLincDB.makeSetOfIds(vizlincDB.getDocumentsWithMentions(Arrays.asList(mention1Alice, mention1Bob1, mention1Bob2))));
        assertEquals(new HashSet<Object>(Arrays.asList(document1Id, document2Id)),
                VizLincDB.makeSetOfIds(vizlincDB.getDocumentsWithMentions(Arrays.asList(mention1Alice, mention2Bob))));
        assertEquals(new HashSet<Object>(Arrays.asList(document2Id)),
                VizLincDB.makeSetOfIds(vizlincDB.getDocumentsWithMentions(Arrays.asList(mention2Alice, mention2Bob))));
   }

    /**
     * Test of getEntitiesInAnyOfTheseDocuments method, of class VizLincDB.
     */
    @Test
    public void testGetEntitiesinAnyOfTheseDocuments() {
        assertEquals(new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId, entityLexingtonId)),
                VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInAnyOfTheseDocuments(Arrays.asList(document1, document2))));
        assertEquals(new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId)),
                VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInAnyOfTheseDocuments(Arrays.asList(document2))));
     }

    /**
     * Test of getEntitiesInAllOfTheseDocuments method, of class VizLincDB.
     */
    @Test
    public void testGetEntitiesInAllOfTheseDocuments() {
        assertEquals(new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId, entityLexingtonId)),
                VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInAllOfTheseDocuments(Arrays.asList(document1))));
        assertEquals(new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId)),
                VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInAllOfTheseDocuments(Arrays.asList(document1, document2))));
        assertEquals(new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId)),
                VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInAllOfTheseDocuments(Arrays.asList(document2))));
    }

    /**
     * Test of getEntitiesInAnyOfTheseDocuments method, of class VizLincDB.
     */
    @Test
    public void testGetEntitiesInAnyOfTheseDocuments() {
        assertEquals(new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId, entityLexingtonId)),
                VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInAnyOfTheseDocuments(Arrays.asList(document1))));
        assertEquals(new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId, entityLexingtonId)),
                VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInAnyOfTheseDocuments(Arrays.asList(document1, document2))));
        assertEquals(new HashSet<Object>(Arrays.asList(entityAliceId, entityBobId)),
                VizLincDB.makeSetOfIds(vizlincDB.getEntitiesInAnyOfTheseDocuments(Arrays.asList(document2))));
    }

    /**
     * Test of setGeoLocations method, of class VizLincDB.
     */
    @Test
    public void testSetGetGeoLocations() {
        Vertex locationNode = vizlincDB.newEntity(E_LOCATION, "Lexington", "test");
        assertNull(vizlincDB.getGeoLocations(locationNode));

        GeoLocation loc1 = new GeoLocation(1.0, 2.0, null, "loc1", null, "NGA1", "US", "TESTSOURCE");
        GeoLocation loc2 = new GeoLocation(3.0, 4.0, null, "loc2", null, "NGA2", "FR", "TESTSOURCE");
        GeoBoundingBox bbox1 = new GeoBoundingBox(10.0, 11.0, 12.0, 13.0);
        GeoBoundingBox bbox2 = new GeoBoundingBox(20.0, 21.0, 22.0, 23.0);
        GeoLocation loc3 = new GeoLocation(4.0, 5.0, bbox1, "loc3", "osm3", null, "US", "TESTSOURCE");
        GeoLocation loc4 = new GeoLocation(6.0, 7.0, bbox2, "loc4", "osm4", null, "US", "TESTSOURCE");

        vizlincDB.setGeoLocations(locationNode, new GeoLocation[] {loc1, loc2});
        GeoLocation[] loc1loc2 = vizlincDB.getGeoLocations(locationNode);
        assertEquals(loc1, loc1loc2[0]);
        assertEquals(loc2, loc1loc2[1]);
        vizlincDB.setGeoLocations(locationNode, null);

        vizlincDB.setGeoLocations(locationNode, new GeoLocation[] {loc3, loc4});
        GeoLocation[] loc3loc4 = vizlincDB.getGeoLocations(locationNode);
        assertEquals(loc3, loc3loc4[0]);
        assertEquals(loc4, loc3loc4[1]);
        vizlincDB.setGeoLocations(locationNode, null);

        vizlincDB.setGeoLocations(locationNode, new GeoLocation[] {loc1, loc3});
        GeoLocation[] loc1loc3 = vizlincDB.getGeoLocations(locationNode);
        assertEquals(loc1, loc1loc2[0]);
        assertEquals(loc3, loc1loc3[1]);
        vizlincDB.setGeoLocations(locationNode, null);

        assertNull(vizlincDB.getGeoLocations(locationNode));

    }

    /**
     * Test of getMentionsInDocumentsForEntity method, of class VizLincDB.
     */
    @Test
    public void testGetMentionsInDocumentsForEntity() throws Exception {
        assertEquals(new HashSet<Object>(Arrays.asList(mention1AliceId)),
            VizLincDB.makeSetOfIds(vizlincDB.getMentionsInDocumentsForEntity(Arrays.asList(document1), entityAlice)));
    }



    /**
     * Test of count method, of class VizLincDB.
     */
    @Test
    public void testCount() {
        ArrayList<Integer> al = new ArrayList<Integer>();
        assertEquals(0, VizLincDB.count(al));
        al.add(1); al.add(2);
        assertEquals(2, VizLincDB.count(al));
    }
}
