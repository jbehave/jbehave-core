package org.jbehave.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario.Outcome;

public class Lifecycle {

    public static final Lifecycle EMPTY = new Lifecycle();

    private Steps before;
    private Steps[] after;
    
    public Lifecycle() {
        this(Steps.EMPTY);
    }

    public Lifecycle(Steps before, Steps... after) {
        this.before = before;
        this.after = after;
    }

    public List<String> getBeforeSteps() {
        return before.steps;
    }

    public List<String> getAfterSteps() {
    	List<String> afterSteps = new ArrayList<String>();
    	for (Steps steps : after) {
			afterSteps.addAll(steps.steps);
		}
        return afterSteps;
    }

    public Set<Outcome> getOutcomes(){
    	Set<Outcome> outcomes = new LinkedHashSet<Outcome>();
    	for ( Steps steps : after ){
    		outcomes.add(steps.outcome);
    	}
    	return outcomes;
    }

    public List<String> getAfterSteps(Outcome outcome) {
    	List<String> afterSteps = new ArrayList<String>();
    	for (Steps steps : after) {
    		if ( outcome.equals(steps.outcome) ){
    			afterSteps.addAll(steps.steps);
    		}
		}
        return afterSteps;
    }

    public boolean isEmpty() {
        return EMPTY == this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static class Steps {
    	
    	public static Steps EMPTY = new Steps(Arrays.<String>asList());
    	
    	private Outcome outcome;
    	private List<String> steps;
    	
		public Steps(List<String> steps) {
			this(null, steps);
		}
		
		public Steps(Outcome outcome, List<String> steps) {
			this.outcome = outcome;
			this.steps = steps;
		}
		
		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
    	
    }
    
}
