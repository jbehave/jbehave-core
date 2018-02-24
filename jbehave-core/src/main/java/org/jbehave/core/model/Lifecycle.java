package org.jbehave.core.model;

import static org.codehaus.plexus.util.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.embedder.MetaFilter;

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
        return getBeforeSteps(Scope.SCENARIO);
    }

    public List<String> getBeforeSteps(Scope scope) {
        return stepsByScope(before, scope);
    }

    public List<String> getAfterSteps() {
        return getAfterSteps(Scope.SCENARIO);
    }

    public List<String> getAfterSteps(Scope scope) {
        List<String> afterSteps = new ArrayList<String>();
        for (Steps steps : after) {
            afterSteps.addAll(stepsByScope(steps, scope));
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

    public MetaFilter getMetaFilter(Outcome outcome){
    	for ( Steps steps : after ){
			if ( outcome.equals(steps.outcome) && isNotBlank(steps.metaFilter) ){
    			return new MetaFilter(steps.metaFilter);
    		}
    	}
    	return MetaFilter.EMPTY;
    }

	public List<String> getAfterSteps(Outcome outcome) {
		return getAfterSteps(outcome, Meta.EMPTY);
    }

	public List<String> getAfterSteps(Outcome outcome, Meta meta) {
        return getAfterSteps(Scope.SCENARIO, outcome, meta);
    }

    public List<String> getAfterSteps(Scope scope, Outcome outcome, Meta meta) {
        MetaFilter filter = getMetaFilter(outcome);
        List<String> afterSteps = new ArrayList<String>();
        for (Steps steps : after) {
            if ( outcome.equals(steps.outcome) ) {
                if ( meta.equals(Meta.EMPTY) ){
                    afterSteps.addAll(stepsByScope(steps, scope));
                } else {
                    if ( filter.allow(meta) ){
                        afterSteps.addAll(stepsByScope(steps, scope));
                    }
                }
            }
        }
        return afterSteps;
    }

    private List<String> stepsByScope(Steps steps, Scope scope) {
        if ( steps.scope  == Scope.SCENARIO ) {
            return steps.steps;
        }
        if ( steps.scope == scope ) {
            return steps.steps;
        }
        return Steps.EMPTY.steps;
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

        private Scope scope;
    	private Outcome outcome;
		private String metaFilter;
    	private List<String> steps;
    	
		public Steps(List<String> steps) {
			this(Scope.SCENARIO, steps);
		}

        public Steps(Scope scope, List<String> steps) {
            this(scope, null, null, steps);
        }

        public Steps(Outcome outcome, List<String> steps) {
			this(outcome, null, steps);
		}

		public Steps(Outcome outcome, String metaFilter, List<String> steps) {
		    this(Scope.SCENARIO, outcome, metaFilter, steps);
		}

        public Steps(Scope scope, Outcome outcome, List<String> steps) {
            this(scope, outcome, null, steps);
        }

        public Steps(Scope scope, Outcome outcome, String metaFilter, List<String> steps) {
		    this.scope = scope;
			this.outcome = outcome;
			this.metaFilter = metaFilter;
			this.steps = steps;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
    	
    }
    
}
