package edu.mit.ll.vizlincdb.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Partial info about a mention: enough to do queries about its location.
 * The information stored is the minimum necessary, to save space.
 */
public class MentionLocation 
{
    private int documentId;
    private int entityId;
    private int index;
    private int textStart;
    private int textStop;
    private short mentionTypeCode;
    
    private static Map<Short, String> shortToType = new HashMap<Short,String>();

    private MentionLocation() 
    {
        //System.err.println("MentionLocation constructor()");
    }

    public MentionLocation(Integer documentId, Integer entityId, Integer index, Integer textStart, Integer textStop, String type) {
        //System.err.println("MentionLocation constructor(args)");
        this.documentId = documentId;
        int entityVal;
        if(entityId == null)
        {
            entityVal = -1;
            System.err.println("Mention entity is null!!!!!");
        }
        else
        {
            entityVal = entityId;
        }
        this.entityId = entityVal;
        this.index = index;
        if(textStart < 0)
        {
            System.err.println("Mention with start < 0 docId = " + documentId );
        }
        
        this.textStart = textStart;
        this.textStop = textStop;
        short typeCode = getShortCodeForType(type);
        if(typeCode >= 0)
        {
            System.err.println("Typecode found: " + typeCode);
            mentionTypeCode = typeCode;
        }
        else
        {
            mentionTypeCode = findNextShort();
            System.err.println("Inserting type code: " + mentionTypeCode + " For type: " + type);
            shortToType.put(mentionTypeCode, type);
        }
    }

    @Override
    public String toString() {
        return "MentionLocation{" + "documentId=" + documentId + ", entityId=" + entityId + ", index=" + index + ", textStart=" + textStart + ", textStop=" + textStop + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.documentId;
        hash = 41 * hash + this.entityId;
        hash = 41 * hash + this.index;
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
        final MentionLocation other = (MentionLocation) obj;
        if (this.documentId != other.documentId) {
            return false;
        }
        if (this.entityId != other.entityId) {
            return false;
        }
        if (this.index != other.index) {
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


    public int getDocumentId() {
        return documentId;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getIndex() {
        return index;
    }

    public int getTextStart() {
        return textStart;
    }

    public int getTextStop() {
        return textStop;
    }
    
    public String getMentionType()
    {
        String type =  shortToType.get(mentionTypeCode);
        if(type == null)
        {
            System.err.println("Type is null!!");
            System.err.println("Here is the map: ");
            for(short code: this.shortToType.keySet())
            {
                System.err.println("code: " + code + " type: " + shortToType.get(code));
            }
        }
        return type;
    }

    private short getShortCodeForType(String type)
    {
        for(short code: shortToType.keySet())
        {
            if(shortToType.get(code).equals(type))
            {
                return code;
            }
        }
        
        return -1;
    }
    
    public static Map<Short,String> getTypeCodeTable()
    {
        return shortToType;
    }
    
    public static void setTypeCodeTable(Map<Short,String> table)
    {
        shortToType = table;
    }

    /**
     * 
     * @return One more than the max value used as key
     */
    private short findNextShort()
    {
        short max = -1;
        for(short v: shortToType.keySet())
        {
            if(v > max)
            {
                max = v;
            }
        }
        return (short)(max + 1);
    }
}
