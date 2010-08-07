package org.jbehave.core.model;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class ExamplesTableBehaviour {
    
    String tableAsString = "|one|two|\n" + "|11|12|\n" + "|21|22|\n";

    String wikiTableAsString = "||one||two||\n" + "|11|12|\n" + "|21|22|\n";

    @Test
    public void shouldParseTableWithDefaultSeparators() {
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureTableContentIsParsed(table);
        assertThat(table.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldParseTableWithDifferentHeaderAndValueSeparator() {
        String headerSeparator = "||";
        String valueSeparator = "|";
        String tableWithCustomSeparator = wikiTableAsString;
        ExamplesTable table = new ExamplesTable(tableWithCustomSeparator, headerSeparator, valueSeparator);
        assertThat(table.getHeaderSeparator(), equalTo(headerSeparator));
        assertThat(table.getValueSeparator(), equalTo(valueSeparator));
        ensureTableContentIsParsed(table);
        assertThat(table.asString(), equalTo(tableWithCustomSeparator));
    }

    @Test
    public void shouldParseTableWithDifferentCustomHeaderAndValueSeparator() {
        String headerSeparator = "!!";
        String valueSeparator = "!";
        String tableWithCustomSeparator = wikiTableAsString.replace("|", "!");
        ExamplesTable table = new ExamplesTable(tableWithCustomSeparator, headerSeparator, valueSeparator);
        assertThat(table.getHeaderSeparator(), equalTo(headerSeparator));
        assertThat(table.getValueSeparator(), equalTo(valueSeparator));
        ensureTableContentIsParsed(table);
        assertThat(table.asString(), equalTo(tableWithCustomSeparator));
    }

    @Test
    public void shouldTrimTableBeforeParsing() {
        String untrimmedTableAsString = "\n    \n" + tableAsString + "\n    \n";
        ExamplesTable table = new ExamplesTable(untrimmedTableAsString);
        ensureTableContentIsParsed(table);
        assertThat(table.asString(), equalTo(untrimmedTableAsString));
    }
    
    @Test
    public void shouldParseEmptyTable() {
        String tableAsString = "";
        ExamplesTable table = new ExamplesTable(tableAsString);
        assertThat(table.asString(), equalTo(tableAsString));
        assertThat(table.getHeaders().size(), equalTo(0));
        assertThat(table.getRows().size(), equalTo(0));
    }

    @Test
    public void shouldParseTableIgnoringDataBeforeLeftBoundarySeparator() {
        String tableAsString = "one|two|\n 11|12|\n 21|22|\n";        
        ExamplesTable table = new ExamplesTable(tableAsString);
        assertThat(table.asString(), equalTo(tableAsString));
        assertThat(table.getHeaders().size(), equalTo(1));
        assertThat(table.getRows().size(), equalTo(2));
        assertThat(tableElement(table, 0, "two"), equalTo("12"));        
        assertThat(tableElement(table, 1, "two"), equalTo("22"));        
    }

    @Test
    public void shouldParseTableWithoutBoundarySeparators() {
        String tableAsString = "one|two\n 11|12\n 21|22\n";        
        ExamplesTable table = new ExamplesTable(tableAsString);
        assertThat(table.asString(), equalTo(tableAsString));
        assertThat(table.getHeaders().size(), equalTo(1));
        assertThat(table.getRows().size(), equalTo(2));
        assertThat(tableElement(table, 0, "two"), equalTo("12"));        
        assertThat(tableElement(table, 1, "two"), equalTo("22"));        
    }

    private void ensureTableContentIsParsed(ExamplesTable table) {
        assertThat(table.getHeaders(), equalTo(asList("one", "two")));
        assertThat(table.getRows().size(), equalTo(2));
        assertThat(tableElement(table, 0, "one"), equalTo("11"));
        assertThat(tableElement(table, 0, "two"), equalTo("12"));
        assertThat(tableElement(table, 1, "one"), equalTo("21"));
        assertThat(tableElement(table, 1, "two"), equalTo("22"));
    }

    private String tableElement(ExamplesTable table, int row, String header) {
        return table.getRow(row).get(header);
    }

}
