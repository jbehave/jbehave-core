package org.jbehave.core.steps;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ParameterControls {

    public static final String DEFAULT_NAME_DELIMITER_LEFT = "<";
    public static final String DEFAULT_NAME_DELIMITER_RIGHT = ">";
    private String nameDelimiterLeft;
    private String nameDelimiterRight;

    public ParameterControls() {
        this(DEFAULT_NAME_DELIMITER_LEFT, DEFAULT_NAME_DELIMITER_RIGHT);
    }

    public ParameterControls(String nameDelimiterLeft, String nameDelimiterRight) {
        this.nameDelimiterLeft = nameDelimiterLeft;
        this.nameDelimiterRight = nameDelimiterRight;
    }

    public String nameDelimiterLeft() {
        return nameDelimiterLeft;
    }

    public String nameDelimiterRight() {
        return nameDelimiterRight;
    }
    
    public ParameterControls useNameDelimiterLeft(String nameDelimiterLeft) {
        this.nameDelimiterLeft = nameDelimiterLeft;
        return this;
    }

    public ParameterControls useNameDelimiterRight(String nameDelimiterRight) {
        this.nameDelimiterRight = nameDelimiterRight;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }    

}
