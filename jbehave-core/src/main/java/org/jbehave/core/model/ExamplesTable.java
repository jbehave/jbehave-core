package org.jbehave.core.model;

import static java.lang.Boolean.parseBoolean;
import static java.util.regex.Pattern.DOTALL;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.Validate.isTrue;
import static org.apache.commons.lang3.Validate.notBlank;

import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.model.TableTransformers.TableTransformer;
import org.jbehave.core.steps.ChainedRow;
import org.jbehave.core.steps.ConvertedParameters;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;
import org.jbehave.core.steps.Parameters;
import org.jbehave.core.steps.Row;

/**
 * Represents a tabular structure that holds rows of example data for parameters named via the column headers:
 * <pre>
 * |header 1|header 2| .... |header n|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |value m1|value m2| .... |value mn|
 * </pre>
 *
 * <p>Different header and value column separators can be specified to replace the default separator "|":
 * <pre>
 * !!header 1!!header 2!! .... !!header n!!
 * !value 11!value 12! .... !value 1n!
 * ...
 * !value m1!value m2| .... !value mn!
 * </pre>
 * </p>
 *
 * <p>Rows starting with an ignorable separator are allowed and ignored:
 * <pre>
 * |header 1|header 2| .... |header n|
 * |-- A commented row --|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |-- Another commented row --|
 * |value m1|value m2| .... |value mn|
 * </pre>
 * </p>
 *
 * <p>Ignorable separator is configurable and defaults to "|--".</p>
 * <p>The separators are also configurable via inlined properties:
 * <pre>
 * {ignorableSeparator=!--,headerSeparator=!,valueSeparator=!}
 * !header 1!header 2! .... !header n!
 * !-- A commented row --!
 * !value 11!value 12! .... !value 1n!
 * ...
 * !-- Another commented row --!
 * !value m1!value m2! .... !value mn!
 * </pre>
 * </p>
 *
 * <p>By default all column values are trimmed. To avoid trimming the values, use the "trim" inlined property:
 * <pre>
 * {trim=false}
 * | header 1 | header 2 | .... | header n |
 * | value 11 | value 12 | .... | value 1n |
 * </pre>
 * </p>
 * 
 * <p>Comments is column values are supported via the "commentSeparator" inlined property:
 * <pre>
 * {commentSeparator=#}
 * | header 1#comment | header 2 | .... | header n |
 * | value 11#comment | value 12 | .... | value 1n |
 * </pre>
 * Comments including the separator are stripped.</p>
 * 
 * <p>The table allows the retrieval of row values as converted parameters. Use {@link #getRowAsParameters(int)} and
 * invoke {@link Parameters#valueAs(String, Type)} specifying the header and the class type of the parameter.</p>
 * 
 * <p>The table allows the transformation of its string representation via the "transformer" inlined property
 * (multiple transformers will be applied in chain-mode):
 * <pre>
 * {transformer=myTransformerName}
 * {transformer=myOtherTransformerName}
 * |header 1|header 2| .... |header n|
 * |value 11|value 12| .... |value 1n|
 * ...
 * |value m1|value m2| .... |value mn|
 * </pre>
 * The transformers need to be registered by name via the
 * {@link TableTransformers#useTransformer(String, TableTransformer)}. A few transformers are already registered by
 * default in {@link TableTransformers}.</p>
 * 
 * <p>The table allow filtering on meta by row via the "metaByRow" inlined property:
 * <pre>
 * {metaByRow=true}
 * | Meta:       | header 1 | .... | header n |
 * | @name=value | value 11 | .... | value 1n |
 * </pre>
 * </p>
 *
 * <p>Once created, the table row can be modified, via the {@link #withRowValues(int, Map)} method, by specifying the
 * map of row values to be changed.</p>
 * 
 * <p>A table can also be created by providing the entire data content, via the {@link #withRows(List)} method.</p>
 *
 * <p>The parsing code assumes that the number of columns for data rows is the same as in the header, if a row has
 * less fields, the remaining are filled with empty values, if it has more, the fields are ignored.</p>
 */
