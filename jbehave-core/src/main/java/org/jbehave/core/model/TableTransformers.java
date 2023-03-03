package org.jbehave.core.model;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.steps.ParameterControls;

/**
 * <p>
 * Facade responsible for transforming table string representations. It allows
 * the registration of several {@link TableTransformer} instances by name.
 * </p>
 * <p>
 * Some Transformers are provided out-of-the-box:
 * <ul>
 * <li>{@link TableTransformers.FromLandscape FromLandscape}: registered under
 * name {@link TableTransformers#FROM_LANDSCAPE}</li>
 * <li>{@link TableTransformers.Formatting Formatting}: registered under name
 * {@link TableTransformers#FORMATTING}</li>
 * <li>{@link TableTransformers.Replacing Replacing}: registered under name
 * {@link TableTransformers#REPLACING}</li>
 * </ul>
 * Out-of-the-box transformers that must be registered:
 * <ul>
 * <li>{@link ResolvingSelfReferences ResolvingSelfReferences}
 * </ul>
 * </p>
 */
public class TableTransformers {

    public static final String FROM_LANDSCAPE = "FROM_LANDSCAPE";
    public static final String FORMATTING = "FORMATTING";
    public static final String REPLACING = "REPLACING";

    private final Map<String, TableTransformer> transformers = new HashMap<>();

    public TableTransformers() {
        useTransformer(FROM_LANDSCAPE, new FromLandscape());
        useTransformer(FORMATTING, new Formatting());
        useTransformer(REPLACING, new Replacing());
    }

    public String transform(String transformerName, String tableAsString, TableParsers tableParsers,
            TableProperties properties) {
        TableTransformer tableTransformer = transformers.get(transformerName);
        if (tableTransformer == null) {
            throw new TransformerNotFoundException(transformerName);
        }
        String result = tableTransformer.transform(tableAsString, tableParsers, properties);
        if (result == null) {
            throw new InvalidTransformationResultException(
                    String.format("Table transformation using transformer '%s' resulted in 'null'", transformerName));
        }
        return result;
    }

    public void useTransformer(String name, TableTransformer transformer) {
        transformers.put(name, transformer);
    }

    public interface TableTransformer {
        String transform(String tableAsString, TableParsers tableParsers, TableProperties properties);
    }

    public static class FromLandscape implements TableTransformer {

        @Override
        public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties) {
            Map<String, List<String>> data = new LinkedHashMap<>();
            for (String rowAsString : tableAsString.split(properties.getRowSeparator())) {
                if (ignoreRow(rowAsString, properties.getIgnorableSeparator())) {
                    continue;
                }
                List<String> values = tableParsers.parseRow(rowAsString, false, properties);
                String header = values.get(0);
                List<String> rowValues = new ArrayList<>(values);
                rowValues.remove(0);
                data.put(header, rowValues);
            }

            if (data.values().stream().mapToInt(List::size).distinct().count() != 1) {
                String errorMessage = data.entrySet()
                        .stream()
                        .map(e -> {
                            int numberOfCells = e.getValue().size();
                            StringBuilder rowDescription = new StringBuilder(e.getKey())
                                    .append(" -> ")
                                    .append(numberOfCells)
                                    .append(" cell");
                            if (numberOfCells > 1) {
                                rowDescription.append('s');
                            }
                            return rowDescription.toString();
                        })
                        .collect(joining(", ", "The table rows have unequal numbers of cells: ", ""));
                throw new IllegalArgumentException(errorMessage);
            }

            StringBuilder builder = new StringBuilder();
            builder.append(properties.getHeaderSeparator());
            for (String header : data.keySet()) {
                builder.append(header).append(properties.getHeaderSeparator());
            }
            builder.append(properties.getRowSeparator());
            int numberOfCells = data.values().iterator().next().size();
            for (int c = 0; c < numberOfCells; c++) {
                builder.append(properties.getValueSeparator());
                for (List<String> row : data.values()) {
                    builder.append(row.get(c)).append(properties.getValueSeparator());
                }
                builder.append(properties.getRowSeparator());
            }
            return builder.toString();
        }

