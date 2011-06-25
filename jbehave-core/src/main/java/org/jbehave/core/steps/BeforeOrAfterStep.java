package org.jbehave.core.steps;

import java.lang.reflect.Method;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.annotations.AfterScenario.Outcome;
import org.jbehave.core.steps.StepCollector.Stage;

/**
 * A BeforeOrAfterStep is associated to a Java method annotated with
 * {@link BeforeStory}, {@link AfterStory}, {@link BeforeScenario} or
 * {@link AfterScenario} in a {@link CandidateSteps} instance class. The
 * BeforeOrAfterStep is responsible for the creation of the executable step via
 * the {@link StepCreator}.
 */
public class BeforeOrAfterStep {

    private final Stage stage;
    private final Method method;
    private final StepCreator stepCreator;
    private final Outcome outcome;
    private StepMonitor stepMonitor = new SilentStepMonitor();

    public BeforeOrAfterStep(Stage stage, Method method, Class<?> type, InjectableStepsFactory stepsFactory) {
        this(stage, method, type, stepsFactory, Outcome.ANY);
    }

    public BeforeOrAfterStep(Stage stage, Method method, Class<?> type, InjectableStepsFactory stepsFactory, Outcome outcome) {
        this.stage = stage;
        this.method = method;
        this.outcome = outcome;
        this.stepCreator = new StepCreator(type, stepsFactory, stepMonitor);
    }

    public Stage getStage() {
        return stage;
    }

    public Method getMethod() {
        return method;
    }

    public Step createStep() {
        return stepCreator.createBeforeOrAfterStep(method);
    }

    public Step createStepUponOutcome(boolean failureOccured) {
        return stepCreator.createAfterStepUponOutcome(method, outcome, failureOccured);
    }

    public void useStepMonitor(StepMonitor stepMonitor) {
        this.stepMonitor = stepMonitor;
        this.stepCreator.useStepMonitor(stepMonitor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(stage).append(method).append(outcome)
                .append(stepMonitor).toString();
    }

}
