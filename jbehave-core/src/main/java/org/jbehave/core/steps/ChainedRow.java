package org.jbehave.core.steps;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of Row that chains delegate Rows in resolving requests for
 * values.
 */
public class ChainedRow implements Row {
    private final Row[] delegates;

    public ChainedRow(Row... delegates) {
        this.delegates = delegates;
    }

    /**
     * Returns values aggregated from all the delegates, without overriding
     * values that already exist.
     * 
     * @return The Map of aggregated values
     */
    public Map<String, String> values() {
        Map<String, String> values = new HashMap<String, String>();
        for (Row each : delegates) {
            for (String name : each.values().keySet()) {
                if (!values.containsKey(name)) {
                    values.put(name, each.values().get(name));
                }
            }
        }
        return values;
    }

}