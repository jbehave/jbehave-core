package org.jbehave.core.steps;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of Parameters that chains delegate Parameters in resolving
 * requests for values.
 */
public class ChainedParameters implements Parameters {
    private final Parameters[] delegates;

    public ChainedParameters(Parameters... delegates) {
        this.delegates = delegates;
    }

    /**
     * Determines if any delegate has the value.
     * 
     * @return A boolean, <code>true</code> if any delegate has the value
     */
    public boolean hasValue(String name) {
        for (Parameters each : delegates) {
            if (each.hasValue(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the value from the first delegate that has the value.
     * 
     * @return The value of type <T> or <code>null</code> if no delegate has the value
     */
    public <T> T valueAs(String name, Class<T> type) {
        for (Parameters each : delegates) {
            if (each.hasValue(name)) {
                return each.valueAs(name, type);
            }
        }
        return null;
    }

    /**
     * Returns the value from the first delegate that has the value.
     * 
     * @return The value of type <T> or the default value
     */
    public <T> T valueAs(String name, Class<T> type, T defaultValue) {
        for (Parameters each : delegates) {
            if (each.hasValue(name)) {
                return each.valueAs(name, type, defaultValue);
            }
        }       
        return defaultValue;
    }

    /**
     * Returns values aggregated from all the delegates, without overriding
     * values that already exist.
     * 
     * @return The Map of aggregated values
     */
    public Map<String, String> values() {
        Map<String, String> values = new HashMap<String, String>();
        for (Parameters each : delegates) {
            for (String name : each.values().keySet()) {
                if (!values.containsKey(name)){
                    values.put(name, each.values().get(name));                    
                }
            }
        }
        return values;
    }

}