        private boolean ignoreRow(String rowAsString, String ignorableSeparator) {
            return rowAsString.startsWith(ignorableSeparator)
                    || rowAsString.length() == 0;
        }

    }

    public static class Formatting implements TableTransformer {

        @Override
        public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties) {
            List<List<String>> data = new ArrayList<>();
            for (String rowAsString : tableAsString.split(properties.getRowSeparator())) {
                if (ignoreRow(rowAsString, properties.getIgnorableSeparator())) {
                    continue;
                }
                data.add(tableParsers.parseRow(rowAsString, rowAsString.contains(properties.getHeaderSeparator()),
                        properties));
            }

            StringBuilder builder = new StringBuilder();
            Map<Integer, Integer> maxWidths = maxWidth(data);
            for (int r = 0; r < data.size(); r++) {
                String formattedRow = formatRow(data.get(r), maxWidths,
                        r == 0 ? properties.getHeaderSeparator() : properties.getValueSeparator());
                builder.append(formattedRow).append(properties.getRowSeparator());
            }
            return builder.toString();
        }

        private boolean ignoreRow(String rowAsString, String ignorableSeparator) {
            return rowAsString.startsWith(ignorableSeparator)
                    || rowAsString.length() == 0;
        }

        private Map<Integer, Integer> maxWidth(List<List<String>> data) {
            Map<Integer, Integer> maxWidths = new HashMap<>();
            for (List<String> row : data) {
                for (int c = 0; c < row.size(); c++) {
                    String cell = row.get(c).trim();
                    Integer width = maxWidths.get(c);
                    int length = cell.length();
                    if (width == null || length > width) {
                        width = length;
                        maxWidths.put(c, width);
                    }
                }
            }

            return maxWidths;
        }

        private String formatRow(List<String> row,
                Map<Integer, Integer> maxWidths, String separator) {
            StringBuilder builder = new StringBuilder();
            builder.append(separator);
            for (int c = 0; c < row.size(); c++) {
                builder.append(formatValue(row.get(c).trim(), maxWidths.get(c)))
                        .append(separator);
            }
            return builder.toString();
        }

        private String formatValue(String value, int width) {
            if (value.length() < width) {
                return value + padding(width - value.length());
            }
            return value;
        }

        private String padding(int size) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                builder.append(' ');
            }
            return builder.toString();
        }

    }

    public static class Replacing implements TableTransformer {

        @Override
        public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties) {
            String replacing = properties.getProperties().getProperty("replacing");
            String replacement = properties.getProperties().getProperty("replacement");
            if (replacing == null || replacement == null) {
                return tableAsString;
            }
            return tableAsString.replace(replacing, replacement);
        }
    }

    public static class ResolvingSelfReferences implements TableTransformer {
        private final ParameterControls parameterControls;
        private final Pattern placeholderPattern;

        public ResolvingSelfReferences(ParameterControls parameterControls) {
            this.parameterControls = parameterControls;
            placeholderPattern = Pattern.compile(
                    parameterControls.nameDelimiterLeft() + "(.*?)" + parameterControls.nameDelimiterRight());
        }

        @Override
        public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties) {
            TableRows rows = tableParsers.parseRows(tableAsString, properties);
            List<String> headers = rows.getHeaders();

            List<List<String>> resolvedRows = getNamedRows(rows.getRows(), headers).stream()
                    .map(this::resolveRow)
                    .collect(Collectors.toList());

            return ExamplesTableStringBuilder.buildExamplesTableString(properties, headers, resolvedRows);
        }

        private List<String> resolveRow(Map<String, String> unresolvedRow) {
            Map<String, String> resolvedRow = new HashMap<>(unresolvedRow.size(), 1);
            return unresolvedRow.keySet().stream()
                    .map(name -> resolveCell(name, resolvedRow, unresolvedRow))
                    .collect(Collectors.toList());
        }

        private String resolveCell(String name, Map<String, String> resolvedRow, Map<String, String> unresolvedRow) {
            return resolveCell(name, new ArrayList<>(), resolvedRow, unresolvedRow);
        }

        private String resolveCell(String name, List<String> resolutionChain, Map<String, String> resolvedRow,
                                   Map<String, String> unresolvedRow) {
            if (resolvedRow.containsKey(name)) {
                return resolvedRow.get(name);
            }
            resolutionChain.add(name);
            String result = unresolvedRow.get(name);
            Matcher matcher = placeholderPattern.matcher(result);
            while (matcher.find()) {
                String nestedName = matcher.group(1);
                Validate.validState(!name.equals(nestedName), "Circular self reference is found in column '%s'", name);
                checkForCircularChainOfReferences(resolutionChain, nestedName);
                if (unresolvedRow.containsKey(nestedName)) {
                    resolveCell(nestedName, resolutionChain, resolvedRow, unresolvedRow);
                }
                result = StringUtils.replace(result,
                        parameterControls.nameDelimiterLeft() + nestedName + parameterControls.nameDelimiterRight(),
                        resolvedRow.get(nestedName));
            }
            resolvedRow.put(name, result);
            return result;
        }

        private void checkForCircularChainOfReferences(List<String> resolutionChain, String name) {
            int index = resolutionChain.indexOf(name);
            if (index >= 0) {
                String delimiter = " -> ";
                String truncatedChain = resolutionChain.stream().skip(index).collect(Collectors.joining(delimiter));
                throw new IllegalStateException(
                        "Circular chain of references is found: " + truncatedChain + delimiter + name);
            }
        }

        private List<Map<String, String>> getNamedRows(List<List<String>> tableRows, List<String> tableHeaders) {
            List<Map<String, String>> namedRows = new ArrayList<>();
            for (List<String> row : tableRows) {
                Map<String, String> namedRow = new LinkedHashMap<>();
                IntStream.range(0, row.size()).forEach(i -> namedRow.put(tableHeaders.get(i), row.get(i)));
                namedRows.add(namedRow);
            }
            return namedRows;
        }
    }

    public static class TransformerNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 3742264745351414296L;

        public TransformerNotFoundException(String transformerName) {
            super(String.format("Table transformer '%s' does not exist", transformerName));
        }
    }

    public static class InvalidTransformationResultException extends RuntimeException {
        private static final long serialVersionUID = -1242448321325167977L;

        public InvalidTransformationResultException(String message) {
            super(message);
        }
    }
}
