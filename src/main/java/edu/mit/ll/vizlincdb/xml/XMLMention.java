package edu.mit.ll.vizlincdb.xml;

/**
 * A mention of a single named entity, holding the entity string
 * and its character offset in the XML document.
 */
public class XMLMention {
    private String type = "";
    private String text = "";
    private int index = -1;
    private long startingCharOffset = -1;
    private long endingCharOffset = -1;
    private int tokenOffset = -1;
    private int depth = 0;

    @Override
    public String toString() {
        return "XMLMention{" + "type=" + type +  ", text=" + text + ", index=" + index + ", startingCharOffset=" + startingCharOffset + ", endingCharOffset=" + endingCharOffset + ", tokenOffset=" + tokenOffset + ", depth=" + depth + '}';
    }

    public XMLMention(String type, String text, int index, long startingCharOffset, long endingCharOffset, int tokenOffset, int depth) {
        this.type = type;
        this.text = text;
        this.index = index;
        this.startingCharOffset = startingCharOffset;
        this.endingCharOffset = endingCharOffset;
        this.tokenOffset = tokenOffset;
        this.depth = depth;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 43 * hash + (this.text != null ? this.text.hashCode() : 0);
        hash = 43 * hash + this.index;
        hash = 43 * hash + (int) (this.startingCharOffset ^ (this.startingCharOffset >>> 32));
        hash = 43 * hash + (int) (this.endingCharOffset ^ (this.endingCharOffset >>> 32));
        hash = 43 * hash + this.tokenOffset;
        hash = 43 * hash + this.depth;
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
        final XMLMention other = (XMLMention) obj;
        if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
            return false;
        }
        if ((this.text == null) ? (other.text != null) : !this.text.equals(other.text)) {
            return false;
        }
        if (this.index != other.index) {
            return false;
        }
        if (this.startingCharOffset != other.startingCharOffset) {
            return false;
        }
        if (this.endingCharOffset != other.endingCharOffset) {
            return false;
        }
        if (this.tokenOffset != other.tokenOffset) {
            return false;
        }
        if (this.depth != other.depth) {
            return false;
        }
        return true;
    }

    public XMLMention(String type) {
        this.type = type;
    }

    public XMLMention() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getStartingCharOffset() {
        return startingCharOffset;
    }

    public void setStartingCharOffset(long startingCharOffset) {
        this.startingCharOffset = startingCharOffset;
    }

    public long getEndingCharOffset() {
        return endingCharOffset;
    }

    public void setEndingCharOffset(long endingCharOffset) {
        this.endingCharOffset = endingCharOffset;
    }

    public int getTokenOffset() {
        return tokenOffset;
    }

    public void setTokenOffset(int tokenOffset) {
        this.tokenOffset = tokenOffset;
    }

    /**
     * If entities are nested, the depth of the nesting. Level 0 is a top-level entity.
     * @return 
     */
    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

}
