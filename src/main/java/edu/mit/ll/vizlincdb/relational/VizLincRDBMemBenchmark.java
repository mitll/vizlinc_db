package edu.mit.ll.vizlincdb.relational;

import edu.mit.ll.vizlincdb.document.Document;
import edu.mit.ll.vizlincdb.util.ElapsedTime;
import edu.mit.ll.vizlincdb.entity.EntityCounts;
import edu.mit.ll.vizlincdb.entity.OrganizationEntity;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class VizLincRDBMemBenchmark {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, Exception {
        ElapsedTime t = new ElapsedTime();
        VizLincRDBMem db = new VizLincRDBMem("J:/something.h2");

        String ORGANIZATION = "SOME ORGANIZATION";

        List<Document> allDocs = db.getDocuments();
        t.done("db.getDocuments()");

        Set<Integer> allDocIdsSet = new HashSet<Integer>();
        for (Document d: allDocs) {
            allDocIdsSet.add(d.getId());
        }

        t.reset();
        List<OrganizationEntity> orgs = db.getOrganizationEntities();
        t.done("db.getOrganizationEntities()");

        int orgId = 0;
        for (OrganizationEntity e : orgs) {
            if (e.getText().equals(ORGANIZATION)) {
                orgId = e.getId();
                break;
            }
        }
        if (orgId == 0) {
            throw new Exception("Couldn't find organization: " + ORGANIZATION);
        }

        List<Integer> docIDs = db.getDocumentIdsWithAllOfTheseEntityIds(Arrays.asList(orgId));
        t.done("db.getDocumentIdsWithAllOfTheseEntityIds(Arrays.asList(orgID)");

        List<Integer> entityIDs = db.getEntityIdsInAnyofTheseDocumentIds(docIDs);
        t.done("db.getEntityIdsInAnyofTheseDocumentIds(docIDs)");

        Map<Integer, Integer> documentCounts = db.getDocumentCountsForEntitiesInDocuments(entityIDs, docIDs);
        t.done("db.getDocumentCountsForEntitiesInDocuments(entityIDs, docIDs)");

        Map<Integer, Integer> mentionCounts = db.getMentionCountsForEntitiesInDocuments(entityIDs, docIDs);
        t.done("db.getMentionCountsForEntitiesInDocuments(entityIDs, docIDs)");

        Map<Integer, EntityCounts> entityIdCountsByTextOffset = db.getEntitiesMentionedNearEntitiesByTextOffset(Arrays.asList(orgId), entityIDs, docIDs, 5);
        t.done("db.getEntitiesMentionedNearEntitiesByTextOffset(Arrays.asList(orgId), entityIDs, docIDs, 100)");

        Map<Integer, EntityCounts> entityIdCountsByIndex = db.getEntitiesMentionedNearEntitiesByIndex(Arrays.asList(orgId), entityIDs, docIDs, 5);
        t.done("db.getEntitiesMentionedNearEntities(Arrays.asList(orgId), entityIDs, docIDs, 5)");

        System.out.println("total memory: " + Runtime.getRuntime().totalMemory());
    }
}
