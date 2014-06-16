package edu.mit.ll.vizlincdb.entity;

import edu.mit.ll.vizlincdb.entity.DateEntity;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Entity implements Serializable {
    private String type;
    private String text;
    private int numDocuments;
    private int numMentions;
    private String createdBy;
    private int id;
    
    protected Entity() { }
    
    
    protected Entity(String text, int numDocuments, int NumMentions, String createdBy, int id)     {
        this.text = text;
        this.numDocuments = numDocuments;
        this.numMentions = NumMentions;
        this.createdBy = createdBy;
        this.id = id;
    }

    public static Entity create(String type, String text, int numDocuments, int numMentions, String createdBy, int id) {
        if (type.equals(DateEntity.getTypeForClass())) {
            return new DateEntity(text, numDocuments, numMentions, createdBy, id);
        } else if (type.equals(LocationEntity.getTypeForClass())) {
            return new LocationEntity(text, numDocuments, numMentions, createdBy, id);
        } else if (type.equals(OrganizationEntity.getTypeForClass())) {
            return new OrganizationEntity(text, numDocuments, numMentions, createdBy, id);
        } else if (type.equals(PersonEntity.getTypeForClass())) {
            return new PersonEntity(text, numDocuments, numMentions, createdBy, id);
        } else {
            throw new IllegalArgumentException("bad entity type: " + type);
        }
    }

    public static List<Integer> listOfEntityIds(List<Entity> entityList) {
        List<Integer> idList = new ArrayList<Integer>(entityList.size());
        for (Entity doc: entityList) {
            idList.add(doc.getId());
        }
        return idList;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode(this.text);
        hash = 23 * hash + this.numDocuments;
        hash = 23 * hash + this.numMentions;
        hash = 23 * hash + Objects.hashCode(this.createdBy);
        hash = 23 * hash + this.id;
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
        final Entity other = (Entity) obj;
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        if (this.numDocuments != other.numDocuments) {
            return false;
        }
        if (this.numMentions != other.numMentions) {
            return false;
        }
        if (!Objects.equals(this.createdBy, other.createdBy)) {
            return false;
        }
        if (this.id != other.id) {
            return false;
        }
        return true;
    }


    /**
     * Return E_PERSON, E_LOCATION, E_ORGANIZATION, or E_DATE.
     * @return the string representing the type of this entity.
     */
    public abstract String getType();

    public static String getTypeForClass() {
        throw new UnsupportedOperationException("Must be called on subtypes");
    }


    public String getText() {
        return text;
    }

    public int getNumDocuments() {
        return numDocuments;
    }

    public int getNumMentions() {
        return numMentions;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString()
    {
        DecimalFormat format = new DecimalFormat("###,###");
        String fCount = format.format((long) this.numMentions);
        String dCount = format.format((long) this.numDocuments);
        return this.text + "(M:" + fCount + ", D:" + dCount + ")";
    }
    
    public void setNumMentions(int mentionCount)
    {
        this.numMentions = mentionCount;
    }

    void setNumDocuments(int docCount)
    {
        this.numDocuments = docCount;
    }

}