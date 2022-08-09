package org.jbehave.core.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class ImmutableExamplesTable extends ExamplesTable {

    public ImmutableExamplesTable(String tableAsString) {
        super(tableAsString);
    }

    @Override
    public List<String> getHeaders() {
        return Collections.unmodifiableList(super.getHeaders());
    }

    @Override
    public Map<String, String> getRow(int rowIndex) {
        return Collections.unmodifiableMap(super.getRow(rowIndex));
    }

    @Override
    public ExamplesTable withNamedParameters(Map<String, String> namedParameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExamplesTable withRowValues(int rowIndex, Map<String, String> values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExamplesTable withRows(List<Map<String, String>> rows) {
        throw new UnsupportedOperationException();
    }

}
