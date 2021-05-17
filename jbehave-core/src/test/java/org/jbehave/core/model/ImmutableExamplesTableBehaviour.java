package org.jbehave.core.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ImmutableExamplesTableBehaviour {

    @Test
    void shouldThrowAnExceptionWhenAttempttingToModifyImmutableExamplesTable() {
        ExamplesTable immutableTable = new ImmutableExamplesTable("|key|\n|value|");
        Map<String, String> values = Collections.singletonMap("key", "value");

        assertAll(
            () -> assertThrows(UnsupportedOperationException.class, () -> immutableTable.getHeaders().add("")),
            () -> assertThrows(UnsupportedOperationException.class, () -> immutableTable.getRow(0).put("", "")),
            () -> assertThrows(UnsupportedOperationException.class, () -> immutableTable.withNamedParameters(values)),
            () -> assertThrows(UnsupportedOperationException.class, () -> immutableTable.withRowValues(0, values)),
            () -> assertThrows(UnsupportedOperationException.class, () -> immutableTable.withRows(Arrays.asList(values)))
        );
    }
}
