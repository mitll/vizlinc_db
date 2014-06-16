package edu.mit.ll.vizlincdb.relational;

import edu.mit.ll.vizlincdb.document.Document;
import edu.mit.ll.vizlincdb.util.ElapsedTime;
import edu.mit.ll.vizlincdb.entity.Mention;
import edu.mit.ll.vizlincdb.entity.OrganizationEntity;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class VizLincRDBBenchmark {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, Exception {
        ElapsedTime t = new ElapsedTime();
        VizLincRDB db = new VizLincRDB("J:/something.h2");

        String ORGANIZATION = "SOME ORGANIZATION";
        
        t.reset();
        List<Mention> allMentions = db.getMentionsWithoutText();
        t.done("db.getMentions()");

        List<Document> allDocs = db.getDocuments();
        t.done("db.getDocuments()");

        List<OrganizationEntity> orgs = db.getOrganizationEntities();
        t.done("db.getOrganizationEntities()");

        int orgID = 0;
        for (OrganizationEntity e : orgs) {
            if (e.getText().equals(ORGANIZATION)) {
                orgID = e.getId();
                break;
            }
        }
        if (orgID == 0) {
            throw new Exception("Couldn't find organization: " + ORGANIZATION);
        }

        List<Integer> docIDs = db.getDocumentIdsWithAllOfTheseEntityIds(Arrays.asList(orgID));
        t.done("db.getDocumentIdsWithAllOfTheseEntityIds(Arrays.asList(orgID)");

        List<Integer> entityIDs = db.getEntityIdsInAnyofTheseDocumentIds(docIDs);
        t.done("db.getEntityIdsInAnyofTheseDocumentIds(docIDs)");

        Map<Integer, Integer> documentCounts = db.getDocumentCountsForEntitiesInDocuments(entityIDs, docIDs);
        t.done("db.getDocumentCountsForEntitiesInDocuments(entityIDs, docIDs)");
        
        Map<Integer, Integer> mentionCounts = db.getMentionCountsForEntitiesInDocuments(entityIDs, docIDs);
        t.done("db.getMentionCountsForEntitiesInDocuments(entityIDs, docIDs)");
    }
}
