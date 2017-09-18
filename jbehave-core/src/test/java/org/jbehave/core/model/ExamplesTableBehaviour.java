package org.jbehave.core.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.Parameter;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.ExamplesTable.RowNotFound;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.jbehave.core.steps.ConvertedParameters.ValueNotFound;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.ParameterConverters.MethodReturningConverter;
import org.jbehave.core.steps.ParameterConverters.ParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.codehaus.plexus.util.StringUtils.isBlank;

import static org.hamcrest.MatcherAssert.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

public class ExamplesTableBehaviour {

    private String tableAsString = "|one|two|\n" + "|11|12|\n" + "|21|22|\n";

    private String tableWithSpacesAsString = "|one |two | |\n" + "|11 |12 | |\n" + "| 21| 22| |\n";

    private String landscapeTableAsString = "|one|11|21|\n" + "|two|12|22|\n";

    private String wikiTableAsString = "||one||two||\n" + "|11|12|\n" + "|21|22|\n";

    private String tableWithCommentsAsString = "|---------|\n" + "|one|two|\n" + "|-- A comment --|\n" + "|11|12|\n"
            + "|-- Another comment --|\n" + "|21|22|\n";

    @Test
    public void shouldParseTableWithDefaultSeparators() {
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldParseTableWithDifferentSeparators() {
        String headerSeparator = "||";
        String valueSeparator = "|";
        String tableWithCustomSeparator = wikiTableAsString;
        TableTransformers tableTransformers = new TableTransformers();
        ExamplesTable table = new ExamplesTable(tableWithCustomSeparator, headerSeparator, valueSeparator,
                new ParameterConverters(new LoadFromClasspath(), tableTransformers), tableTransformers);
        assertThat(table.getHeaderSeparator(), equalTo(headerSeparator));
        assertThat(table.getValueSeparator(), equalTo(valueSeparator));
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo(tableWithCustomSeparator));
    }