public class ExamplesTable {
    private static final Map<String, String> EMPTY_MAP = Collections.emptyMap();
    private static final String EMPTY_VALUE = "";

    public static final Pattern INLINED_PROPERTIES_PATTERN = Pattern.compile("\\{(.*?[^\\\\])\\}\\s*(.*)", DOTALL);
    public static final ExamplesTable EMPTY = new ImmutableExamplesTable(EMPTY_VALUE);

    private final ParameterConverters parameterConverters;
    private final Row defaults;
    private final TableRows tableRows;
    private final Deque<TableProperties> tablePropertiesQueue = new LinkedList<>();

    private Map<String, String> namedParameters = Collections.emptyMap();
    private ParameterControls parameterControls;

    public ExamplesTable(String tableAsString) {
        this(tableAsString, new TableTransformers());
    }

    private ExamplesTable(String tableAsString, TableTransformers tableTransformers) {
        this(tableAsString, new ParameterConverters(new LoadFromClasspath(), tableTransformers), tableTransformers);
    }

    private ExamplesTable(String tableAsString, ParameterConverters parameterConverters,
            TableTransformers tableTransformers) {
        this(tableAsString, parameterConverters, new TableParsers(new LocalizedKeywords(), parameterConverters),
                tableTransformers);
    }

    private ExamplesTable(String tableAsString, ParameterConverters parameterConverters, TableParsers tableParsers,
            TableTransformers tableTransformers) {
        this(tableParsers.parseProperties(tableAsString), parameterConverters, new ParameterControls(), tableParsers,
                tableTransformers);
    }

    ExamplesTable(TablePropertiesQueue tablePropertiesQueue, ParameterConverters parameterConverters,
            ParameterControls parameterControls, TableParsers tableParsers, TableTransformers tableTransformers) {
        this.parameterConverters = parameterConverters;
        this.parameterControls = parameterControls;
        this.defaults = new ConvertedParameters(EMPTY_MAP, parameterConverters);
        this.tablePropertiesQueue.addAll(tablePropertiesQueue.getProperties());
        String transformedTable = applyTransformers(tableTransformers, tablePropertiesQueue.getTable(), tableParsers);
        this.tableRows = tableParsers.parseRows(transformedTable, lastTableProperties());
    }

    private TableProperties lastTableProperties() {
        return tablePropertiesQueue.getLast();
    }

    private ExamplesTable(ExamplesTable other, Row defaults) {
        this.tableRows = new TableRows(other.tableRows.getHeaders(),
                other.tableRows.getRows());
        this.parameterConverters = other.parameterConverters;
        this.tablePropertiesQueue.addAll(other.tablePropertiesQueue);
        this.defaults = defaults;
    }

    private String applyTransformers(TableTransformers tableTransformers, String tableAsString,
            TableParsers tableParsers) {
        String transformedTable = tableAsString;
        TableProperties previousProperties = null;
        for (TableProperties properties : tablePropertiesQueue) {
            String transformer = properties.getTransformer();
            if (transformer != null) {
                if (previousProperties != null) {
                    properties.overrideSeparatorsFrom(previousProperties);
                }
                transformedTable = tableTransformers.transform(transformer, transformedTable, tableParsers, properties);
            }
            previousProperties = properties;
        }
        return transformedTable;
    }

    public ExamplesTable withDefaults(Parameters defaults) {
        return new ExamplesTable(this, new ChainedRow(defaults, this.defaults));
    }

    public ExamplesTable withNamedParameters(Map<String, String> namedParameters) {
        this.namedParameters = namedParameters;
        return this;
    }

    public ExamplesTable withRowValues(int rowIndex, Map<String, String> values) {
        for (String header : values.keySet()) {
            if (!getHeaders().contains(header)) {
                getHeaders().add(header);
            }
        }
        List<String> row = getRowValues(rowIndex);
        List<String> headers = getHeaders();
        for (int i = 0, headersSize = headers.size(); i < headersSize; i++) {
            String value = values.get(headers.get(i));

            if (i >= row.size()) {
                row.add(Optional.ofNullable(value).orElse(EMPTY_VALUE));
            } else if (value != null) {
                row.set(i, value);
            }
        }
        return this;
    }

