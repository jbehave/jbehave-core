package org.jbehave.core.steps;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.reporters.StoryReporter;

import java.lang.reflect.Method;

/**
 * Represents the possible step results:
 * <ul>
 * <li>Failed</li>
 * <li>NotPerformed</li>
 * <li>Pending</li>
 * <li>Successful</li>
 * <li>Ignorable</li>
 * <li>Skipped</li>
 * </ul>
 */
public abstract class AbstractStepResult implements StepResult {

    public static class Failed extends AbstractStepResult {

        public Failed(String step, UUIDExceptionWrapper throwable) {
            super(step, throwable);
        }

        public Failed(Method method, UUIDExceptionWrapper throwable) {
            super(asString(method), throwable);
        }

        private static String asString(Method method) {
            return method != null ? method.toGenericString(): "";
        }

        public void describeTo(StoryReporter reporter) {
            if (throwable.getCause() instanceof OutcomesFailed) {
                reporter.failedOutcomes(parametrisedStep(), ((OutcomesFailed) throwable.getCause()).outcomesTable());
            } else {
                reporter.failed(parametrisedStep(), throwable);
            }
        }
    }

    public static class NotPerformed extends AbstractStepResult {

        public NotPerformed(String step) {
            super(step);
        }

        public void describeTo(StoryReporter reporter) {
            reporter.notPerformed(parametrisedStep());
        }
    }

    public static class Pending extends AbstractStepResult {
        public Pending(String step) {
            this(step, new PendingStepFound(step));
        }

        public Pending(String step, PendingStepFound e) {
            super(step, e);
        }

        public void describeTo(StoryReporter reporter) {
            reporter.pending(parametrisedStep());
        }
    }

    public static class Successful extends AbstractStepResult {

        public Successful(String string) {
            super(string);
        }

        public void describeTo(StoryReporter reporter) {
            reporter.successful(parametrisedStep());
        }

    }

    public static class Ignorable extends AbstractStepResult {
        public Ignorable(String step) {
            super(step);
        }

        public void describeTo(StoryReporter reporter) {
            reporter.ignorable(step);
        }
    }

    public static class Skipped extends AbstractStepResult {

        public Skipped() {
            super("");
        }

        public void describeTo(StoryReporter reporter) {
        }
    }

    protected final String step;
    private String parametrisedStep;
    protected final UUIDExceptionWrapper throwable;

    public AbstractStepResult(String step) {
        this(step, null);
    }

    public AbstractStepResult(String step, UUIDExceptionWrapper throwable) {
        this.step = step;
        this.throwable = throwable;
    }

    public String parametrisedStep() {
        return parametrisedStep != null ? parametrisedStep : step;
    }

    public StepResult withParameterValues(String parametrisedStep) {
        this.parametrisedStep = parametrisedStep;
        return this;
    }

    public UUIDExceptionWrapper getFailure() {
        return throwable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(parametrisedStep()).toString();
    }

    public static StepResult successful(String step) {
        return new Successful(step);
    }

    public static StepResult ignorable(String step) {
        return new Ignorable(step);
    }

    public static StepResult pending(String step) {
        return new Pending(step);
    }

    public static StepResult pending(String step, PendingStepFound e) {
        return new Pending(step, e);
    }

    public static StepResult notPerformed(String step) {
        return new NotPerformed(step);
    }

    public static StepResult failed(String step, UUIDExceptionWrapper e) {
        return new Failed(step, e);
    }

    public static StepResult failed(Method method, UUIDExceptionWrapper e) {
        return new Failed(method, e);
    }

    public static StepResult skipped() {
        return new Skipped();
    }

}
