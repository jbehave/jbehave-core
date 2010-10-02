package org.jbehave.core.model;

import static java.util.Arrays.asList;
import static org.codehaus.plexus.util.StringUtils.isBlank;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Map;

import org.junit.Test;

public class ExamplesTableBehaviour {
    
    private String tableAsString = "|one|two|\n" + "|11|12|\n" + "|21|22|\n";

    private String wikiTableAsString = "||one||two||\n" + "|11|12|\n" + "|21|22|\n";

    private String tableWithCommentsAsString = "|one|two|\n" + "|-- A comment --|\n" + "|11|12|\n" + "|-- Another comment --|\n" + "|21|22|\n";

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
    public void shouldParseTableWithCommentLines() {
        ExamplesTable table = new ExamplesTable(tableWithCommentsAsString);
        assertThat(table.asString(), equalTo(tableWithCommentsAsString));
        ensureTableContentIsParsed(table);
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
    public void shouldParseTableWithBlankValues() {
        String tableWithEmptyValues = "|one|two|\n |||\n | ||\n || |\n";
        ExamplesTable table = new ExamplesTable(tableWithEmptyValues);
        assertThat(table.getRowCount(), equalTo(3));
        for (Map<String,String> row : table.getRows()) {
            assertThat(row.size(), equalTo(2));
            for (String column : row.keySet() ) {
                assertThat(isBlank(row.get(column)), is(true));                
            }
        }
        assertThat(table.asString(), equalTo(tableWithEmptyValues));
    }

    @Test
    public void shouldParseTableWithoutLeftBoundarySeparator() {
        String tableAsString = "one|two|\n 11|12|\n 21|22|\n";        
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureTableContentIsParsed(table);
        assertThat(table.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldParseTableWithoutRightBoundarySeparator() {
        String tableAsString = "|one|two\n |11|12\n |21|22\n";        
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureTableContentIsParsed(table);
        assertThat(table.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldParseTableWithoutAnyBoundarySeparators() {
        String tableAsString = "one|two\n 11|12\n 21|22\n";        
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureTableContentIsParsed(table);
        assertThat(table.asString(), equalTo(tableAsString));
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