    public ExamplesTable withRows(List<Map<String, String>> rows) {
        tableRows.clear();
        if (!rows.isEmpty()) {
            getHeaders().addAll(rows.get(0).keySet());
            rows.stream().map(Map::values).map(ArrayList::new).forEach(tableRows.rows::add);
        }
        return this;
    }

    public Properties getProperties() {
        return lastTableProperties().getProperties();
    }

    public String getPropertiesAsString() {
        return lastTableProperties().getPropertiesAsString();
    }

    private String replaceNamedParameters(String text) {
        return parameterControls.replaceAllDelimitedNames(text, namedParameters);
    }

    private List<String> replaceNamedParameters(List<String> values, boolean replaceNamedParameters) {
        return replaceNamedParameters ? values.stream().map(this::replaceNamedParameters).collect(toList()) : values;
    }

    public List<String> getHeaders() {
        return tableRows.getHeaders();
    }

    public Map<String, String> getRow(int rowIndex) {
        return getRow(rowIndex, false);
    }

    public Map<String, String> getRow(int rowIndex, boolean replaceNamedParameters) {
        List<String> values = getRowValues(rowIndex, replaceNamedParameters);

        if (!tableRows.areAllColumnsDistinct()) {
            throw new NonDistinctColumnFound("ExamplesTable contains non-distinct columns");
        }

        Map<String, String> result = new LinkedHashMap<>();
        List<String> headers = getHeaders();
        for (int i = 0, headersSize = headers.size(); i < headersSize; i++) {
            result.put(headers.get(i), i < values.size() ? values.get(i) : EMPTY_VALUE);
        }
        return result;
    }

    public List<String> getRowValues(int rowIndex) {
        return getRowValues(rowIndex, false);
    }

    public List<String> getRowValues(int rowIndex, boolean replaceNamedParameters) {
        return replaceNamedParameters(tableRows.getRow(rowIndex), replaceNamedParameters);
    }

    public Parameters getRowAsParameters(int rowIndex) {
        return getRowAsParameters(rowIndex, false);
    }

    public Parameters getRowAsParameters(int rowIndex, boolean replaceNamedParameters) {
        Map<String, String> row = getRow(rowIndex, replaceNamedParameters);
        return new ConvertedParameters(new ChainedRow(new ConvertedParameters(row, parameterConverters), defaults),
                parameterConverters);
    }

    public int getRowCount() {
        return tableRows.getRows().size();
    }

    public boolean metaByRow() {
        return lastTableProperties().isMetaByRow();
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
        return getRowsAs(type, Collections.emptyMap());
    }

    public <T> List<T> getRowsAs(Class<T> type, Map<String, String> fieldNameMapping) {
        return getRowsAsParameters().stream()
                                    .map(p -> p.mapTo(type, fieldNameMapping))
                                    .collect(toList());
    }

    public List<String> getColumn(String columnName) {
        return getColumn(columnName, false);
    }

    public List<String> getColumn(String columnName, boolean replaceNamedParameters) {
        return replaceNamedParameters(tableRows.getColumn(columnName), replaceNamedParameters);
    }

    public String getHeaderSeparator() {
        return lastTableProperties().getHeaderSeparator();
    }

    public String getValueSeparator() {
        return lastTableProperties().getValueSeparator();
    }

