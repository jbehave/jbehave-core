package org.jbehave.core.steps;

import java.lang.reflect.Method;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.model.Meta;

/**
 * A BeforeOrAfterStep is associated to a Java method annotated with
 * {@link BeforeStory}, {@link AfterStory}, {@link BeforeScenario} or
 * {@link AfterScenario} in a {@link CandidateSteps} instance class. The
 * BeforeOrAfterStep is responsible for the creation of the executable step via
 * the {@link StepCreator}.
 */
public class BeforeOrAfterStep {

    private final Method method;
    private final int order;
    private final StepCreator stepCreator;
    private final Outcome outcome;
    private StepMonitor stepMonitor = new SilentStepMonitor();

    public BeforeOrAfterStep(Method method, int order, StepCreator stepCreator) {
        this(method, order, Outcome.ANY, stepCreator);
    }

    public BeforeOrAfterStep(Method method, int order, Outcome outcome, StepCreator stepCreator) {
        this.method = method;
        this.order = order;
        this.outcome = outcome;
        this.stepCreator = stepCreator;
    }

    public Method getMethod() {
        return method;
    }

    public int getOrder() {
        return order;
    }

    public Step createStep() {
        return createStepWith(Meta.EMPTY);
    }

    public Step createStepWith(Meta meta) {
        return stepCreator.createBeforeOrAfterStep(method, meta);
    }

    public Step createStepUponOutcome(Meta storyAndScenarioMeta) {
        return stepCreator.createAfterStepUponOutcome(method, outcome, storyAndScenarioMeta);
    }

    public void useStepMonitor(StepMonitor stepMonitor) {
        this.stepMonitor = stepMonitor;
        this.stepCreator.useStepMonitor(stepMonitor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(method).append(order).append(outcome)
                .append(stepMonitor).toString();
    }
}
