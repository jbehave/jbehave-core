package org.jbehave.core.model;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.Parameter;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.jbehave.core.steps.ChainedRow;
import org.jbehave.core.steps.ConvertedParameters;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Parameters;
import org.jbehave.core.steps.Row;

import static java.util.regex.Pattern.DOTALL;

/**
 * <p>
 * Represents a tabular structure that holds rows of example data for parameters
 * named via the column headers:
 * <p/>
 * 
 * <pre>
 * |header 1|header 2| .... |header n|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |value m1|value m2| .... |value mn|
 * </pre>
 * <p>
 * Different header and value column separators can be specified to replace the
 * default separator "|":
 * </p>
 * 
 * <pre>
 * !!header 1!!header 2!! .... !!header n!!
 * !value 11!value 12! .... !value 1n!
 * ...
 * !value m1!value m2| .... !value mn!
 * </pre>
 * <p>
 * Rows starting with an ignorable separator are allowed and ignored:
 * </p>
 * 
 * <pre>
 * |header 1|header 2| .... |header n|
 * |-- A commented row --|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |-- Another commented row --|
 * |value m1|value m2| .... |value mn|
 * </pre>
 * <p>
 * Ignorable separator is configurable and defaults to "|--".
 * </p>
 * <p>
 * The separators are also configurable via inlined properties:
 * 
 * <pre>
 * {ignorableSeparator=!--,headerSeparator=!,valueSeparator=!}
 * !header 1!header 2! .... !header n!
 * !-- A commented row --!
 * !value 11!value 12! .... !value 1n!
 * ...
 * !-- Another commented row --!
 * !value m1!value m2! .... !value mn!
 * </pre>
 * 
 * </p>
 * <p>
 * By default all column values are trimmed. To avoid trimming the values, use
 * the "trim" inlined property:
 * 
 * <pre>
 * {trim=false}
 * | header 1 | header 2 | .... | header n |
 * | value 11 | value 12 | .... | value 1n |
 * </pre>
 * 
 * <p>
 * Comments is column values are supported via the "commentSeparator" inlined property:
 * 
 * <pre>
 * {commentSeparator=#}
 * | header 1#comment | header 2 | .... | header n |
 * | value 11#comment | value 12 | .... | value 1n |
 * </pre>
 * 
 * Comments including the separator are stripped. 
 * </p>
 * 
 * <p>
 * The table allows the retrieval of row values as converted parameters. Use
 * {@link #getRowAsParameters(int)} and invoke
 * {@link Parameters#valueAs(String, Class)} specifying the header and the class
 * type of the parameter.
 * </p>
 * 
 * <p>
 * The table allows the transformation of its string representation via the
 * "transformer" inlined property:
 * 
 * <pre>
 * {transformer=myTransformerName}
 * |header 1|header 2| .... |header n|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |value m1|value m2| .... |value mn|
 * </pre>
 * 
 * The transformer needs to be registered by name via the
 * {@link TableTransformers#useTransformer(String, TableTransformer)}. A few
 * transformers are already registered by default in {@link TableTransformers}.
 * </p>
 * 
 * <p>
 * The table allow filtering on meta by row via the "metaByRow" inlined property:
 * 
 * <pre>
 * {metaByRow=true}
 * | Meta:       | header 1 | .... | header n |
 * | @name=value | value 11 | .... | value 1n |
 * </pre>
 * 
 * </p>
 * <p>
 * Once created, the table row can be modified, via the
 * {@link #withRowValues(int, Map)} method, by specifying the map of row values
 * to be changed.
 * </p>
 * 
 * <p>
 * A table can also be created by providing the entire data content, via the
 * {@link #withRows(List<Map<String,String>>)} method.
 * 
 * </p>
 * The parsing code assumes that the number of columns for data rows is the same
 * as in the header, if a row has less fields, the remaining are filled with
 * empty values, if it has more, the fields are ignored.
 * <p>
 */
public class ExamplesTable {
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
    private static final String EMPTY_VALUE = "";

    public static final Pattern INLINED_PROPERTIES_PATTERN = Pattern.compile("\\{(.*?)\\}\\s*(.*)", DOTALL);
    public static final ExamplesTable EMPTY = new ExamplesTable("");

    private static final String ROW_SEPARATOR_PATTERN = "\r?\n";
    private static final String HEADER_SEPARATOR = "|";
    private static final String VALUE_SEPARATOR = "|";
    private static final String IGNORABLE_SEPARATOR = "|--";

    private final String tableAsString;
    private final ParameterConverters parameterConverters;
    private final TableTransformers tableTransformers;
    private final Row defaults;
    private final List<String> headers = new ArrayList<String>();
    private final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    private ExamplesTableProperties properties;
    private String propertiesAsString = "";
    private Map<String, String> namedParameters = new HashMap<String, String>();

