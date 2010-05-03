package org.jbehave.core.steps;

import org.jbehave.core.errors.PendingError;
import org.jbehave.core.reporters.StoryReporter;

/**
 * Represents a collection of possible step results:
 * <ul>
 * <li>Failed</li>
 * <li>NotPerformed</li>
 * <li>Pending</li>
 * <li>Success</li>
 * </ul>
 */
public abstract class StepResult {

    public static class Failed extends StepResult {

		public Failed(String step, Throwable throwable) {
			super(step, throwable);
		}

		@Override
		public void describeTo(StoryReporter reporter) {
			reporter.failed(step, throwable);
		}
	}

	public static class NotPerformed extends StepResult {

		public NotPerformed(String step) {
			super(step);
		}

		@Override
		public void describeTo(StoryReporter reporter) {
			reporter.notPerformed(step);
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

	public static class Pending extends StepResult {
		public Pending(String step) {
			this(step, new PendingError(step));
		}

		public Pending(String step, PendingError e) {
			super(step, e);
		}

		@Override
		public void describeTo(StoryReporter reporter) {
			reporter.pending(step);
		}
	}

	public static class Success extends StepResult {

        public Success(String string) {
			super(string);
		}

		@Override
		public void describeTo(StoryReporter reporter) {
			reporter.successful(getTranslatedText() != null ? getTranslatedText() : step);
		}

	}

	protected final String step;
    private String translatedText;
	protected final Throwable throwable;

	public StepResult(String step) {
		this(step, null);
	}

	public StepResult(String step, Throwable throwable) {
		this.step = step;
		this.throwable = throwable;
	}

    public StepResult withTranslatedText(String translatedText) {
        this.translatedText = translatedText;
        return this;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public static StepResult success(String step) {
		return new Success(step);
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

	public static StepResult failure(String step, Throwable e) {
		return new Failed(step, e);
	}

	public abstract void describeTo(StoryReporter reporter);

	public Throwable getThrowable() {
		return throwable;
	}
}
