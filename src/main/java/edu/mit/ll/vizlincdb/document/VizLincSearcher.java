package edu.mit.ll.vizlincdb.document;

import edu.mit.ll.vizlincdb.document.VizLincIndexSearcher;
import edu.mit.ll.vizlincdb.document.FoldingSpanishAnalyzer;
import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;



/**
 * An API to hide the details of Lucene searching for VizLinc.
 */
public class VizLincSearcher {

    IndexReader indexReader;
    VizLincIndexSearcher indexSearcher;
    QueryParser queryParser;


    /**
     * Open an existing Lucene index or create a new one using the path specified by the File (which will be a directory).
     * @param indexPath  specifies a directory which does not need to exist already (though the containing directory should exist).
     */
    public VizLincSearcher(File indexPath) throws IOException {
        this(indexPath.getPath());
    }

   /**
     * Open an existing Lucene index or create a new one using the path specified by the String (which will be a directory).
     * @param indexPath  specifies a directory which does not need to exist already (though the containing directory should exist).
     */
    public VizLincSearcher(String indexPath) throws IOException {
        Directory directory = new SimpleFSDirectory(new File(indexPath));
        indexReader = IndexReader.open(directory);
        indexSearcher = new VizLincIndexSearcher(indexReader);
        Analyzer queryAnalyzer = new FoldingSpanishAnalyzer(VIZLINCDB_LUCENE_VERSION);
        queryParser = new QueryParser(VIZLINCDB_LUCENE_VERSION, P_DOCUMENT_TEXT, queryAnalyzer);
    }

    public List<Integer> searchWithQuery(String queryString, int n) throws IOException, ParseException {
        Query query = queryParser.parse(queryString);
        TopDocs docs = indexSearcher.search(query, n);
        List<Integer> results = new ArrayList<Integer>();
        for (ScoreDoc scoreDoc: docs.scoreDocs) {
            results.add(scoreDoc.doc);
        }
        return results;
    }
    
    public List<Integer> searchWithQuery(Query query, int n) throws IOException, ParseException 
    {
        long start = System.currentTimeMillis();
        TopDocs docs = indexSearcher.search(query, n);
        long end = System.currentTimeMillis();
        System.out.println("Query " + query.toString()+ "\n" + "Search took" + (end - start));
        List<Integer> results = new ArrayList<Integer>();
        for (ScoreDoc scoreDoc: docs.scoreDocs) {
            results.add(scoreDoc.doc);
            //System.out.println(getDocumentName(scoreDoc.doc) + " = " + indexSearcher.explain(query, scoreDoc.doc).toString());
        }
        
        return results;
    }
    
    
    public Map<String, Integer> getTermPhraseFreqs(Query query, List<Document> docs) throws IOException
    {
        long start = System.currentTimeMillis();
        List<Query> requiredClauses = getRequiredClauses(query);
        System.out.println("Required Clauses:");
        for(Query q: requiredClauses)
        {
            System.out.println(q.getClass() + " => " + q.toString());
        }
        
        Map<String, Integer> term_PhraseFreqs = indexSearcher.getTotalHitCountInDocuments(requiredClauses, docs);
        if(term_PhraseFreqs == null)
        {
            System.err.println("termPhraseFreqs is null!!!");
        }
        long end = System.currentTimeMillis();
        System.out.println("Computing freqs took: " + (end - start));
        
        System.out.println(query.getClass() + " => " + query.toString());
        
        //***** DEBUGGING ****//
        for(Document doc: docs)
        {
            System.out.println("Testing doc: " + doc.getPath());
            if(term_PhraseFreqs.get(doc.getPath()) == null)
            {
                System.err.println("frequency is NULL");
                System.out.println("frequency is NULL");
            }
            int freq = term_PhraseFreqs.get(doc.getPath());
            System.out.println("**Document: " + doc.getName() + " confirmation from index: " + getDocumentName(doc.getLuceneId()) + "\nfreq: " + freq);
        }
        
        return term_PhraseFreqs;
    }

    public String getDocumentName(int docNumber) throws CorruptIndexException, IOException {
        return indexReader.document(docNumber).get(P_DOCUMENT_NAME);
    }

    public String getDocumentText(int docNumber) throws CorruptIndexException, IOException {
        return indexReader.document(docNumber).get(P_DOCUMENT_TEXT);
    }


    /**
     * Shutdown this searcher; it can't be used after calling close().
     * @throws IOException
     */
    public void close() throws IOException {
        indexSearcher.getIndexReader().close();
        indexSearcher.close();
    }

    private List<Query> getRequiredClauses(Query query)
    {
        List<Query> result = new LinkedList<Query>();
        if(query instanceof TermQuery || query instanceof PhraseQuery)
        {
            result.add(query);
            return result;
        }
        
        if(query instanceof BooleanQuery)
        {
            BooleanQuery bQuery = (BooleanQuery)query;
            List<BooleanClause> clauses = bQuery.clauses();
            for(BooleanClause clause: clauses)
            {
                result.add(clause.getQuery());
            }
            return result;
        }
        
        throw new RuntimeException("Can't compute total number of keyword instances in document."
                + " unexpected query type: " + query.getClass());
            
    }



}
