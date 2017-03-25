package org.jbehave.core.model;

import static java.lang.Boolean.parseBoolean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

public final class ExamplesTableProperties
{
    private static final String HEADER_SEPARATOR_KEY = "headerSeparator";
    private static final String VALUE_SEPARATOR_KEY = "valueSeparator";
    private static final String IGNORABLE_SEPARATOR_KEY = "ignorableSeparator";
    private static final String COMMENT_SEPARATOR_KEY = "commentSeparator";

    private static final String ROW_SEPARATOR = "\n";

    private final Properties properties = new Properties();

    public ExamplesTableProperties(Properties properties){
        this.properties.putAll(properties);
    }

    public ExamplesTableProperties(String propertiesAsString, String defaultHeaderSeparator, String defaultValueSeparator,
            String defaultIgnorableSeparator) {
        properties.setProperty(HEADER_SEPARATOR_KEY, defaultHeaderSeparator);
        properties.setProperty(VALUE_SEPARATOR_KEY, defaultValueSeparator);
        properties.setProperty(IGNORABLE_SEPARATOR_KEY, defaultIgnorableSeparator);
        try {
            properties.load(new ByteArrayInputStream(propertiesAsString.replace(",", System.lineSeparator()).getBytes()));
        } catch (IOException e) {
            // carry on
        }
    }

    String getRowSeparator() {
        return ROW_SEPARATOR;
    }

    String getHeaderSeparator() {
        return properties.getProperty(HEADER_SEPARATOR_KEY);
    }

    String getValueSeparator() {
        return properties.getProperty(VALUE_SEPARATOR_KEY);
    }

    String getIgnorableSeparator() {
        return properties.getProperty(IGNORABLE_SEPARATOR_KEY);
    }

    String getCommentSeparator() {
        return properties.getProperty(COMMENT_SEPARATOR_KEY);
    }

    boolean isTrim() {
        return parseBoolean(properties.getProperty("trim", "true"));
    }

    boolean isMetaByRow(){
        return parseBoolean(properties.getProperty("metaByRow", "false"));
    }

    String getTransformer() {
        return properties.getProperty("transformer");
    }

    Properties getProperties() {
        return properties;
    }
}
