package org.jbehave.core.model;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ExamplesTableBehaviour {
    String tableAsString =
            "|one|two|\n" +
            "|11|12|\n" +
            "|21|22|\n";

    String wikiTableAsString =
            "||one||two||\n" +
            "|11|12|\n" +
            "|21|22|\n";

    @Test
    public void shouldParseTableWithDefaultSeparators() {
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureTableContentIsParsed(table);
        assertEquals(tableAsString, table.toString());
    }

    @Test
    public void shouldParseTableWithDifferentHeaderAndValueSeparator() {
        String headerSeparator = "||";
        String valueSeparator = "|";
        String tableWithCustomSeparator = wikiTableAsString;
        ExamplesTable table = new ExamplesTable(tableWithCustomSeparator, headerSeparator, valueSeparator);
        assertEquals(headerSeparator, table.getHeaderSeparator());
        ensureTableContentIsParsed(table);
        assertEquals(tableWithCustomSeparator, table.toString());
    }

    @Test
    public void shouldParseTableWithDifferentCustomHeaderAndValueSeparator() {
        String headerSeparator = "!!";
        String valueSeparator = "!";
        String tableWithCustomSeparator = wikiTableAsString.replace("|", "!");
        ExamplesTable table = new ExamplesTable(tableWithCustomSeparator, headerSeparator, valueSeparator);
        assertEquals(headerSeparator, table.getHeaderSeparator());
        ensureTableContentIsParsed(table);
        assertEquals(tableWithCustomSeparator, table.toString());
    }


    @Test
    public void shouldTrimTableBeforeParsing() {
        String untrimmedTableAsString = "\n    \n" + tableAsString + "\n    \n";
        ExamplesTable table = new ExamplesTable(untrimmedTableAsString);
        ensureTableContentIsParsed(table);
        assertEquals(untrimmedTableAsString, table.toString());
    }


    private void ensureTableContentIsParsed(ExamplesTable table) {
        assertEquals(asList("one", "two"), table.getHeaders());
        assertEquals(2, table.getRows().size());
        assertEquals("11", tableElement(table, 0, "one"));
        assertEquals("12", tableElement(table, 0, "two"));
        assertEquals("21", tableElement(table, 1, "one"));
        assertEquals("22", tableElement(table, 1, "two"));
    }

    private String tableElement(ExamplesTable table, int row, String header) {
        return table.getRow(row).get(header);
    }

}
