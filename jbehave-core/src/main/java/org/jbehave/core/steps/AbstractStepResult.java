package org.jbehave.core.steps;

import org.jbehave.core.failures.PendingStepFound;
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
 * </ul>
 */
public abstract class AbstractStepResult implements StepResult {

    public static class Failed extends AbstractStepResult {

		public Failed(String step, Throwable throwable) {
			super(step, throwable);
		}

		public void describeTo(StoryReporter reporter) {
			if ( throwable instanceof OutcomesFailed ){
				reporter.failedOutcomes(parametrisedStep(), ((OutcomesFailed)throwable).outcomesTable());
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
    
    public static class SkippedStepResult extends AbstractStepResult {

    	public SkippedStepResult() {
            super("");
        }

        public void describeTo(StoryReporter reporter) {
        }
    }

	protected final String step;
    private String parametrisedStep;
	protected final Throwable throwable;

	public AbstractStepResult(String step) {
		this(step, null);
	}

	public AbstractStepResult(String step, Throwable throwable) {
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

	public Throwable getFailure() {
		return throwable;
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

	public static StepResult failed(String step, Throwable e) {
		return new Failed(step, e);
	}
	
	public static StepResult skipped(){
		return new SkippedStepResult();
	}

}
