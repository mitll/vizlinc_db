package edu.mit.ll.vizlincdb.document;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.ReaderUtil;

/**
 * 
 */
public class VizLincIndexSearcher extends IndexSearcher
{

    VizLincIndexSearcher(IndexReader reader)
    {
        super(reader);
    }

    public VizLincIndexSearcher(IndexReader r, ExecutorService executor)
    {
        super(r, executor);
    }

    public VizLincIndexSearcher(IndexReader reader, IndexReader[] subReaders, int[] docStarts)
    {
        super(reader, subReaders, docStarts);
    }

    public VizLincIndexSearcher(IndexReader reader, IndexReader[] subReaders, int[] docStarts, ExecutorService executor)
    {
        super(reader, subReaders, docStarts, executor);
    }

    /**
     *
     * @param requiredClauses
     * @param docs List of Document objects. The caller must make sure that the
     * luceneId field has been correctly set in each element of this list before
     * calling this method.
     * @return Map containing the document the tuple (documentPath,
     * totalHitCount) where totalHitCount is the total number of times instances
     * of the required clauses appear the corresponding document.
     * @throws IOException
     */
    public Map<String, Integer> getTotalHitCountInDocuments(List<Query> requiredClauses, List<Document> docs) throws IOException
    {
        //TODO: Make sure queries are either term queries or phrase queries
        Map<String, Integer> result = new HashMap<String, Integer>(docs.size());

        for (Document doc : docs)
        {
            if (doc.getLuceneId() == null)
            {
                System.err.println("LUCENE ID IS NULL");
            }
            int hitCount = (int) getTotalHitsForDoc(requiredClauses, doc.getLuceneId());
            System.err.println("Adding " + doc.getPath() + " to the map");
            result.put(doc.getPath(), hitCount);
        }

        return result;
    }

    private float getTotalHitsForDoc(List<Query> requiredClauses, int doc) throws IOException
    {
        if(requiredClauses == null && requiredClauses.isEmpty())
        {
            System.err.println("Required clauses empty or null");
            System.out.println("Required clauses empty or null");
        }
        float hitTotal = 0;
        for (Query q : requiredClauses)
        {
            float hitsOfThisClause = countInstancesInDoc(q, doc);
            hitTotal += hitsOfThisClause;
        }
        return hitTotal;
    }

    private float countInstancesInDoc(Query q, int doc) throws IOException
    {
        try
        {
            Weight weight = createNormalizedWeight(q);
            int n = ReaderUtil.subIndex(doc, docStarts);
            int deBasedDoc = doc - docStarts[n];
            IndexReader subReader = subReaders[n];

            Scorer scorer = weight.scorer(subReader, true, false);
            int d = scorer.advance(deBasedDoc);
            if (d == deBasedDoc)
            {
                return scorer.freq();
            } else
            {
                return 0.0f;
            }
        } catch (NullPointerException ex)
        {
            System.err.println("Something is NULL here");
            return 0;
        }
    }
}
