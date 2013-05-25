package org.jbehave.core.model;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Lifecycle {

    public static final Lifecycle EMPTY = new Lifecycle();

    private List<String> beforeSteps;
    private List<String> afterSteps;

    public Lifecycle() {
        this(Arrays.<String>asList(), Arrays.<String>asList());
    }

    public Lifecycle(List<String> beforeSteps, List<String> afterSteps) {
        this.beforeSteps = beforeSteps;
        this.afterSteps = afterSteps;
    }

    public List<String> getBeforeSteps() {
        return beforeSteps;
    }

    public List<String> getAfterSteps() {
        return afterSteps;
    }

    public boolean isEmpty() {
        return EMPTY == this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
