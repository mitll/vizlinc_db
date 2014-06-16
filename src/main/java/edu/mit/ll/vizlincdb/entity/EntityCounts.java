package edu.mit.ll.vizlincdb.entity;

/**
 * Counts for a particular entity, as returned by
 * getEntitiesMentionedNearEntities*.
 */
public class EntityCounts {
    /**
     * The number of mentions of this entity in the specified neighborhood.
     */
    public int mentionCount;
    /**
     * The number of documents that this entity appears in, in the specified
     * neighborhood.
     */
    public int documentCount;

    public EntityCounts(int mentionCount, int documentCount) {
        this.mentionCount = mentionCount;
        this.documentCount = documentCount;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.mentionCount;
        hash = 37 * hash + this.documentCount;
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
        final EntityCounts other = (EntityCounts) obj;
        if (this.mentionCount != other.mentionCount) {
            return false;
        }
        if (this.documentCount != other.documentCount) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "EntityCounts{" + "mentionCount=" + mentionCount + ", documentCount=" + documentCount + '}';
    }

}
