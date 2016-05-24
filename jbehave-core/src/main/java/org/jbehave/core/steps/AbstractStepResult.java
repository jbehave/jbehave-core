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
 * <li>Silent</li>
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

        public void describeTo(StoryReporter reporter) {
            reporter.notPerformed(parametrisedStep());
        }
    }

    public static class Pending extends AbstractStepResult {
        public Pending(String step) {
            this(step, new PendingStepFound(step));
        }

        public Pending(String step, PendingStepFound e) {
            super(step, Type.PENDING, e);
        }

        public void describeTo(StoryReporter reporter) {
            reporter.pending(parametrisedStep());
        }
    }

    public static class Successful extends AbstractStepResult {

        public Successful(String step) {
            super(Type.SUCCESSFUL, step);
        }

        public Successful(Method method) {
            super(Type.SUCCESSFUL, asString(method));
        }

        public void describeTo(StoryReporter reporter) {
            reporter.successful(parametrisedStep());
        }

    }

    public static class Silent extends Successful {

        public Silent(Method method) {
            super(method);
        }

        public void describeTo(StoryReporter reporter) {
            // do not report
        }
    }

    public static class Ignorable extends AbstractStepResult {
        public Ignorable(String step) {
            super(Type.IGNORABLE, step);
        }

        public void describeTo(StoryReporter reporter) {
            reporter.ignorable(step);
        }
    }

    public static class Skipped extends AbstractStepResult {

        public Skipped() {
            super(Type.SKIPPED, "");
        }

        public void describeTo(StoryReporter reporter) {
            // do not report
        }
    }

    protected final String step;
    protected final Type type;
    protected final UUIDExceptionWrapper throwable;
    private String parametrisedStep;
    private long durationInMillis;
    private long start;
    private long end;

    public AbstractStepResult(Type type, String step) {
        this(step, type, null);
    }

    public AbstractStepResult(String step, Type type, UUIDExceptionWrapper throwable) {
        this.step = step;
		this.type = type;
        this.throwable = throwable;
    }

    public String parametrisedStep() {
        return parametrisedStep != null ? parametrisedStep : step;
    }

    public StepResult withParameterValues(String parametrisedStep) {
        this.parametrisedStep = parametrisedStep;
        return this;
    }

    public long durationInMillis(){
        return durationInMillis;
    }
    
    public StepResult setTimings(Timer timer) {
        this.start = timer.getStart();
        this.end = timer.getEnd();
        this.durationInMillis = timer.getDuration();
        return this;
    }
    
    public UUIDExceptionWrapper getFailure() {
        return throwable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(parametrisedStep()).append(durationInMillis()).toString();
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

    public static StepResult silent(Method method) {
        return new Silent(method);
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
            if (i+1 < types.length) {
                sb.append(",");
            }
        }
        return sb.append(")").toString();
    }



}