    @Test
    public void shouldParseTableWithDifferentCustomSeparators() {
        String headerSeparator = "!!";
        String valueSeparator = "!";
        String tableWithCustomSeparator = wikiTableAsString.replace("|", "!");
        TableTransformers tableTransformers = new TableTransformers();
        ExamplesTable table = new ExamplesTable(tableWithCustomSeparator, headerSeparator, valueSeparator,
                new ParameterConverters(new LoadFromClasspath(), tableTransformers), tableTransformers);
        assertThat(table.getHeaderSeparator(), equalTo(headerSeparator));
        assertThat(table.getValueSeparator(), equalTo(valueSeparator));
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo("!!one!!two!!\n!11!12!\n!21!22!\n"));
    }

    @Test
    public void shouldTrimTableBeforeParsing() {
        String untrimmedTableAsString = "\n    \n" + tableAsString + "\n    \n";
        ExamplesTable table = new ExamplesTable(untrimmedTableAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo("|one|two|\n|11|12|\n|21|22|\n"));
    }

    @Test
    public void shouldParseTableWithCommentsInValues() {
        String tableWithEmptyValues = "{commentSeparator=#}\n|one #comment|two|\n |11 #comment|12 #comment|\n |21|22|\n";
        ExamplesTable table = new ExamplesTable(tableWithEmptyValues);
        assertThat(table.getRowCount(), equalTo(2));
        for (Parameters row : table.getRowsAsParameters()) {
            Map<String, String> values = row.values();
            assertThat(values.size(), equalTo(2));
            for (String column : values.keySet()) {
                assertThat(values.get(column), not(containsString("#comment")));
            }
        }
        assertThat(table.asString(), equalTo("{commentSeparator=#}\n|one|two|\n|11|12|\n|21|22|\n"));
    }

    @Test
    public void shouldParseTableWithUntrimmedCommentsInValues() {
        String tableWithEmptyValues = "{commentSeparator=#, trim=false}\n|one #comment|two|\n |11 #comment|12 #comment|\n |21|22|\n";
        ExamplesTable table = new ExamplesTable(tableWithEmptyValues);
        assertThat(table.getRowCount(), equalTo(2));
        for (Parameters row : table.getRowsAsParameters()) {
            Map<String, String> values = row.values();
            assertThat(values.size(), equalTo(2));
            for (String column : values.keySet()) {
                assertThat(values.get(column), not(containsString("#comment")));
            }
        }
        assertThat(table.asString(), equalTo("{commentSeparator=#, trim=false}\n|one |two|\n|11 |12 |\n|21|22|\n"));
    }

    @Test
    public void shouldParseEmptyTable() {
        String tableAsString = "";
        ExamplesTable table = new ExamplesTable(tableAsString);
        assertThat(table.getHeaders().size(), equalTo(0));
        assertThat(table.getRows().size(), equalTo(0));
        assertThat(table.getRowsAsParameters().size(), equalTo(0));
        assertThat(table.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldParseTableWithBlankValues() {
        String tableWithEmptyValues = "|one|two|\n |||\n | ||\n || |\n";
        ExamplesTable table = new ExamplesTable(tableWithEmptyValues);
        assertThat(table.getRowCount(), equalTo(3));
        for (Parameters row : table.getRowsAsParameters()) {
            Map<String, String> values = row.values();
            assertThat(values.size(), equalTo(2));
            for (String column : values.keySet()) {
                assertThat(isBlank(values.get(column)), is(true));
            }
        }
        assertThat(table.asString(), equalTo("|one|two|\n|||\n|||\n|||\n"));
    }

    @Test
    public void shouldParseTableWithoutLeftBoundarySeparator() {
        String tableAsString = "one|two|\n 11|12|\n 21|22|\n";
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo("|one|two|\n|11|12|\n|21|22|\n"));
    }

    @Test
    public void shouldParseTableWithoutRightBoundarySeparator() {
        String tableAsString = "|one|two\n |11|12\n |21|22\n";
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo("|one|two|\n|11|12|\n|21|22|\n"));
    }

    @Test
    public void shouldParseTableWithoutAnyBoundarySeparators() {
        String tableAsString = "one|two\n 11|12\n 21|22\n";
        ExamplesTable table = new ExamplesTable(tableAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo("|one|two|\n|11|12|\n|21|22|\n"));
    }

    @Test
    public void shouldParseTablePreservingWhitespace() {
        String tableWithProperties = "{trim=false}\n" + tableWithSpacesAsString;
        ExamplesTable table = new ExamplesTable(tableWithProperties);
        Properties properties = table.getProperties();
        assertThat(properties.getProperty("trim"), equalTo("false"));
        ensureWhitespaceIsPreserved(table);
        assertThat(table.asString(), equalTo("{trim=false}\n|one |two | |\n|11 |12 | |\n| 21| 22| |\n"));
    }

    @Test
    public void shouldParseTableWithSeparatorsSpecifiedViaProperties() {
        String tableWithProperties = "{ignorableSeparator=!--,headerSeparator=!,valueSeparator=!}\n"
                + tableWithCommentsAsString.replace("|", "!");
        ExamplesTable table = new ExamplesTable(tableWithProperties);
        Properties properties = table.getProperties();
        assertThat(properties.getProperty("ignorableSeparator"), equalTo("!--"));
        assertThat(properties.getProperty("headerSeparator"), equalTo("!"));
        assertThat(properties.getProperty("valueSeparator"), equalTo("!"));
        ensureColumnOrderIsPreserved(table);
    }

    @Test
    public void shouldParseTableAsLandscape() {
        String tableWithProperties = "{transformer=FROM_LANDSCAPE}\n" + landscapeTableAsString;
        ExamplesTableFactory factory = createFactory();
        ExamplesTable table = factory.createExamplesTable(tableWithProperties);
        Properties properties = table.getProperties();
        assertThat(properties.getProperty("transformer"), equalTo("FROM_LANDSCAPE"));
        ensureColumnOrderIsPreserved(table);
    }

    @Test
    public void shouldParseTableWithCustomTransformerSpecifiedViaProperties() {
        String tableWithProperties = "{transformer=myTransformer, trim=false}\n" + tableWithCommentsAsString;
        TableTransformers tableTransformers = new TableTransformers();
        tableTransformers.useTransformer("myTransformer", new TableTransformer() {

            @Override
            public String transform(String tableAsString, ExamplesTableProperties properties) {
                return tableWithSpacesAsString;
            }

        });
        ExamplesTable table = new ExamplesTableFactory(new LoadFromClasspath(), tableTransformers)
                .createExamplesTable(tableWithProperties);
        Properties properties = table.getProperties();
        assertThat(properties.getProperty("transformer"), equalTo("myTransformer"));
        ensureWhitespaceIsPreserved(table);
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
        ExamplesTableFactory factory = createFactory(new MethodReturningConverter(methodFor("convertDate"), this));

        // When
        String tableAsString = "|one|two|\n|11|22|\n|1/1/2010|2/2/2010|";
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString);

        // Then
        Parameters integers = examplesTable.getRowAsParameters(0);
        assertThat(integers.<Integer>valueAs("one", Integer.class), equalTo(11));
        assertThat(integers.<Integer>valueAs("two", Integer.class), equalTo(22));
        Parameters dates = examplesTable.getRowAsParameters(1);
        assertThat(dates.<Date>valueAs("one", Date.class), equalTo(convertDate("1/1/2010")));
        assertThat(dates.<Date>valueAs("two", Date.class), equalTo(convertDate("2/2/2010")));
    }

    @Test
    public void shouldConvertParameterValuesOfTableRowWithDefaults() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory(new MethodReturningConverter(methodFor("convertDate"), this));

        // When
        String tableDefaultsAsString = "|three|\n|99|";
        ExamplesTable defaultsTable = factory.createExamplesTable(tableDefaultsAsString);

        Parameters defaults = defaultsTable.getRowAsParameters(0);
        String tableAsString = "|one|\n|11|\n|22|";
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString).withDefaults(defaults);

        // Then
        Parameters firstRow = examplesTable.getRowAsParameters(0);
        Map<String, String> firstRowValues = firstRow.values();
        assertThat(firstRowValues.containsKey("one"), is(true));
        assertThat(firstRow.<String>valueAs("one", String.class), is("11"));
        assertThat(firstRow.<Integer>valueAs("one", Integer.class), is(11));
        assertThat(firstRowValues.containsKey("three"), is(true));
        assertThat(firstRow.<String>valueAs("three", String.class), is("99"));
        assertThat(firstRow.<Integer>valueAs("three", Integer.class), is(99));
        assertThat(firstRowValues.containsKey("XX"), is(false));
        assertThat(firstRow.valueAs("XX", Integer.class, 13), is(13));

        Parameters secondRow = examplesTable.getRowAsParameters(1);
        Map<String, String> secondRowValues = secondRow.values();
        assertThat(secondRowValues.containsKey("one"), is(true));
        assertThat(secondRow.<String>valueAs("one", String.class), is("22"));
        assertThat(secondRow.<Integer>valueAs("one", Integer.class), is(22));
        assertThat(secondRowValues.containsKey("three"), is(true));
        assertThat(secondRow.<String>valueAs("three", String.class), is("99"));
        assertThat(secondRow.<Integer>valueAs("three", Integer.class), is(99));
        assertThat(secondRowValues.containsKey("XX"), is(false));
        assertThat(secondRow.valueAs("XX", Integer.class, 13), is(13));

    }

    @Test
    public void shouldReplaceNamedParameterValues() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory();

        // When
        String tableAsString = "|Name|Value|\n|name1|<value>|";
        Map<String, String> namedParameters = new HashMap<String, String>();
        namedParameters.put("<value>", "value1");
        ExamplesTable table = factory.createExamplesTable(tableAsString).withNamedParameters(namedParameters);

        // Then
        Parameters firstRow = table.getRowsAsParameters(true).get(0);
        Map<String, String> firstRowValues = firstRow.values();
        assertThat(firstRowValues.containsKey("Value"), is(true));
        assertThat(firstRow.<String>valueAs("Value", String.class), is("value1"));

    }

    /**
     * The values given named parameter values as strings should not suffer any modification after are replaced in table.
     *
     * @see {@link String#replaceAll(String, String)} to see why are not present in values the '\' and '$' characters.
     */
    @Test
    public void shouldKeepExactValueInReplacedNamedParameterValues() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory();
        String problematicNamedParameterValueCharacters = "value having the \\ backslash and the $ dollar character";

        // When
        String tableAsString = "|Name|Value|\n|name|<value>|";
        Map<String, String> namedParameters = new HashMap<String, String>();
        namedParameters.put("<value>", problematicNamedParameterValueCharacters);
        ExamplesTable table = factory.createExamplesTable(tableAsString).withNamedParameters(namedParameters);

        // Then
        Parameters firstRow = table.getRowsAsParameters(true).get(0);
        Map<String, String> firstRowValues = firstRow.values();
        assertThat(firstRowValues.containsKey("Value"), is(true));
        assertThat(firstRow.<String>valueAs("Value", String.class), is(problematicNamedParameterValueCharacters));

    }

    @Test
    public void shouldMapParametersToType() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory();

        // When
        String tableAsString = "|string|integer|stringList|integerList|\n|11|22|1,1|2,2|";
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString);

        // Then
        for (MyParameters parameters : examplesTable.getRowsAs(MyParameters.class)) {
            assertThat(parameters.string, equalTo("11"));
            assertThat(parameters.integer, equalTo(22));
            assertThat(parameters.stringList, equalTo(asList("1", "1")));
            assertThat(parameters.integerList, equalTo(asList(2, 2)));
        }
    }

    @Test
    public void shouldMapParametersToTypeWithFieldMappings() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory();

        // When
        String tableAsString = "|aString|anInteger|aStringList|anIntegerList|\n|11|22|1,1|2,2|";
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString);

        Map<String, String> nameMapping = new HashMap<String, String>();
        nameMapping.put("aString", "string");
        nameMapping.put("anInteger", "integer");
        nameMapping.put("aStringList", "stringList");
        nameMapping.put("anIntegerList", "integerList");

        // Then
        for (MyParameters parameters : examplesTable.getRowsAs(MyParameters.class, nameMapping)) {
            assertThat(parameters.string, equalTo("11"));
            assertThat(parameters.integer, equalTo(22));
            assertThat(parameters.stringList, equalTo(asList("1", "1")));
            assertThat(parameters.integerList, equalTo(asList(2, 2)));
        }
    }

    @Test
    public void shouldMapParametersToTypeWithAnnotatedFields() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory();

        // When
        String tableAsString = "|aString|anInteger|aStringList|anIntegerList|\n|11|22|1,1|2,2|";
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString);

        // Then
        for (MyParametersWithAnnotatedFields parameters : examplesTable.getRowsAs(MyParametersWithAnnotatedFields.class)) {
            assertThat(parameters.string, equalTo("11"));
            assertThat(parameters.integer, equalTo(22));
            assertThat(parameters.stringList, equalTo(asList("1", "1")));
            assertThat(parameters.integerList, equalTo(asList(2, 2)));
        }
    }

    @Test
    public void shouldThrowExceptionIfValuesOrRowsAreNotFound() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory();

        // When
        String tableAsString = "|one|two|\n|11|22|\n";
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString);

        // Then
        Parameters integers = examplesTable.getRowAsParameters(0);
        assertThat(integers.<Integer>valueAs("one", Integer.class), equalTo(11));
        try {
            integers.valueAs("unknown", Integer.class);
            fail("Exception was not thrown");
        } catch (ValueNotFound e) {
            assertThat(e.getMessage(), equalTo("unknown"));
        }
        try {
            examplesTable.getRowAsParameters(1);
            fail("Exception was not thrown");
        } catch (RowNotFound e) {
            assertThat(e.getMessage(), equalTo("1"));
        }

    }

    @Test
    public void shouldAllowAdditionAndModificationOfRowValues() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory();

        // When
        String tableAsString = "|one|two|\n|11|12|\n|21|22|";
        ExamplesTable examplesTable = factory.createExamplesTable(tableAsString);
        Map<String, String> values = new HashMap<String, String>();
        values.put("one", "111");
        values.put("three", "333");
        examplesTable.withRowValues(0, values);
        Map<String, String> otherValues = new HashMap<String, String>();
        otherValues.put("two", "222");
        examplesTable.withRowValues(1, otherValues);

        // Then
        Parameters firstRow = examplesTable.getRowAsParameters(0);
        assertThat(firstRow.<Integer>valueAs("one", Integer.class), equalTo(111));
        assertThat(firstRow.<Integer>valueAs("two", Integer.class), equalTo(12));
        assertThat(firstRow.<Integer>valueAs("three", Integer.class), equalTo(333));
        Parameters secondRow = examplesTable.getRowAsParameters(1);
        assertThat(secondRow.<Integer>valueAs("one", Integer.class), equalTo(21));
        assertThat(secondRow.<Integer>valueAs("two", Integer.class), equalTo(222));
        assertThat(secondRow.<String>valueAs("three", String.class), equalTo(""));
        assertThat(examplesTable.asString(), equalTo("|one|two|three|\n|111|12|333|\n|21|222||\n"));
    }

    @Test
    public void shouldAllowBuildingOfTableFromContent() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory();

        // When
        String tableAsString = "|one|two|\n|11|12|\n|21|22|";
        ExamplesTable originalTable = factory.createExamplesTable(tableAsString);
        List<Map<String, String>> content = originalTable.getRows();
        content.get(0).put("three", "13");
        content.get(1).put("three", "23");
        ExamplesTable updatedTable = originalTable.withRows(content);

        // Then
        assertThat(updatedTable.asString(), equalTo("|one|two|three|\n|11|12|13|\n|21|22|23|\n"));
    }

    @Test
    public void shouldAllowOutputToPrintStream() throws Exception {
        // Given
        ExamplesTableFactory factory = createFactory();

        // When
        String tableAsString = "|one|two|\n|11|12|\n|21|22|\n";
        ExamplesTable table = factory.createExamplesTable(tableAsString);
        OutputStream out = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(out);
        table.outputTo(output);

        // Then
        assertThat(out.toString(), equalTo(tableAsString));
    }

    @Test
    public void shouldIgnoreEmptyLines() throws Exception {
        // ignore blank line
        String tableWithEmptyLine = "|one|two|\n|a|b|\n\n|c|d|\n";
        ExamplesTable table = new ExamplesTable(tableWithEmptyLine);
        assertThat(table.getRowCount(), equalTo(2));
        assertThat(table.asString(), equalTo("|one|two|\n|a|b|\n|c|d|\n"));
    }

    @Test
    public void shouldIgnoreAllCommentLines() {
        // ignore comment lines
        ExamplesTable table = new ExamplesTable(tableWithCommentsAsString);
        ensureColumnOrderIsPreserved(table);
        assertThat(table.asString(), equalTo(tableAsString));
    }

    @Test
    public void shouldHandleWrongNumberOfColumns() {
        assertTableAsString("|a|b|\n|a|\n", "|a|b|\n|a||\n");
        assertTableAsString("|a|b|\n|a|b|c|\n", "|a|b|\n|a|b|\n");
    }

    private ExamplesTableFactory createFactory(ParameterConverter... converters) {
        LoadFromClasspath resourceLoader = new LoadFromClasspath();
        TableTransformers tableTransformers = new TableTransformers();
        ParameterConverters parameterConverters = new ParameterConverters(resourceLoader, tableTransformers);
        parameterConverters.addConverters(converters);
        return new ExamplesTableFactory(resourceLoader, parameterConverters, tableTransformers);
    }

    private void assertTableAsString(String tableAsString, String expectedTableAsString) {
        assertThat(new ExamplesTable(tableAsString).asString(), equalTo(expectedTableAsString));
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

    @AsParameters
    public static class MyParameters {

        private String string;
        private Integer integer;
        private List<String> stringList;
        private List<Integer> integerList;

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

    @AsParameters
    public static class MyParametersWithAnnotatedFields {

        @Parameter(name = "aString")
        private String string;
        @Parameter(name = "anInteger")
        private Integer integer;
        @Parameter(name = "aStringList")
        private List<String> stringList;
        @Parameter(name = "anIntegerList")
        private List<Integer> integerList;

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
        }
    }

}