    public ExamplesTable(String tableAsString) {
        this(tableAsString, HEADER_SEPARATOR, VALUE_SEPARATOR,
                new ParameterConverters(new LoadFromClasspath(), new TableTransformers()), new TableTransformers());
    }

    public ExamplesTable(String tableAsString, String headerSeparator, String valueSeparator,
            ParameterConverters parameterConverters, TableTransformers tableTransformers) {
        this(tableAsString, headerSeparator, valueSeparator, IGNORABLE_SEPARATOR, parameterConverters,
                tableTransformers);
    }

    public ExamplesTable(String tableAsString, String headerSeparator, String valueSeparator,
            String ignorableSeparator, ParameterConverters parameterConverters, TableTransformers tableTransformers) {
        this.tableAsString = tableAsString;
        this.parameterConverters = parameterConverters;
        this.tableTransformers = tableTransformers;
        this.defaults = new ConvertedParameters(EMPTY_MAP, parameterConverters);
        parse(headerSeparator, valueSeparator, ignorableSeparator);
    }

    private void parse(String headerSeparator, String valueSeparator, String ignorableSeparator) {
        String tableWithoutProperties = stripProperties(tableAsString.trim());
        properties = new ExamplesTableProperties(propertiesAsString, headerSeparator, valueSeparator, ignorableSeparator);
        parseTable(tableWithoutProperties);
    }

    private ExamplesTable(ExamplesTable other, Row defaults) {
        this.data.addAll(other.data);
        this.tableAsString = other.tableAsString;
        this.parameterConverters = other.parameterConverters;
        this.tableTransformers = other.tableTransformers;
        this.headers.addAll(other.headers);
        this.properties = other.properties;
        this.defaults = defaults;
    }

    private String stripProperties(String tableAsString) {
        Matcher matcher = INLINED_PROPERTIES_PATTERN.matcher(tableAsString);
        if (matcher.matches()) {
            propertiesAsString = matcher.group(1);
            return matcher.group(2);
        }
        return tableAsString;
    }

    private void parseTable(String tableAsString) {
        headers.clear();
        data.clear();
        String transformer = properties.getTransformer();
        if (transformer != null) {
            tableAsString = tableTransformers.transform(transformer, tableAsString, properties);
        }
        parseByRows(headers, data, tableAsString);
    }

    private void parseByRows(List<String> headers, List<Map<String, String>> data, String tableAsString) {
        String[] rows = tableAsString.split(ROW_SEPARATOR_PATTERN);
        for (int row = 0; row < rows.length; row++) {
            String rowAsString = rows[row];
            if (rowAsString.startsWith(properties.getIgnorableSeparator()) || rowAsString.length() == 0) {
                // skip ignorable or empty lines
                continue;
            } else if (headers.isEmpty()) {
                headers.addAll(TableUtils.parseRow(rowAsString, true, properties));
            } else {
                List<String> columns = TableUtils.parseRow(rowAsString, false, properties);
                Map<String, String> map = new LinkedHashMap<String, String>();
                for (int column = 0; column < columns.size(); column++) {
                    if (column < headers.size()) {
                        map.put(headers.get(column), columns.get(column));
                    }
                }
                data.add(map);
            }
        }
    }

    public ExamplesTable withDefaults(Parameters defaults) {
        return new ExamplesTable(this, new ChainedRow(defaults, this.defaults));
    }

    public ExamplesTable withNamedParameters(Map<String, String> namedParameters) {
        this.namedParameters = namedParameters;
        return this;
    }

    public ExamplesTable withRowValues(int row, Map<String, String> values) {
        getRow(row).putAll(values);
        for (String header : values.keySet()) {
            if (!headers.contains(header)) {
                headers.add(header);
            }
        }
        return this;
    }

    public ExamplesTable withRows(List<Map<String, String>> values) {
        this.data.clear();
        this.data.addAll(values);
        this.headers.clear();
        this.headers.addAll(values.get(0).keySet());
        return this;
    }

    public Properties getProperties() {
        return properties.getProperties();
    }

    public List<String> getHeaders() {
        return headers;
    }

    public Map<String, String> getRow(int row) {
        if (row > data.size() - 1) {
            throw new RowNotFound(row);
        }
        Map<String, String> values = data.get(row);
        if (headers.size() != values.keySet().size()) {
            for (String header : headers) {
                if (!values.containsKey(header)) {
                    values.put(header, EMPTY_VALUE);
                }
            }
        }
        return values;
    }

    public Parameters getRowAsParameters(int row) {
        return getRowAsParameters(row, false);
    }

