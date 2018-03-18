package org.jbehave.core.steps;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

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
    @Override
    public Map<String, String> values() {
        Map<String, String> values = new LinkedHashMap<>();
        for (Row each : delegates) {
            for (Entry<String, String> entry : each.values().entrySet()) {
                String name = entry.getKey();
                if (!values.containsKey(name)) {
                    values.put(name, entry.getValue());
                }
            }
        }
        return values;
    }

}
