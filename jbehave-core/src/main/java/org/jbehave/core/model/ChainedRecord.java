package org.jbehave.core.model;

/**
 * Record that contains a list of other {@link Record}s that it uses to try and resolve requests for
 * values.
 */
public class ChainedRecord implements Record {
    private final Record[] records;

    public ChainedRecord(Record... records) {
        this.records = records;
    }

    public String value(String name) {
        for (Record each : records)
            if (each.contains(name))
                return each.value(name);

        return null;
    }

    public boolean contains(String name) {
        for (Record each : records)
            if (each.contains(name))
                return true;

        return false;
    }
}