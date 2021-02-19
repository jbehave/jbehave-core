package org.jbehave.core.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.Scope;
import org.jbehave.core.embedder.MetaFilter;

import java.util.*;

import static org.codehaus.plexus.util.StringUtils.isNotBlank;

public class Lifecycle {

    public static final Lifecycle EMPTY = new Lifecycle();

    private ExamplesTable examplesTable;
    private List<Steps> before;
    private List<Steps> after;
    
    public Lifecycle() {
        this(Arrays.<Steps>asList(), Arrays.<Steps>asList());
    }

    public Lifecycle(ExamplesTable examplesTable) {
        this(examplesTable, Arrays.<Steps>asList(), Arrays.<Steps>asList());
    }

    public Lifecycle(List<Steps> before, List<Steps> after) {
        this(ExamplesTable.EMPTY, before, after);
    }

    public Lifecycle(ExamplesTable examplesTable, List<Steps> before, List<Steps> after) {
        this.examplesTable = examplesTable;
        this.before = before;
        this.after = after;
    }

    public Set<Scope> getScopes() {
        Set<Scope> scopes = new LinkedHashSet<>();
        scopes.add(Scope.STEP);
        scopes.add(Scope.SCENARIO);
        scopes.add(Scope.STORY);
        return scopes;
    }

    public ExamplesTable getExamplesTable() {
        return examplesTable;
    }

    public boolean hasBeforeSteps() {
        return !unwrap(before).isEmpty();
    }

    public List<String> getBeforeSteps() {
        return getBeforeSteps(Scope.SCENARIO);
    }

    public List<String> getBeforeSteps(Scope scope) {
        return unwrap(filter(this.before, scope));
    }

    public List<Steps> getBefore() {
        return before;
    }

    public boolean hasAfterSteps() {
        return !unwrap(this.after).isEmpty();
    }

    public List<String> getAfterSteps() {
        return getAfterSteps(Scope.SCENARIO);
    }

    public List<String> getAfterSteps(Scope scope) {
        return unwrap(filter(this.after, scope));
    }

    public List<Steps> getAfter() {
        return after;
    }

    public Set<Outcome> getOutcomes(){
        Set<Outcome> outcomes = new LinkedHashSet<>();
        for (Steps steps : after) {
            outcomes.add(steps.outcome);
        }
        return outcomes;
    }

    public MetaFilter getMetaFilter(Outcome outcome){
        for (Steps steps : after) {
            if (outcome.equals(steps.outcome) && isNotBlank(steps.metaFilter)) {
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

    public List<String> getAfterSteps(Scope scope, Outcome outcome) {
        return getAfterSteps(scope, outcome, Meta.EMPTY);
    }

    public List<String> getAfterSteps(Scope scope, Outcome outcome, Meta meta) {
        MetaFilter filter = getMetaFilter(outcome);
        List<Steps> afterSteps = new ArrayList<>();
        for (Steps steps : after) {
            if (outcome == steps.outcome && (meta.equals(Meta.EMPTY) || !filter.excluded(meta))) {
                afterSteps.add(stepsByScope(steps, scope));
            }
        }
        return unwrap(afterSteps);
    }

    private List<String> unwrap(List<Steps> stepsCollection) {
        List<String> allSteps = new ArrayList<>();
        for (Steps steps : stepsCollection) {
            allSteps.addAll(steps.steps);
        }
        return allSteps;
    }

    private List<Steps> filter(List<Steps> stepsCollection, Scope scope) {
        List<Steps> filteredSteps = new ArrayList<>();
        for (Steps steps : stepsCollection) {
            filteredSteps.add(stepsByScope(steps, scope));
        }
        return filteredSteps;
    }

    private Steps stepsByScope(Steps steps, Scope scope) {
        return steps.scope == scope ? steps : Steps.EMPTY;
    }

    public boolean isEmpty() {
        return examplesTable.isEmpty() && before.isEmpty() && after.isEmpty();
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
            this(scope, Outcome.ANY, null, steps);
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
