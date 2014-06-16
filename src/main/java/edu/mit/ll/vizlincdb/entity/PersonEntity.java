package edu.mit.ll.vizlincdb.entity;

import static edu.mit.ll.vizlincdb.util.VizLincProperties.*;
import java.util.ArrayList;
import java.util.List;

public class PersonEntity extends Entity {

    static final long serialVersionUID = 0x33F;

    private PersonEntity() { }
    
    public PersonEntity(String text, int num_documents, int num_mentions, String createdBy, int id) {
        super(text, num_documents, num_mentions, createdBy, id);
    }

    public static List<Integer> listOfPersonEntityIds(List<PersonEntity> entityList) {
        List<Integer> idList = new ArrayList<Integer>(entityList.size());
        for (PersonEntity e: entityList) {
            idList.add(e.getId());
        }
        return idList;
    }

    public String getType()
    {
        return getTypeForClass();
    }

    public static String getTypeForClass() {
        return E_PERSON;
    }

}