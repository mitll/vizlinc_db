package edu.mit.ll.vizlincdb.document;

import edu.mit.ll.vizlincdb.graph.VizLincDB;
import com.tinkerpop.blueprints.Vertex;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.io.File;
import java.io.IOException;
import org.apache.commons.cli.ParseException;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

/**
 * Index documents into a Lucene database.
 */
public class VizLincDocumentIndexer implements AutoCloseable {

    Analyzer analyzer;
    IndexWriter indexWriter;

    public VizLincDocumentIndexer(String indexPath) throws IOException {
        analyzer = new FoldingSpanishAnalyzer(VIZLINCDB_LUCENE_VERSION);
        Directory directory = new SimpleFSDirectory(new File(indexPath));
        IndexWriterConfig config = new IndexWriterConfig(VIZLINCDB_LUCENE_VERSION, analyzer);
        indexWriter = new IndexWriter(directory, config);
    }

    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        Options options = new Options();

        Option database_option = new Option("d", "database", true, "database directory");
        database_option.setRequired(true);
        options.addOption(database_option);

        Option index_option = new Option("i", "index", true, "Lucene index directory");
        index_option.setRequired(true);
        options.addOption(index_option);

        try {
            CommandLine line = parser.parse(options, args);
            VizLincDocumentIndexer indexer = new VizLincDocumentIndexer(line.getOptionValue("index"));
            VizLincDB db = new VizLincDB(line.getOptionValue("database"));
            int documentCount = indexer.indexDocuments(db);
            indexer.close();
            db.shutdown();
            System.out.println(String.format("Indexed %d documents", documentCount));
        } catch (IOException | ParseException ex) {
            System.err.println(ex.getMessage());
        }
    }

    public int indexDocuments(VizLincDB db) throws IOException {
        int documentCount = 0;
        for (Vertex doc : db.getDocuments()) {
            indexDocumentText((String) doc.getProperty(P_DOCUMENT_NAME), (String) doc.getProperty(P_DOCUMENT_TEXT));
            documentCount++;
        }
        return documentCount;
    }

    public void indexDocumentText(String documentName, String text) throws CorruptIndexException, IOException {
        System.out.println("Indexing " + documentName);
        Document doc = new Document();
        doc.add(new Field(P_DOCUMENT_NAME, documentName, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field(P_DOCUMENT_TEXT, text, Field.Store.YES, Field.Index.ANALYZED));
        indexWriter.addDocument(doc);
    }

    @Override
    public void close() throws CorruptIndexException, IOException {
        indexWriter.close();
    }
}
