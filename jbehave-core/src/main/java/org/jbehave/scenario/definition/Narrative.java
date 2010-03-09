package org.jbehave.scenario.definition;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Narrative {

    public static final Narrative EMPTY = new Narrative("", "", "");

    private final String inOrderTo;
    private final String asA;
    private final String iWantTo;

    public Narrative(String inOrderTo, String asA, String iWantTo) {
        this.inOrderTo = inOrderTo;
        this.asA = asA;
        this.iWantTo = iWantTo;
    }

    public String inOrderTo() {
        return inOrderTo;
    }

    public String asA() {
        return asA;
    }

    public String iWantTo() {
        return iWantTo;
    }
    
    public boolean isEmpty() {
        return EMPTY == this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }


}
