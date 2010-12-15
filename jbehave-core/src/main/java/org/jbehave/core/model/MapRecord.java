package org.jbehave.core.model;

import java.util.Map;

/**
 * {@link Record} that pulls the values from a {@link Map}.
 */
public class MapRecord implements Record {
    private final Map<String, String> map;

    /**
     * Class constructor.
     * 
     * @param map the map to pull the defaults from.
     */
    public MapRecord(Map<String, String> map) {
        this.map = map;
    }

    public String value(String name) {
        return map.get(name);
    }

    public boolean contains(String name) {
        return map.containsKey(name);
    }
}