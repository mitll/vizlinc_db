package edu.mit.ll.vizlincdb.document;

import edu.mit.ll.vizlincdb.relational.VizLincRDB;
import edu.mit.ll.vizlincdb.relational.VizLincRDBMem;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



public class Document implements Serializable {

    static final long serialVersionUID = 0x1D;

    private String name;
    private String path;
    private String text;
    private int id;
    private Integer luceneID;

    public static List<Integer> listOfDocumentIds(List<Document> docList) {
        List<Integer> idList = new ArrayList<Integer>(docList.size());
        for (Document doc: docList) {
            idList.add(doc.getId());
        }
        return idList;
    }

    public Document() { }


    public Document(String name, String path, int id) {
        this.name = name;
        this.path = path;
        this.id = id;
        this.text = null;     // Fetched on demand.
        this.luceneID = null; //Lazy assignment; set when an operation is performed on the lucene index.
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public int getId() {
        return id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText(VizLincRDBMem db) throws SQLException {
        if (text == null) {
            text = db.getDocumentText(id);
        }
        return text;
    }

    public String getText(VizLincRDB db) throws SQLException {
        if (text == null) {
            text = db.getDocumentText(id);
        }
        return text;
    }

    public Integer getLuceneId()
    {
        return this.luceneID;
    }

    public void setLuceneId(Integer id)
    {
        this.luceneID = id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 89 * hash + (this.path != null ? this.path.hashCode() : 0);
        hash = 89 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Document other = (Document) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.path == null) ? (other.path != null) : !this.path.equals(other.path)) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}