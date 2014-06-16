package edu.mit.ll.vizlincdb.io;

import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.batch.Neo4jBatchGraph;
import edu.mit.ll.vizlincdb.util.VizLincProperties;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;

/**
 * Efficiently ingest the text source documents to graph database, creating document nodes and named-entity mention
 * nodes.
 */
public class VizLincGraphPopulator {

    Neo4jBatchGraph batchGraph;

    public VizLincGraphPopulator(String databasePath) {
        batchGraph = new Neo4jBatchGraph(databasePath);
        initDB(batchGraph);
        // Make sure the database gets written out when the JVM quits.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (batchGraph != null) {
                    batchGraph.shutdown();
                }
            }
        });
    }

    /**
     * Do an explicit shutdown in case it needs to be done before the JVM shuts down.
     */
    public void shutdown() {
        // Neo4jBatchGraph does not do transactions.
        if (batchGraph != null) {
            batchGraph.shutdown();
            batchGraph = null;
        }
    }

    protected final void initDB(KeyIndexableGraph graph) {
        // Easy lookup of Document vertices by document name.
        graph.createKeyIndex(P_DOCUMENT_NAME, Vertex.class);
        // Easy lookup of vertices by type.
        graph.createKeyIndex(P_NODE_TYPE, Vertex.class);
        // Easy lookup of mentions and entities by type.
        graph.createKeyIndex(P_ENTITY_TYPE, Vertex.class);
        graph.createKeyIndex(P_MENTION_TYPE, Vertex.class);
        // Easy lookup of vertices by the createdBy tag.
        graph.createKeyIndex(P_CREATED_BY, Vertex.class);
        // Easy lookup of mentions by global id.
        graph.createKeyIndex(P_MENTION_GLOBAL_ID, Vertex.class);
    }

    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        Option database_option = new Option("d", "database", true, "database directory");
        database_option.setRequired(true);
        options.addOption(database_option);

        VizLincGraphPopulator populator = null;
        int documentCount = 0;
        int mentionCount = 0;
        try {
            CommandLine line = parser.parse(options, args);
            String[] filenames = line.getArgs();
            if (filenames.length % 2 != 0) {
                System.err.println("ERROR: Odd number of files. Arguments must of be pairs of text and ner-csv files.");
                System.exit(1);
            }
            populator = new VizLincGraphPopulator(line.getOptionValue("database"));

            for (int i = 0; i < filenames.length; i += 2) {
                String txtFilename = filenames[i];
                String mentionsFilename = filenames[i + 1];
                try {
                    mentionCount += populator.ingestDocumentAndMentions(new File(txtFilename), txtFilename, new File(mentionsFilename));
                    documentCount++;
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        } catch (org.apache.commons.cli.ParseException ex) {
            System.err.println(ex.getMessage());
        } finally {
            System.out.println(String.format("Ingested %d documents", documentCount));
            System.out.println(String.format("Ingested %d mentions", mentionCount));

            if (populator != null) {
                populator.shutdown();

            }
        }
    }

    public int ingestDocumentAndMentions(File txtFile, String txtFileName, File mentionsFile) throws FileNotFoundException, IOException {
        Vertex docNode = addTextDocument(txtFileName, txtFile.getCanonicalPath(), FileUtils.readFileToString(txtFile, "UTF-8"));
        CSVMentionFileReader csvMentionFileReader = new CSVMentionFileReader(mentionsFile);

        int mentionCount = 0;
        CSVMention csvMention;
        while ((csvMention = csvMentionFileReader.next()) != null) {
            addMention(docNode, csvMention);
            mentionCount++;
        }
        return mentionCount;
    }

    public Vertex addTextDocument(String documentName, String documentPath, String text) {
        Vertex documentNode = batchGraph.addVertex(null);
        documentNode.setProperty(P_NODE_TYPE, NODE_TYPE_DOCUMENT);
        documentNode.setProperty(P_DOCUMENT_NAME, documentName);
        documentNode.setProperty(P_DOCUMENT_PATH, documentPath);
        documentNode.setProperty(P_DOCUMENT_TEXT, text);
        return documentNode;
    }

    public Vertex addMention(Vertex documentNode, CSVMention mention) {
        return addMention(
                documentNode,
                mention.getEntityType(),
                mention.getMentionText(),
                mention.getIndexInDocument(),
                mention.getGlobalId(),
                mention.getStartPosition(),
                mention.getStopPosition());
    }

    public Vertex addMention(Vertex documentNode, String type, String text, Integer index, Integer globalId, int begin, Integer end) {
        Vertex mentionNode = batchGraph.addVertex(null);
        mentionNode.setProperty(P_NODE_TYPE, NODE_TYPE_MENTION);
        mentionNode.setProperty(P_MENTION_TYPE, type);
        if (text != null) {
            mentionNode.setProperty(P_MENTION_TEXT, text);
        }
        mentionNode.setProperty(P_MENTION_INDEX, index);
        if (globalId != null) {
            mentionNode.setProperty(P_MENTION_GLOBAL_ID, globalId);
        }
        mentionNode.setProperty(P_MENTION_TEXT_START, begin);
        mentionNode.setProperty(P_MENTION_TEXT_STOP, end);

        batchGraph.addEdge(null, documentNode, mentionNode, VizLincProperties.L_DOCUMENT_TO_MENTION);

        return mentionNode;
    }
}
