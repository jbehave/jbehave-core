package org.jbehave.core.steps;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ParameterControls {

    public static final String DEFAULT_NAME_DELIMITER_LEFT = "<";
    public static final String DEFAULT_NAME_DELIMITER_RIGHT = ">";

    private String nameDelimiterLeft;
    private String nameDelimiterRight;
    private boolean delimiterNamedParameters;
    
    public ParameterControls() {
        this(DEFAULT_NAME_DELIMITER_LEFT, DEFAULT_NAME_DELIMITER_RIGHT, true);
    }

    public ParameterControls(String nameDelimiterLeft, String nameDelimiterRight, boolean delimiterNamedParameters) {
        this.nameDelimiterLeft = nameDelimiterLeft;
        this.nameDelimiterRight = nameDelimiterRight;
        this.delimiterNamedParameters = delimiterNamedParameters;
    }

    public String nameDelimiterLeft() {
        return nameDelimiterLeft;
    }

    public String nameDelimiterRight() {
        return nameDelimiterRight;
    }

    public boolean delimiterNamedParameters() {
        return delimiterNamedParameters;
    }    
    
    public ParameterControls useNameDelimiterLeft(String nameDelimiterLeft) {
        this.nameDelimiterLeft = nameDelimiterLeft;
        return this;
    }

    public ParameterControls useNameDelimiterRight(String nameDelimiterRight) {
        this.nameDelimiterRight = nameDelimiterRight;
        return this;
    }

    public ParameterControls useDelimiterNamedParameters(boolean delimiterNamedParameters) {
        this.delimiterNamedParameters = delimiterNamedParameters;
        return this;
    }

    private String createDelimitedName(String name) {
        return nameDelimiterLeft + name + nameDelimiterRight;
    }

    public String replaceAllDelimitedNames(String text, String name, String value) {
        String delimitedName = createDelimitedName(name);
        if (value != null) {
            return StringUtils.replace(text, delimitedName, value);
        } else if (text.equals(delimitedName)) {
            return null;
        }
        throw new IllegalArgumentException(
                "Unable to replace \"" + delimitedName + "\" in text \"" + text + "\" with \"null\" value");
    }

    public String replaceAllDelimitedNames(String text, Map<String, String> namedParameters) {
        String textWithReplacedDelimitedNames = text;
        for (Entry<String, String> parameter : namedParameters.entrySet()) {
            textWithReplacedDelimitedNames = replaceAllDelimitedNames(textWithReplacedDelimitedNames,
                    parameter.getKey(), parameter.getValue());
        }
        return textWithReplacedDelimitedNames;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
