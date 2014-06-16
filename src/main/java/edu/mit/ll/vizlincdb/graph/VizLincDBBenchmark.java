package edu.mit.ll.vizlincdb.graph;

import com.tinkerpop.blueprints.Vertex;
import edu.mit.ll.vizlincdb.util.ElapsedTime;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * A simple benchmarks for measuring graphdb performance. Manually compared with VizLincRDB and VizLincRDBMem performance.
 */
public class VizLincDBBenchmark {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws SQLException, Exception {
        ElapsedTime t = new ElapsedTime();       
        VizLincDB db = new VizLincDB("J:/something.neo4j");
        
        t.reset();
        for (Vertex v : db.getDocuments()) {
            String name = (String) v.getProperty(P_DOCUMENT_NAME);
            String path = (String) v.getProperty(P_DOCUMENT_PATH);
        }
        t.done("get all documents + fetch properties");

        Vertex org = null;
        for (Vertex v : db.getEntitiesOfType(E_ORGANIZATION)) {
            // type, text, num_documents, num_mentions, created_by
            String type = (String) v.getProperty(P_ENTITY_TYPE);
            String text = (String) v.getProperty(P_ENTITY_TEXT);
            if (text.equals("SOME ORGANIZATION")) {
                org = v;
            }
            Integer num_documents = (Integer) v.getProperty(P_NUM_DOCUMENTS);
            Integer num_mentions = (Integer) v.getProperty(P_NUM_MENTIONS);
            String created_by = (String) v.getProperty(P_CREATED_BY);
        }
        t.done("get all ORGANIZATION entities + fetch properties");
        
        List<Vertex> docs = VizLincDB.makeList(db.getDocumentsWithAllOfTheseEntities(Arrays.asList(org)));
        t.done("VizLincDB.makeList(db.getDocumentsWithAllOfTheseEntities(Arrays.asList(org)))");
        
        List<Vertex> entities = VizLincDB.makeList(db.getEntitiesInAnyOfTheseDocuments(docs));
        t.done("VizLincDB.makeList(db.getEntitiesInAnyOfTheseDocuments(docs))");
    }
}
