package org.jbehave.core.steps;

import java.lang.reflect.Method;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jbehave.core.failures.PendingStepFound;
import org.jbehave.core.failures.UUIDExceptionWrapper;
import org.jbehave.core.model.OutcomesTable.OutcomesFailed;
import org.jbehave.core.reporters.StoryReporter;

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
            super(step, Type.FAILED, throwable);
        }

        public Failed(Method method, UUIDExceptionWrapper throwable) {
            super(asString(method), Type.FAILED, throwable);
        }

        @Override
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
            super(Type.NOT_PERFORMED, step);
        }

        @Override
        public void describeTo(StoryReporter reporter) {
            reporter.notPerformed(parametrisedStep());
        }
    }

    public static class Pending extends AbstractStepResult {
        private StepCreator.PendingStep pendingStep;

        public Pending(StepCreator.PendingStep step) {
            this(step, new PendingStepFound(step.stepAsString()));
            pendingStep = step;
        }

        public Pending(StepCreator.PendingStep step, PendingStepFound e) {
            super(step.getStepAsString(), Type.PENDING, e);
            pendingStep = step;
        }

        @Override
        public void describeTo(StoryReporter reporter) {
            reporter.pending(pendingStep);
        }
    }

    public static class Successful extends AbstractStepResult {

        public Successful(String step) {
            super(Type.SUCCESSFUL, step);
        }

        public Successful(Method method) {
            super(Type.SUCCESSFUL, asString(method));
        }

        @Override
        public void describeTo(StoryReporter reporter) {
            reporter.successful(parametrisedStep());
        }

    }

    public static class Ignorable extends AbstractStepResult {
        public Ignorable(String step) {
            super(Type.IGNORABLE, step);
        }

        @Override
        public void describeTo(StoryReporter reporter) {
            reporter.ignorable(step);
        }
    }

    public static class Comment extends AbstractStepResult {
        public Comment(String step) {
            super(Type.COMMENT, step);
        }

        @Override
        public void describeTo(StoryReporter reporter) {
            reporter.comment(step);
        }
    }

    public static class Skipped extends AbstractStepResult {

        public Skipped() {
            super(Type.SKIPPED, "");
        }

        @Override
        public void describeTo(StoryReporter reporter) {
            // do not report
        }
    }

    protected final String step;
    protected final Type type;
    protected final UUIDExceptionWrapper throwable;
    private Timing timing = new Timing();
    private String parametrisedStep;

    public AbstractStepResult(Type type, String step) {
        this(step, type, null);
    }

    public AbstractStepResult(String step, Type type, UUIDExceptionWrapper throwable) {
        this.step = step;
        this.type = type;
        this.throwable = throwable;
    }

    @Override
    public String parametrisedStep() {
        return parametrisedStep != null ? parametrisedStep : step;
    }

    @Override
    public StepResult withParameterValues(String parametrisedStep) {
        this.parametrisedStep = parametrisedStep;
        return this;
    }

    public Timing getTiming() {
        return timing;
    }
    
    @Override
    public StepResult setTimings(Timer timer) {
        this.timing = new Timing(timer);
        return this;
    }
    
    @Override
    public UUIDExceptionWrapper getFailure() {
        return throwable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(parametrisedStep()).append(
                getTiming()).toString();
    }

    public static StepResult successful(String step) {
        return new Successful(step);
    }

    public static StepResult successful(Method method) {
        return new Successful(method);
    }

    public static StepResult ignorable(String step) {
        return new Ignorable(step);
    }

    public static StepResult comment(String step) {
        return new Comment(step);
    }

    public static StepResult pending(StepCreator.PendingStep step) {
        return new Pending(step);
    }

    public static StepResult pending(StepCreator.PendingStep step, PendingStepFound e) {
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
    
    private static String asString(Method method) {
        if (method == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder()
                .append(method.getDeclaringClass().getName()).append(".")
                .append(method.getName()).append("(");
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            sb.append(type.getName());
            if (i + 1 < types.length) {
                sb.append(",");
            }
        }
        return sb.append(")").toString();
    }
}
