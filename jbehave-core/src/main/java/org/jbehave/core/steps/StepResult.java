package org.jbehave.core.steps;

import org.jbehave.core.errors.PendingError;
import org.jbehave.core.reporters.StoryReporter;

/**
 * Represents the possible step results:
 * <ul>
 * <li>Failed</li>
 * <li>NotPerformed</li>
 * <li>Pending</li>
 * <li>Successful</li>
 * <li>Ignorable</li>
 * </ul>
 */
public abstract class StepResult {

    public static class Failed extends StepResult {

		public Failed(String step, Throwable throwable) {
			super(step, throwable);
		}

		@Override
		public void describeTo(StoryReporter reporter) {
			reporter.failed(parametrisedStep(), throwable);
		}
	}

	public static class NotPerformed extends StepResult {

		public NotPerformed(String step) {
			super(step);
		}

		@Override
		public void describeTo(StoryReporter reporter) {
			reporter.notPerformed(parametrisedStep());
		}
	}
	
	
	public static class Pending extends StepResult {
		public Pending(String step) {
			this(step, new PendingError(step));
		}

		public Pending(String step, PendingError e) {
			super(step, e);
		}

		@Override
		public void describeTo(StoryReporter reporter) {
			reporter.pending(parametrisedStep());
		}
	}

	public static class Successful extends StepResult {

        public Successful(String string) {
			super(string);
		}

		@Override
		public void describeTo(StoryReporter reporter) {
			reporter.successful(parametrisedStep());
		}

	}

    public static class Ignorable extends StepResult {
        public Ignorable(String step) {
            super(step);
        }

        @Override
        public void describeTo(StoryReporter reporter) {
            reporter.ignorable(step);
        }
    }

	protected final String step;
    private String parametrisedStep;
	protected final Throwable throwable;

	public StepResult(String step) {
		this(step, null);
	}

	public StepResult(String step, Throwable throwable) {
		this.step = step;
		this.throwable = throwable;
	}

    public StepResult withParameterValues(String parametrisedStep) {
        this.parametrisedStep = parametrisedStep;
        return this;
    }

    public String parametrisedStep() {
        return parametrisedStep != null ? parametrisedStep : step;
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

	public static StepResult pending(String step, PendingError e) {
		return new Pending(step, e);
	}

	public static StepResult notPerformed(String step) {
		return new NotPerformed(step);
	}

	public static StepResult failed(String step, Throwable e) {
		return new Failed(step, e);
	}

	public abstract void describeTo(StoryReporter reporter);

	public Throwable getThrowable() {
		return throwable;
	}
}
