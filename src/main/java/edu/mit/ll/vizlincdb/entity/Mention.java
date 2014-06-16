package edu.mit.ll.vizlincdb.entity;

import edu.mit.ll.vizlincdb.util.InternedStringTable;
import java.util.Objects;

/**
 * A mention of a single named entity, as stored in the database.
 */
public class Mention {
    private int id;
    private int documentId;
    private int entityId;
    private String type;
    private String text;
    private int index;
    private Integer globalId;    // may be null
    private int textStart;
    private int textStop;

    private Mention() {
    }

    protected static InternedStringTable typeStrings = new InternedStringTable();

    public Mention(int id, int documentId, int entityId, String type, String text, int index, Integer globalId, int textStart, int textStop) {
        this.id = id;
        this.documentId = documentId;
        this.entityId = entityId;
        this.type = typeStrings.get(type);
        this.text = text;
        this.index = index;
        this.textStart = textStart;
        this.textStop = textStop;
    }

    @Override
    public String toString() {
        return "Mention{" + "id=" + id + ", documentId=" + documentId + ", entityId=" + entityId + ", type=" + type + ", text=" + text + ", index=" + index + ", globalId=" + globalId + ", textStart=" + textStart + ", textStop=" + textStop + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.id;
        hash = 41 * hash + this.documentId;
        hash = 41 * hash + this.entityId;
        hash = 41 * hash + Objects.hashCode(this.type);
        hash = 41 * hash + Objects.hashCode(this.text);
        hash = 41 * hash + this.index;
        hash = 41 * hash + Objects.hashCode(this.globalId);
        hash = 41 * hash + this.textStart;
        hash = 41 * hash + this.textStop;
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
        final Mention other = (Mention) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.documentId != other.documentId) {
            return false;
        }
        if (this.entityId != other.entityId) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        if (!Objects.equals(this.globalId, other.globalId)) {
            return false;
        }
        if (this.textStart != other.textStart) {
            return false;
        }
        if (this.textStop != other.textStop) {
            return false;
        }
        return true;
    }


    public int getId() {
        return id;
    }

    public int getDocumentId() {
        return documentId;
    }

    public int getEntityId() {
        return entityId;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getIndex() {
        return index;
    }

    public Integer getGlobalId() {
        return globalId;
    }

    public int getTextStart() {
        return textStart;
    }

    public int getTextStop() {
        return textStop;
    }

}
