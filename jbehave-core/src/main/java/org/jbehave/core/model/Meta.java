package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.configuration.Keywords;

public class Meta {

    public static final Meta EMPTY = new Meta();

    public static final String BLANK = "";
    public static final String SPACE = " ";

    private final Properties properties;

    public Meta() {
        this(new Properties());
    }

    public Meta(Properties properties) {
        this.properties = properties;
    }

    public Meta(List<String> properties) {
        this.properties = new Properties();
        parse(properties);
    }

    private void parse(List<String> propertiesAsString) {
        for (String propertyAsString : new HashSet<String>(propertiesAsString)) {
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

    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    public String getProperty(String name) {
        String value = properties.getProperty(name);
        if (value == null) {
            return BLANK;
        }
        return value;
    }

    public Meta inheritFrom(Meta meta) {       
        return inherit(this, meta);
    }

    private Meta inherit(Meta child, Meta parent) {
        Set<String> names = new HashSet<String>(child.getPropertyNames());
        // only names that are not already present in the child are added
        names.addAll(parent.getPropertyNames());
        Properties inherited = new Properties();
        for (String name : names) {
            if (child.hasProperty(name)) {
                inherited.put(name, child.getProperty(name));
            } else { // if not in child, must be in parent
                inherited.put(name, parent.getProperty(name));
            }
        }
        return new Meta(inherited);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }
    
	public String asString(Keywords keywords) {
		StringBuffer sb = new StringBuffer();
		for (String name : getPropertyNames()) {
			sb.append(keywords.metaProperty()).append(name).append(SPACE)
					.append(getProperty(name)).append(SPACE);
		}
		return sb.toString().trim();
	}

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class Property {

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

    public static Meta createMeta(String meta, Keywords keywords) {
        List<String> properties = new ArrayList<String>();
        for (String property : meta.split(keywords.metaProperty())) {
            if (StringUtils.isNotBlank(property)) {
                String beforeIgnorable = StringUtils.substringBefore(property,keywords.ignorable());
                if ( StringUtils.isNotBlank(beforeIgnorable)){
                    properties.add(beforeIgnorable);
                }
            }
        }
        return new Meta(properties);
    }

}
