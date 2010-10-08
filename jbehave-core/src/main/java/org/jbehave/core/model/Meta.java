package org.jbehave.core.model;

import java.util.Properties;

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

    public String getProperty(String name){
        String value = properties.getProperty(name);
        if ( value == null ){
            return BLANK;
        }
        return value;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
