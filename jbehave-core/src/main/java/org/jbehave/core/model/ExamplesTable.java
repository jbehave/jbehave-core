package org.jbehave.core.model;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.Parameter;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.jbehave.core.steps.ChainedRow;
import org.jbehave.core.steps.ConvertedParameters;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Parameters;
import org.jbehave.core.steps.Row;

import static java.lang.Boolean.parseBoolean;
import static java.util.regex.Pattern.DOTALL;
import static org.apache.commons.lang3.StringUtils.isEmpty;

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
 * {@link Parameters#valueAs(String, Type)} specifying the header and the class
 * type of the parameter.
 * </p>
 * 
 * <p>
 * The table allows the transformation of its string representation via the
 * "transformer" inlined property (multiple transformers will be applied in chain-mode):
 * 
 * <pre>
 * {transformer=myTransformerName}
 * {transformer=myOtherTransformerName}
 * |header 1|header 2| .... |header n|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |value m1|value m2| .... |value mn|
 * </pre>
 * 
 * The transformers need to be registered by name via the
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

    public static final Pattern INLINED_PROPERTIES_PATTERN = Pattern.compile("\\{(.*?[^\\\\])\\}\\s*(.*)", DOTALL);
    public static final ExamplesTable EMPTY = new ExamplesTable("");

    private static final String ROW_SEPARATOR_PATTERN = "\r?\n";
    private static final String HEADER_SEPARATOR = "|";
    private static final String VALUE_SEPARATOR = "|";
    private static final String IGNORABLE_SEPARATOR = "|--";

    private final ParameterConverters parameterConverters;
    private final TableParsers tableParsers;
    private final TableTransformers tableTransformers;
    private final Row defaults;
    private final List<String> headers = new ArrayList<>();
    private final List<Map<String, String>> data = new ArrayList<>();
    private final Deque<ExamplesTableProperties> propertiesList = new LinkedList<>();

    private Map<String, String> namedParameters = new HashMap<>();
    private ParameterControls parameterControls;

    public ExamplesTable(String tableAsString) {
        this(tableAsString, HEADER_SEPARATOR, VALUE_SEPARATOR,
                new ParameterConverters(new LoadFromClasspath(), new TableTransformers()), new ParameterControls(),
                new TableParsers(), new TableTransformers());
    }

    public ExamplesTable(String tableAsString, String headerSeparator, String valueSeparator,
                         ParameterConverters parameterConverters, ParameterControls parameterControls,
                         TableParsers tableParsers, TableTransformers tableTransformers) {
        this(tableAsString, headerSeparator, valueSeparator, IGNORABLE_SEPARATOR, parameterConverters,
                parameterControls, tableParsers, tableTransformers);
    }

    public ExamplesTable(String tableAsString, String headerSeparator, String valueSeparator,
                         String ignorableSeparator, ParameterConverters parameterConverters, ParameterControls parameterControls,
                         TableParsers tableParsers, TableTransformers tableTransformers) {
        this.parameterConverters = parameterConverters;
        this.parameterControls = parameterControls;
        this.tableParsers = tableParsers;
        this.tableTransformers = tableTransformers;
        this.defaults = new ConvertedParameters(EMPTY_MAP, parameterConverters);
        ExamplesTableData data = tableParsers.parseData(tableAsString, headerSeparator, valueSeparator, ignorableSeparator);
        this.propertiesList.addAll(data.getProperties());
        String transformedTable = applyTransformers(data.getTable());
        parseByRows(transformedTable);
    }

    ExamplesTable(ExamplesTableData examplesTableData, String headerSeparator, String valueSeparator,
                  String ignorableSeparator, ParameterConverters parameterConverters, ParameterControls parameterControls,
                  TableParsers tableParsers, TableTransformers tableTransformers) {
        this.parameterConverters = parameterConverters;
        this.parameterControls = parameterControls;
        this.tableParsers = tableParsers;
        this.tableTransformers = tableTransformers;
        this.defaults = new ConvertedParameters(EMPTY_MAP, parameterConverters);
        this.propertiesList.addAll(examplesTableData.getProperties());
        String transformedTable = applyTransformers(examplesTableData.getTable());
        parseByRows(transformedTable);
    }

    private ExamplesTable(ExamplesTable other, Row defaults) {
        this.data.addAll(other.data);
        this.parameterConverters = other.parameterConverters;
        this.tableParsers = other.tableParsers;
        this.tableTransformers = other.tableTransformers;
        this.headers.addAll(other.headers);
        this.propertiesList.addAll(other.propertiesList);
        this.defaults = defaults;
    }

    private String applyTransformers(String tableAsString) {
        String transformedTable = tableAsString;
        for (ExamplesTableProperties properties : propertiesList) {
            String transformer = properties.getTransformer();
            if (transformer != null) {
                transformedTable = tableTransformers.transform(transformer, transformedTable, tableParsers, properties);
            }
        }
        return transformedTable;
    }

    private void parseByRows(String tableAsString) {
        String[] rows = tableAsString.split(ROW_SEPARATOR_PATTERN);
        for (String row : rows) {
            if (row.startsWith(getExampleTableProperties().getIgnorableSeparator()) || row.isEmpty()) {
                // skip ignorable or empty lines
                continue;
            } else if (headers.isEmpty()) {
                headers.addAll(tableParsers.parseRow(row, true, getExampleTableProperties()));
            } else {
                List<String> columns = tableParsers.parseRow(row, false, getExampleTableProperties());
                Map<String, String> map = new LinkedHashMap<>();
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
        return getExampleTableProperties().getProperties();
    }

    private ExamplesTableProperties getExampleTableProperties() {
        return propertiesList.getLast();
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
        return createParameters(replaceNamedParameters ? replaceNamedParameters(rowValues) : rowValues);
    }

    private Map<String, String> replaceNamedParameters(Map<String, String> row) {
        Map<String, String> replaced = new LinkedHashMap<>();
        for (Entry<String, String> rowEntry : row.entrySet()) {
            String replacedValue = rowEntry.getValue();
            for (Entry<String, String> namedParameter : namedParameters.entrySet()) {
                replacedValue = parameterControls.replaceAllDelimitedNames(replacedValue, namedParameter.getKey(),
                        namedParameter.getValue());
            }
            replaced.put(rowEntry.getKey(), replacedValue);
        }
        return replaced;
    }

    public int getRowCount() {
        return data.size();
    }

    public boolean metaByRow(){
        return getExampleTableProperties().isMetaByRow();
    }

    public List<Map<String, String>> getRows() {
        List<Map<String, String>> rows = new ArrayList<>();
        for (int row = 0; row < getRowCount(); row++) {
            rows.add(getRow(row));
        }
        return rows;
    }

    public List<Parameters> getRowsAsParameters() {
        return getRowsAsParameters(false);
    }

    public List<Parameters> getRowsAsParameters(boolean replaceNamedParameters) {
        List<Parameters> rows = new ArrayList<>();

        for (int row = 0; row < getRowCount(); row++) {
            rows.add(getRowAsParameters(row, replaceNamedParameters));
        }

        return rows;
    }

    public <T> List<T> getRowsAs(Class<T> type) {
        return getRowsAs(type, new HashMap<String, String>());
    }

    public <T> List<T> getRowsAs(Class<T> type, Map<String, String> fieldNameMapping) {
        List<T> rows = new ArrayList<>();

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
        return findField(type, fieldName);
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        for (Field field : type.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        if (type.getSuperclass() != null) {
            return findField(type.getSuperclass(), fieldName);
        }
        throw new NoSuchFieldException(fieldName);
    }

    private Parameters createParameters(Map<String, String> values) {
        return new ConvertedParameters(new ChainedRow(new ConvertedParameters(values, parameterConverters), defaults),
                parameterConverters);
    }

    public String getHeaderSeparator() {
        return getExampleTableProperties().getHeaderSeparator();
    }

    public String getValueSeparator() {
        return getExampleTableProperties().getValueSeparator();
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
        for (ExamplesTableProperties properties : propertiesList) {
            String propertiesAsString = properties.getPropertiesAsString();
            if (!propertiesAsString.isEmpty()) {
                sb.append("{").append(propertiesAsString).append("}").append(getExampleTableProperties().getRowSeparator());
            }
        }
        for (String header : headers) {
            sb.append(getHeaderSeparator()).append(header);
        }
        sb.append(getHeaderSeparator()).append(getExampleTableProperties().getRowSeparator());
        for (Map<String, String> row : getRows()) {
            for (String header : headers) {
                sb.append(getValueSeparator()).append(row.get(header));
            }
            sb.append(getValueSeparator()).append(getExampleTableProperties().getRowSeparator());
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

    public static final class ExamplesTableProperties {

        private static final String HEADER_SEPARATOR = "|";
        private static final String VALUE_SEPARATOR = "|";
        private static final String IGNORABLE_SEPARATOR = "|--";
        private static final String COMMENT_SEPARATOR = "#";

        private static final String HEADER_SEPARATOR_KEY = "headerSeparator";
        private static final String VALUE_SEPARATOR_KEY = "valueSeparator";
        private static final String IGNORABLE_SEPARATOR_KEY = "ignorableSeparator";
        private static final String COMMENT_SEPARATOR_KEY = "commentSeparator";

        private static final String ROW_SEPARATOR = "\n";

        private final Properties properties = new Properties();
        private final String propertiesAsString;

        public ExamplesTableProperties(Properties properties){
            this.properties.putAll(properties);
            if ( !this.properties.containsKey(HEADER_SEPARATOR_KEY) ){
                this.properties.setProperty(HEADER_SEPARATOR_KEY, HEADER_SEPARATOR);
            }
            if ( !this.properties.containsKey(VALUE_SEPARATOR_KEY) ){
                this.properties.setProperty(VALUE_SEPARATOR_KEY, VALUE_SEPARATOR);
            }
            if ( !this.properties.containsKey(IGNORABLE_SEPARATOR_KEY) ){
                this.properties.setProperty(IGNORABLE_SEPARATOR_KEY, IGNORABLE_SEPARATOR);
            }
            if ( !this.properties.containsKey(COMMENT_SEPARATOR_KEY) ){
                this.properties.setProperty(COMMENT_SEPARATOR_KEY, COMMENT_SEPARATOR);
            }
            StringBuilder propertiesAsStringBuilder = new StringBuilder();
            for (Map.Entry<Object, Object> property : this.properties.entrySet()) {
                propertiesAsStringBuilder.append(property.getKey()).append('=').append(property.getValue()).append(',');
            }
            propertiesAsString = propertiesAsStringBuilder.substring(0, propertiesAsStringBuilder.length() - 1);
        }

        public ExamplesTableProperties(String propertiesAsString, String defaultHeaderSeparator, String defaultValueSeparator,
                String defaultIgnorableSeparator) {
            properties.setProperty(HEADER_SEPARATOR_KEY, defaultHeaderSeparator);
            properties.setProperty(VALUE_SEPARATOR_KEY, defaultValueSeparator);
            properties.setProperty(IGNORABLE_SEPARATOR_KEY, defaultIgnorableSeparator);
            properties.putAll(parseProperties(propertiesAsString));
            this.propertiesAsString = propertiesAsString;
        }

        private Map<String, String> parseProperties(String propertiesAsString) {
            Map<String, String> result = new LinkedHashMap<>();
            if (!isEmpty(propertiesAsString)) {
                for (String propertyAsString : propertiesAsString.split("(?<!\\\\),")) {
                    String[] property = StringUtils.split(propertyAsString, "=", 2);
                    result.put(property[0].trim(), StringUtils.replace(property[1], "\\,", ",").trim());
                 }
            }
            return result;
        }

        public String getRowSeparator() {
            return ROW_SEPARATOR;
        }

        public String getHeaderSeparator() {
            return properties.getProperty(HEADER_SEPARATOR_KEY);
        }

        public String getValueSeparator() {
            return properties.getProperty(VALUE_SEPARATOR_KEY);
        }

        public String getIgnorableSeparator() {
            return properties.getProperty(IGNORABLE_SEPARATOR_KEY);
        }

        public String getCommentSeparator() {
            return properties.getProperty(COMMENT_SEPARATOR_KEY);
        }

        public boolean isTrim() {
            return parseBoolean(properties.getProperty("trim", "true"));
        }

        public boolean isMetaByRow(){
            return parseBoolean(properties.getProperty("metaByRow", "false"));
        }

        public String getTransformer() {
            return properties.getProperty("transformer");
        }

        public Properties getProperties() {
            return properties;
        }

        public String getPropertiesAsString() {
            return propertiesAsString;
        }
    }

    static final class ExamplesTableData {
        private final String table;
        private final Deque<ExamplesTableProperties> properties;

        ExamplesTableData(String table, Deque<ExamplesTableProperties> properties) {
            this.table = table;
            this.properties = properties;
        }

        String getTable() {
            return table;
        }

        Deque<ExamplesTableProperties> getProperties() {
            return properties;
        }
    }
}