    public Parameters getRowAsParameters(int row, boolean replaceNamedParameters) {
        Map<String, String> rowValues = getRow(row);
        return createParameters((replaceNamedParameters ? replaceNamedParameters(rowValues) : rowValues));
    }

    private Map<String, String> replaceNamedParameters(Map<String, String> row) {
        Map<String, String> replaced = new LinkedHashMap<String, String>();
        for (Entry<String, String> rowEntry : row.entrySet()) {
            String replacedValue = rowEntry.getValue();
            for (String namedKey : namedParameters.keySet()) {
                String namedValue = namedParameters.get(namedKey);
                replacedValue = replacedValue.replaceAll(namedKey, Matcher.quoteReplacement(namedValue));
            }
            replaced.put(rowEntry.getKey(), replacedValue);
        }
        return replaced;
    }

    public int getRowCount() {
        return data.size();
    }

    public boolean metaByRow(){
        return properties.isMetaByRow();
    }

    public List<Map<String, String>> getRows() {
        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        for (int row = 0; row < getRowCount(); row++) {
            rows.add(getRow(row));
        }
        return rows;
    }

    public List<Parameters> getRowsAsParameters() {
        return getRowsAsParameters(false);
    }

    public List<Parameters> getRowsAsParameters(boolean replaceNamedParameters) {
        List<Parameters> rows = new ArrayList<Parameters>();

        for (int row = 0; row < getRowCount(); row++) {
            rows.add(getRowAsParameters(row, replaceNamedParameters));
        }

        return rows;
    }

    public <T> List<T> getRowsAs(Class<T> type) {
        return getRowsAs(type, new HashMap<String, String>());
    }

    public <T> List<T> getRowsAs(Class<T> type, Map<String, String> fieldNameMapping) {
        List<T> rows = new ArrayList<T>();

        for (Parameters parameters : getRowsAsParameters()) {
            rows.add(mapToType(parameters, type, fieldNameMapping));
        }

        return rows;
    }

    private <T> T mapToType(Parameters parameters, Class<T> type, Map<String, String> fieldNameMapping) {
        try {
            T instance = type.newInstance();
            Map<String, String> values = parameters.values();
            for (String name : values.keySet()) {
                Field field = findField(type, name, fieldNameMapping);
                Type fieldType = field.getGenericType();
                Object value = parameters.valueAs(name, fieldType);
                field.setAccessible(true);
                field.set(instance, value);
            }
            return instance;
        } catch (Exception e) {
            throw new ParametersNotMappableToType(parameters, type, e);
        }
    }

    private <T> Field findField(Class<T> type, String name, Map<String, String> fieldNameMapping)
            throws NoSuchFieldException {
        // Get field name from mapping, if specified
        String fieldName = fieldNameMapping.get(name);
        if (fieldName == null) {
            fieldName = name;
        }
        // First look for fields annotated by @Parameter specifying the name
        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(Parameter.class)) {
                Parameter parameter = field.getAnnotation(Parameter.class);
                if (fieldName.equals(parameter.name())) {
                    return field;
                }
            }
        }
        // Default to field matching given name
        return type.getDeclaredField(fieldName);
    }

    private Parameters createParameters(Map<String, String> values) {
        return new ConvertedParameters(new ChainedRow(new ConvertedParameters(values, parameterConverters), defaults),
                parameterConverters);
    }

    public String getHeaderSeparator() {
        return properties.getHeaderSeparator();
    }

    public String getValueSeparator() {
        return properties.getValueSeparator();
    }

    public String asString() {
        if (data.isEmpty()) {
            return EMPTY_VALUE;
        }
        return format();
    }

    public void outputTo(PrintStream output) {
        output.print(asString());
    }

    private String format() {
        StringBuilder sb = new StringBuilder();
        if (!propertiesAsString.isEmpty()){
            sb.append("{").append(propertiesAsString).append("}").append(properties.getRowSeparator());
        }
        for (String header : headers) {
            sb.append(getHeaderSeparator()).append(header);
        }
        sb.append(getHeaderSeparator()).append(properties.getRowSeparator());
        for (Map<String, String> row : getRows()) {
            for (String header : headers) {
                sb.append(getValueSeparator()).append(row.get(header));
            }
            sb.append(getValueSeparator()).append(properties.getRowSeparator());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @SuppressWarnings("serial")
    public static class RowNotFound extends RuntimeException {

        public RowNotFound(int row) {
            super(Integer.toString(row));
        }

    }

    @SuppressWarnings("serial")
    public static class ParametersNotMappableToType extends RuntimeException {

        public ParametersNotMappableToType(Parameters parameters, Class<?> type, Exception e) {
            super(parameters.values() + " not mappable to type " + type, e);
        }

    }
}
