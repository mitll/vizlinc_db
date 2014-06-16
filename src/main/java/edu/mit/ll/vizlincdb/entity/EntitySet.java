package edu.mit.ll.vizlincdb.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class EntitySet implements Serializable {

    static final long serialVersionUID = 0x1E;

    private List<DateEntity> dateEntities;
    private List<LocationEntity> locationEntities;
    private List<OrganizationEntity> organizationEntities;
    private List<PersonEntity> personEntities;

    /**
     * Create an EntitySet from an existing set of entity lists.
     * @param dateEntities
     * @param locationEntities
     * @param organizationEntities
     * @param personEntities
     */
    public EntitySet(List<DateEntity> dateEntities, List<LocationEntity> locationEntities, List<OrganizationEntity> organizationEntities, List<PersonEntity> personEntities) {
        this.dateEntities = dateEntities;
        this.locationEntities = locationEntities;
        this.organizationEntities = organizationEntities;
        this.personEntities = personEntities;
    }

   /**
     * Create an empty entity set.
     */
    public EntitySet() {
        this.dateEntities = new ArrayList<DateEntity>();
        this.locationEntities = new ArrayList<LocationEntity>();
        this.organizationEntities = new ArrayList<OrganizationEntity>();
        this.personEntities = new ArrayList<PersonEntity>();

    }

    /**
     * Create an EntitySet with all the given entities put into the proper lists.
     * @param entities
     */
    public EntitySet(List<Entity> entities) {
        super();
        for (Entity e : entities) {
            add(e);
        }
    }

    /**
     * Add the given entity to the entity set, putting in the proper list.
     * @param e
     */
    public void add(Entity e) {
        if (e instanceof DateEntity) {
            this.dateEntities.add((DateEntity) e);
        } else if (e instanceof LocationEntity) {
            this.locationEntities.add((LocationEntity) e);
        } else if (e instanceof OrganizationEntity) {
            this.organizationEntities.add((OrganizationEntity) e);
        } else if (e instanceof PersonEntity) {
            this.personEntities.add((PersonEntity) e);
        } else {
            throw new IllegalArgumentException("unexpected type of entity");
        }
    }

    public int size() {
        return dateEntities.size() + locationEntities.size() + organizationEntities.size() + personEntities.size();
    }
    
    public List<DateEntity> getDateEntities() {
        return dateEntities;
    }

    public List<LocationEntity> getLocationEntities() {
        return locationEntities;
    }

    public List<OrganizationEntity> getOrganizationEntities() {
        return organizationEntities;
    }

    public List<PersonEntity> getPersonEntities() {
        return personEntities;
    }
    
    /**
     * Return a List of all the entities.
     * @return a new list built out of the category lists.
     */
    public List<Entity> getEntities() {
        List<Entity> entities = new ArrayList<Entity>(size());
        entities.addAll(dateEntities);
        entities.addAll(locationEntities);
        entities.addAll(organizationEntities);
        entities.addAll(personEntities);
        return entities;
    }
    
    /**
     * Return a list of all the entity ids.
     * @return 
     */
    public List<Integer> getEntityIds() {
        return Entity.listOfEntityIds(getEntities());
    }

    public void updateMentionCounts(Map<Integer, Integer> mentionMap)
    {
        updateMentionCountsAux(this.dateEntities, mentionMap);
        updateMentionCountsAux(this.locationEntities, mentionMap);
        updateMentionCountsAux(this.organizationEntities, mentionMap);
        updateMentionCountsAux(this.personEntities, mentionMap);
    }

    private void updateMentionCountsAux(List<? extends Entity> entityList, Map<Integer, Integer> mentionMap)
    {
        for(Entity e: entityList)
        {
          int count = mentionMap.get(e.getId());
          e.setNumMentions(count);
        }
    }

    public void updateDocumentCounts(Map<Integer, Integer> docCountMap)
    {
        updateDocumentCountsAux(this.dateEntities,docCountMap);
        updateDocumentCountsAux(this.locationEntities,docCountMap);
        updateDocumentCountsAux(this.organizationEntities,docCountMap);
        updateDocumentCountsAux(this.personEntities,docCountMap);
        
    }
    
    private void updateDocumentCountsAux(List<? extends Entity> entityList, Map<Integer, Integer> docCountMap)
    {
        for(Entity e: entityList)
        {
            int docCount = docCountMap.get(e.getId());
            e.setNumDocuments(docCount);
        }
    }
}