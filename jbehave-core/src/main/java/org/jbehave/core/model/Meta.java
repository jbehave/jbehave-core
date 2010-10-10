package org.jbehave.core.model;

import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Meta {

    public static final Meta EMPTY = new Meta();

    private static final String BLANK = "";

    private final Properties properties;

    public Meta() {
        this(new Properties());
    }

    public Meta(Properties properties) {
        this.properties = properties;
    }

    public Meta(Set<String> properties) {
        this.properties = new Properties();
        parse(properties);
    }

    private void parse(Set<String> propertiesAsString) {
        for (String propertyAsString : propertiesAsString) {
            Property property = new Property(propertyAsString);
            this.properties.setProperty(property.getName(), property.getValue());
        }
    }

    public Set<String> getPropertyNames() {
        Set<String> names = new TreeSet<String>();
        for (Object key : properties.keySet()) {
            names.add((String) key);
        }
        return names;
    }

    public String getProperty(String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            return BLANK;
        }
        return value;
    }

    public boolean isEmpty() {
        return EMPTY == this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class Property {

        private static final String SPACE = " ";

        private String propertyAsString;
        private String name;
        private String value;

        public Property(String propertyAsString) {
            this.propertyAsString = propertyAsString.trim();
            parse();
        }

        private void parse() {
            name = StringUtils.substringBefore(propertyAsString, SPACE).trim();
            value = StringUtils.substringAfter(propertyAsString, SPACE).trim();
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

    }
}
