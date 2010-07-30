package org.jbehave.core.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Description {

    public static final Description EMPTY = new Description("");

    private final String descriptionAsString;

    public Description(String descriptionAsString) {
        this.descriptionAsString = descriptionAsString;
    }

    public String asString() {
        return descriptionAsString;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