    public String asString() {
        if (tableRows.getRows().isEmpty()) {
            return EMPTY_VALUE;
        }
        StringBuilder sb = new StringBuilder();
        for (TableProperties properties : tablePropertiesQueue) {
            String propertiesAsString = properties.getPropertiesAsString();
            if (!propertiesAsString.isEmpty()) {
                sb.append("{").append(propertiesAsString).append("}").append(lastTableProperties().getRowSeparator());
            }
        }
        List<String> headers = getHeaders();

        headers.forEach(header -> sb.append(getHeaderSeparator()).append(header));

        sb.append(getHeaderSeparator()).append(lastTableProperties().getRowSeparator());
        for (List<String> row : tableRows.getRows()) {
            for (int i = 0, headersSize = headers.size(); i < headersSize; i++) {
                sb.append(getValueSeparator()).append(i < row.size() ? row.get(i) : EMPTY_VALUE);
            }
            sb.append(getValueSeparator()).append(lastTableProperties().getRowSeparator());
        }
        return sb.toString();
    }

    public boolean isEmpty() {
        return getHeaders().isEmpty();
    }

    public void outputTo(PrintStream output) {
        output.print(asString());
    }

    public static ExamplesTable empty() {
        return new ExamplesTable("");
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static final class TableProperties {

        public enum Decorator {
            LOWERCASE, UPPERCASE, VERBATIM, TRIM
        }

        private static final String COMMA = ",";
        private static final String COMMA_REGEX = "\\,";
        private static final String EQUAL = "=";
        private static final String PIPE_REGEX = "\\|";

        private static final String DECORATORS_REGEX = Stream.of(Decorator.values())
                .map(Decorator::name)
                .collect(Collectors.joining("|", "(", ")"));

        private static final Pattern DECORATED_PROPERTY_PATTERN = Pattern.compile(
                "\\s*\\{([^=,\\s]+(\\|" + DECORATORS_REGEX + ")*)}\\s*", Pattern.CASE_INSENSITIVE);

        private static final String HEADER_SEPARATOR_KEY = "headerSeparator";
        private static final String VALUE_SEPARATOR_KEY = "valueSeparator";
        private static final String IGNORABLE_SEPARATOR_KEY = "ignorableSeparator";
        private static final String COMMENT_SEPARATOR_KEY = "commentSeparator";
        private static final String NULL_PLACEHOLDER_KEY = "nullPlaceholder";

        private static final String ROW_SEPARATOR = "\n";

        private final Properties properties = new Properties();
        private final ParameterConverters parameterConverters;
        private final String propertiesAsString;

        public TableProperties(String propertiesAsString, Keywords keywords, ParameterConverters parameterConverters) {
            this.propertiesAsString = propertiesAsString;
            this.parameterConverters = parameterConverters;
            properties.setProperty(HEADER_SEPARATOR_KEY, keywords.examplesTableHeaderSeparator());
            properties.setProperty(VALUE_SEPARATOR_KEY, keywords.examplesTableValueSeparator());
            properties.setProperty(IGNORABLE_SEPARATOR_KEY, keywords.examplesTableIgnorableSeparator());
            properties.putAll(parseProperties(propertiesAsString));
        }

        private Map<String, String> parseProperties(String propertiesAsString) {
            Map<String, String> result = new LinkedHashMap<>();
            if (!StringUtils.isEmpty(propertiesAsString)) {
                for (String propertyAsString : propertiesAsString.split("(?<!\\\\),")) {
                    String[] property = StringUtils.split(propertyAsString, EQUAL, 2);
                    String propertyName = property[0];
                    String propertyValue = property[1];
                    Matcher decoratedPropertyMatcher = DECORATED_PROPERTY_PATTERN.matcher(propertyName);
                    if (decoratedPropertyMatcher.matches()) {
                        String[] propertyWithDecorators = decoratedPropertyMatcher.group(1).split(PIPE_REGEX);
                        propertyName = propertyWithDecorators[0];
                        for (int i = 1; i < propertyWithDecorators.length; i++) {
                            String decorator = propertyWithDecorators[i].toUpperCase();
                            propertyValue = decoratePropertyValue(propertyValue, Decorator.valueOf(decorator));
                        }
                    } else {
                        propertyValue = propertyValue.trim();
                    }
                    result.put(propertyName.trim(), StringUtils.replace(propertyValue, COMMA_REGEX, COMMA));
                }
            }
            return result;
        }

        private String decoratePropertyValue(String value, Decorator decorator) {
            switch (decorator) {
                case VERBATIM:
                    return value;
                case LOWERCASE:
                    return value.toLowerCase();
                case UPPERCASE:
                    return value.toUpperCase();
                case TRIM:
                default:
                    return value.trim();
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T getMandatoryNonBlankProperty(String propertyName, Type type) {
            String propertyValue = properties.getProperty(propertyName);
            isTrue(propertyValue != null, "'%s' is not set in ExamplesTable properties", propertyName);
            notBlank(propertyValue, "ExamplesTable property '%s' is blank", propertyName);
            return (T) parameterConverters.convert(propertyValue, type);
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

        public Optional<String> getNullPlaceholder() {
            return Optional.ofNullable(properties.getProperty(NULL_PLACEHOLDER_KEY));
        }

        void overrideSeparatorsFrom(TableProperties properties) {
            Stream.of(HEADER_SEPARATOR_KEY, VALUE_SEPARATOR_KEY, IGNORABLE_SEPARATOR_KEY, COMMENT_SEPARATOR_KEY)
                  .forEach(key -> {
                      String value = properties.properties.getProperty(key);
                      if (value != null) {
                          this.properties.setProperty(key, value);
                      }
                  });
        }

        public boolean isTrim() {
            return parseBoolean(properties.getProperty("trim", "true"));
        }

        public boolean isMetaByRow() {
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

    static final class TablePropertiesQueue {
        private final String table;
        private final Deque<TableProperties> properties;

        TablePropertiesQueue(String table, Deque<TableProperties> properties) {
            this.table = table;
            this.properties = properties;
        }

        String getTable() {
            return table;
        }

        Deque<TableProperties> getProperties() {
            return properties;
        }
    }

    public static final class TableRows {
        private final List<String> headers;
        private final List<List<String>> rows;
        private final boolean allColumnsDistinct;

        public TableRows(List<String> headers, List<List<String>> rows) {
            this.headers = headers;
            this.rows = rows;
            this.allColumnsDistinct = headers.stream().distinct().count() == headers.size();
        }

        public List<String> getHeaders() {
            return headers;
        }

        public List<List<String>> getRows() {
            return rows;
        }

        private boolean areAllColumnsDistinct() {
            return allColumnsDistinct;
        }

        private void clear() {
            headers.clear();
            rows.clear();
        }

        private List<String> getRow(int rowIndex) {
            if (rowIndex > rows.size() - 1) {
                throw new RowNotFound(rowIndex);
            }
            return rows.get(rowIndex);
        }

        private List<String> getColumn(int columnIndex) {
            return rows.stream().map(row -> row.get(columnIndex)).collect(toList());
        }

        private List<String> getColumn(String columnName) {
            int[] headerIndexes = IntStream.range(0, headers.size()).filter(i -> headers.get(i).equals(columnName))
                    .toArray();
            long headerCount = headerIndexes.length;
            if (headerCount == 0) {
                throw new ColumnNotFound(columnName);
            } else if (headerCount > 1) {
                throw new NonDistinctColumnFound(columnName, headerCount);
            }
            return getColumn(headerIndexes[0]);
        }
    }

    public static class RowNotFound extends RuntimeException {
        static final long serialVersionUID = 6577709350720827070L;

        public RowNotFound(int rowIndex) {
            super(Integer.toString(rowIndex));
        }
    }

    public static class ColumnNotFound extends RuntimeException {
        static final long serialVersionUID = -6008855238823273059L;

        public ColumnNotFound(String columnName) {
            super(String.format("The '%s' column does not exist", columnName));
        }
    }

    public static class NonDistinctColumnFound extends RuntimeException {
        static final long serialVersionUID = -6898791308443992005L;

        public NonDistinctColumnFound(String columnName, long totalColumnCount) {
            super(String.format("There are %d columns with the name '%s'", totalColumnCount, columnName));
        }

        public NonDistinctColumnFound(String message) {
            super(message);
        }
    }
}
