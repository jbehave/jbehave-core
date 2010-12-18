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

    public boolean hasValue(String name) {
        for (Parameters each : delegates) {
            if (each.hasValue(name)) {
                return true;
            }
        }
        return false;
    }

    public <T> T valueAs(String name, Class<T> type) {
        for (Parameters each : delegates) {
            if (each.hasValue(name)) {
                return each.valueAs(name, type);
            }
        }
        return null;
    }

    public <T> T valueAs(String name, Class<T> type, T defaultValue) {
        for (Parameters each : delegates) {
            if (each.hasValue(name)) {
                return each.valueAs(name, type, defaultValue);
            }
        }
        return null;
    }

    public Map<String, String> values() {
        Map<String, String> values = new HashMap<String, String>();
        for (Parameters each : delegates) {
            values.putAll(each.values());
        }
        return values;
    }

}