package org.jbehave.core.configuration.spring;

import org.jbehave.core.steps.ParameterControls;

/**
 * Extends {@link ParameterControls} to provide getter/setter methods for all
 * control properties, so it can be used by Spring's property mechanism.
 */
public class SpringParameterControls extends ParameterControls {

    public String getNameDelimiterLeft() {
        return nameDelimiterLeft();
    }

    public String getNameDelimiterRight() {
        return nameDelimiterRight();
    }

    public ParameterControls setNameDelimiterLeft(String nameDelimiterLeft) {
        return useNameDelimiterLeft(nameDelimiterLeft);
    }

    public ParameterControls setNameDelimiterRight(String nameDelimiterRight) {
        return useNameDelimiterRight(nameDelimiterRight);
    }

}
