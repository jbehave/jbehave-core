package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TablePropertiesQueue;
import org.jbehave.core.model.ExamplesTable.TableRows;
import org.jbehave.core.steps.ParameterConverters;

public class TableParsers {

    private static final String ROW_SEPARATOR_PATTERN = "\r?\n";

    private final Keywords keywords;
    private final ParameterConverters parameterConverters;
    private final Optional<String> defaultNullPlaceholder;

    public TableParsers(ParameterConverters parameterConverters) {
        this(new LocalizedKeywords(), parameterConverters);
    }

    public TableParsers(Keywords keywords, ParameterConverters parameterConverters) {
        this(keywords, parameterConverters, Optional.empty());
    }

    public TableParsers(Keywords keywords, ParameterConverters parameterConverters,
            Optional<String> defaultNullPlaceholder) {
        this.keywords = keywords;
        this.parameterConverters = parameterConverters;
        this.defaultNullPlaceholder = defaultNullPlaceholder;
    }

    public TablePropertiesQueue parseProperties(String tableAsString) {
        Deque<TableProperties> properties = new LinkedList<>();
        String tableWithoutProperties = tableAsString.trim();
        Matcher matcher = ExamplesTable.INLINED_PROPERTIES_PATTERN.matcher(tableWithoutProperties);
        while (matcher.matches()) {
            String propertiesAsString = matcher.group(1);
            propertiesAsString = StringUtils.replace(propertiesAsString, "\\{", "{");
            propertiesAsString = StringUtils.replace(propertiesAsString, "\\}", "}");
            properties.add(new TableProperties(propertiesAsString, keywords, parameterConverters));
            tableWithoutProperties = matcher.group(2).trim();
            matcher = ExamplesTable.INLINED_PROPERTIES_PATTERN.matcher(tableWithoutProperties);
        }
        if (properties.isEmpty()) {
            properties.add(new TableProperties("", keywords, parameterConverters));
        }
        return new TablePropertiesQueue(tableWithoutProperties, properties);
    }

    public TableRows parseRows(String tableAsString, TableProperties properties) {
        List<String> headers = new ArrayList<>();
        List<List<String>> rows = new ArrayList<>();

        for (String rowLine : tableAsString.split(ROW_SEPARATOR_PATTERN)) {
            String trimmedRowLine = rowLine.trim();
            // skip ignorable or empty lines
            if (!trimmedRowLine.startsWith(properties.getIgnorableSeparator()) && !trimmedRowLine.isEmpty()) {
                if (headers.isEmpty()) {
                    headers.addAll(parseRow(trimmedRowLine, true, properties));
                } else {
                    List<String> cells = parseRow(trimmedRowLine, false, properties);
                    if (cells.size() > headers.size()) {
                        cells = cells.subList(0, headers.size());
                    }
                    rows.add(cells);
                }
            }
        }

        return new TableRows(headers, rows);
    }

    public List<String> parseRow(String rowAsString, boolean header, TableProperties properties) {
        String separator = header ? properties.getHeaderSeparator() : properties.getValueSeparator();
        String commentSeparator = properties.getCommentSeparator();
        Optional<String> nullPlaceholder = properties.getNullPlaceholder().map(Optional::of).orElse(
                defaultNullPlaceholder);
        UnaryOperator<String> trimmer = properties.isTrim() ? String::trim : UnaryOperator.identity();
        String[] cells = StringUtils.splitByWholeSeparatorPreserveAllTokens(rowAsString.trim(), separator);
        List<String> row = new ArrayList<>(cells.length);
        for (int i = 0; i < cells.length; i++) {
            String cell = cells[i];
            cell = StringUtils.substringBefore(cell, commentSeparator);
            if ((i == 0 || i == cells.length - 1) && cell.isEmpty()) {
                continue;
            }
            String trimmedCell = trimmer.apply(cell);
            row.add(nullPlaceholder.filter(trimmedCell::equals).isPresent() ? null : trimmedCell);
        }
        return row;
    }
}
