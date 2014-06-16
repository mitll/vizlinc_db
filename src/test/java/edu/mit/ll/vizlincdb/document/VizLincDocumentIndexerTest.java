package edu.mit.ll.vizlincdb.document;

import edu.mit.ll.vizlincdb.io.VizLincGraphPopulator;
import edu.mit.ll.vizlincdb.graph.VizLincDB;
import com.tinkerpop.blueprints.Vertex;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;


/**
 *
 */
public class VizLincDocumentIndexerTest {

    public VizLincDocumentIndexerTest() {
    }

    @ClassRule
    public static TemporaryFolder temp_dir = new TemporaryFolder();

    static File sample_txt_file;
    static File sample_mentions_file;
    static File db_dir;

    @BeforeClass
    public static void setUpClass() throws IOException, ParseException {
        sample_txt_file = temp_dir.newFile();
        FileUtils.writeStringToFile(sample_txt_file,
                "abc Pedro Antonio de Alarcón def Mexico ghi Nezahualcóyotl jkl",
                "UTF-8");
        sample_mentions_file = temp_dir.newFile();
        FileUtils.writeStringToFile(sample_mentions_file, "");

        db_dir = temp_dir.newFolder();
        VizLincGraphPopulator populator = new VizLincGraphPopulator(db_dir.getAbsolutePath());
        populator.ingestDocumentAndMentions(sample_txt_file, sample_txt_file.getName(), sample_mentions_file);
        populator.shutdown();
    }


    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of main method, of class VizLincDocumentIndexer.
     * @throws java.io.IOException
     */
    @Test
    public void testMain() throws IOException {
        String[] args = new String[] {
            "-d", db_dir.getAbsolutePath(),
            "-i", temp_dir.newFolder().getAbsolutePath()
        };

        VizLincDocumentIndexer.main(args);
    }

    /**
     * Test of indexDocumentText method, of class VizLincDocumentIndexer.
     * @throws java.io.IOException
    */
    @Test
    public void testIndexDocumentText() throws IOException {
        VizLincDocumentIndexer indexer = new VizLincDocumentIndexer(temp_dir.newFolder().getAbsolutePath());
        VizLincDB db = new VizLincDB(db_dir);
        for (Vertex doc: db.getDocuments()) {
            indexer.indexDocumentText((String) doc.getProperty(P_DOCUMENT_NAME), (String) doc.getProperty(P_DOCUMENT_TEXT));
        }
        db.shutdown();
    }

    /**
     * Test of indexDocuments method, of class VizLincDocumentIndexer.
     * @throws java.io.IOException
     */
    @Test
    public void testIndexDocuments() throws IOException {
        VizLincDB db = new VizLincDB(db_dir);
        try (VizLincDocumentIndexer indexer = new VizLincDocumentIndexer(temp_dir.newFolder().getAbsolutePath())) {
            indexer.indexDocuments(db);
        }
        db.shutdown();
    }

}
