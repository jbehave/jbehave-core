package org.jbehave.core.model;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Narrative {

    public static final Narrative EMPTY = new Narrative("", "", "");

    private final String inOrderTo;
    private final String asA;
    private final String iWantTo;
    private final String soThat;

    public Narrative(String inOrderTo, String asA, String iWantTo) {
        this(inOrderTo, asA, iWantTo, "");
    }

    public Narrative(String inOrderTo, String asA, String iWantTo, String soThat) {
        this.inOrderTo = inOrderTo;
        this.asA = asA;
        this.iWantTo = iWantTo;
        this.soThat = soThat;
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

    public String soThat(){
        return soThat;
    }
    
    public boolean isEmpty() {
        return EMPTY == this;
    }

    public boolean isAlternative(){
        return inOrderTo.isEmpty();
    }
    
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
