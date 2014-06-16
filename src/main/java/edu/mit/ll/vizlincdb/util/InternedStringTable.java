package edu.mit.ll.vizlincdb.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Look up a given string in this table and return an existing unique string that is equal, to save string space.
 */
public class InternedStringTable {

    private Map<String, String> stringMap = new ConcurrentHashMap<String, String>();

    public String get(String s) {
        if (s == null) {
            return null;
        }
        if (!stringMap.containsKey(s)) {
            stringMap.put(s, s);
            return s;
        }
        return stringMap.get(s);
    }
}
