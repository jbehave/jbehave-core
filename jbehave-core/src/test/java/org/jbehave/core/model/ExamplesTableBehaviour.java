package org.jbehave.core.model;

import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.codehaus.plexus.util.StringUtils.isBlank;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class ExamplesTableBehaviour {

    private String tableAsString = "|one|two|\n" + "|11|12|\n" + "|21|22|\n";

    private String tableWithSpacesAsString = "|one |two | |\n" + "|11 |12 | |\n" + "| 21| 22| |\n";

    private String wikiTableAsString = "||one||two||\n" + "|11|12|\n" + "|21|22|\n";

    private String tableWithCommentsAsString = "|one|two|\n" + "|-- A comment --|\n" + "|11|12|\n"
            + "|-- Another comment --|\n" + "|21|22|\n";

    @Test
    public void shouldParseTableWithDefaultSeparators() {
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureColumnOrderIsPreserved(table);
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
        ensureColumnOrderIsPreserved(table);
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
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo(tableWithCustomSeparator));
    }

    @Test
    public void shouldTrimTableBeforeParsing() {
        String untrimmedTableAsString = "\n    \n" + tableAsString + "\n    \n";
        ExamplesTable table = new ExamplesTable(untrimmedTableAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo(untrimmedTableAsString));
    }

    @Test
    public void shouldParseTableWithCommentLines() {
        ExamplesTable table = new ExamplesTable(tableWithCommentsAsString);
        assertThat(table.asString(), equalTo(tableWithCommentsAsString));
        ensureColumnOrderIsPreserved(table);
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
        for (Map<String, String> row : table.getRows()) {
            assertThat(row.size(), equalTo(2));
            for (String column : row.keySet()) {
                assertThat(isBlank(row.get(column)), is(true));
            }
        }
        assertThat(table.asString(), equalTo(tableWithEmptyValues));
    }

    @Test
    public void shouldParseTableWithoutLeftBoundarySeparator() {
        String tableAsString = "one|two|\n 11|12|\n 21|22|\n";
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldParseTableWithoutRightBoundarySeparator() {
        String tableAsString = "|one|two\n |11|12\n |21|22\n";
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldParseTableWithoutAnyBoundarySeparators() {
        String tableAsString = "one|two\n 11|12\n 21|22\n";
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldParseTablePreservingWhitespace() {
        String tableWithProperties = "{trim=false}\n" + tableWithSpacesAsString;
        ExamplesTable table = new ExamplesTable(tableWithProperties);
        Properties properties = table.getProperties();
        assertThat(properties.size(), equalTo(1));
        assertThat(properties.getProperty("trim"), equalTo("false"));
        ensureWhitespaceIsPreserved(table);
        assertThat(table.asString(), equalTo(tableWithProperties));
    }

    private void ensureColumnOrderIsPreserved(ExamplesTable table) {
        assertThat(table.getHeaders(), equalTo(asList("one", "two")));
        List<Map<String, String>> rows = table.getRows();
        assertThat(rows.size(), equalTo(2));
        ensureRowContentIs(rows, 0, asList("11", "12"));
        ensureRowContentIs(rows, 1, asList("21", "22"));
        ensureCellContentIs(table, 0, "one", "11");
        ensureCellContentIs(table, 0, "two", "12");
        ensureCellContentIs(table, 1, "one", "21");
        ensureCellContentIs(table, 1, "two", "22");
    }

    private void ensureWhitespaceIsPreserved(ExamplesTable table) {
        assertThat(table.getHeaders(), equalTo(asList("one ", "two ", " ")));
        List<Map<String, String>> rows = table.getRows();
        assertThat(rows.size(), equalTo(2));
        ensureRowContentIs(rows, 0, asList("11 ", "12 ", " "));
        ensureRowContentIs(rows, 1, asList(" 21", " 22", " "));
        ensureCellContentIs(table, 0, "one ", "11 ");
        ensureCellContentIs(table, 0, "two ", "12 ");
        ensureCellContentIs(table, 0, " ", " ");
        ensureCellContentIs(table, 1, "one ", " 21");
        ensureCellContentIs(table, 1, "two ", " 22");
        ensureCellContentIs(table, 1, " ", " ");
    }

    private void ensureRowContentIs(List<Map<String, String>> rows, int row, List<String> expected) {
        assertThat(new ArrayList<String>(rows.get(row).values()), equalTo(expected));
    }

    private void ensureCellContentIs(ExamplesTable table, int row, String header, String value) {
        assertThat(table.getRow(row).get(header), equalTo(value));
    }

    @Test
    public void shouldConvertParameterValuesOfTableRow() throws Exception {
        // Given
        ParameterConverters parameterConverters = new ParameterConverters();
        parameterConverters.addConverters(new MethodReturningConverter(methodFor("convertDate"), this));
        ExamplesTableFactory factory = new ExamplesTableFactory(parameterConverters);

        // When
        String tableAsString = "|one|two|\n|11|22|\n|1/1/2010|2/2/2010|";
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString);

        // Then
        Parameters integers = examplesTable.getRowAsParameters(0);
        assertThat(integers.valueAs("one", Integer.class), equalTo(11));
        assertThat(integers.valueAs("two", Integer.class), equalTo(22));
        Parameters dates = examplesTable.getRowAsParameters(1);
        assertThat(dates.valueAs("one", Date.class), equalTo(convertDate("1/1/2010")));
        assertThat(dates.valueAs("two", Date.class), equalTo(convertDate("2/2/2010")));
    }

    @Test
    public void shouldConvertParameterValuesOfTableRowWithDefaults() throws Exception {
        // Given
        ParameterConverters parameterConverters = new ParameterConverters();
        parameterConverters.addConverters(new MethodReturningConverter(methodFor("convertDate"), this));
        ExamplesTableFactory factory = new ExamplesTableFactory(parameterConverters);

        // When
        String tableDefaultsAsString = "|three|\n|99|";
        ExamplesTable defaultsTable = factory.createExamplesTable(tableDefaultsAsString);

        Parameters defaults = defaultsTable.getRowAsParameters(0);
        String tableAsString = "|one|\n|11|\n|22|";
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString).withDefaults(defaults);

        // Then
        Parameters firstRow = examplesTable.getRowAsParameters(0);
        assertThat(firstRow.valueAs("one", String.class), is("11"));
        assertThat(firstRow.valueAs("one", Integer.class), is(11));
        assertThat(firstRow.valueAs("three", String.class), is("99"));
        assertThat(firstRow.valueAs("three", Integer.class), is(99));
        
        Parameters secondRow = examplesTable.getRowAsParameters(1);
        assertThat(secondRow.valueAs("one", String.class), is("22"));
        assertThat(secondRow.valueAs("one", Integer.class), is(22));
        assertThat(secondRow.valueAs("three", String.class), is("99"));
        assertThat(secondRow.valueAs("three", Integer.class), is(99));

        assertThat(firstRow.valueAs("XX", Integer.class, 13), is(13));
    }

    public Date convertDate(String value) throws ParseException {
        return new SimpleDateFormat("dd/MM/yyyy").parse(value);
    }

    public static Method methodFor(String methodName) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(ExamplesTableBehaviour.class);
        for (MethodDescriptor md : beanInfo.getMethodDescriptors()) {
            if (md.getMethod().getName().equals(methodName)) {
                return md.getMethod();
            }
        }
        return null;
    }

}
