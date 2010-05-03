package org.jbehave.core.errors;

/**
 * PendingErrorStrategy allows to define how pending error are handled. Two
 * standard strategies are provided:
 * <ul>
 * <li>{@link PendingErrorStrategy#PASSING}: passes stories upon pending errors</li>
 * <li>{@link PendingErrorStrategy#FAILING}: fails core upon pending errors</li>
 * </ul>
 */
public interface PendingErrorStrategy extends ErrorStrategy {

    /**
     * Strategy that passes stories upon pending errors
     */
    PendingErrorStrategy PASSING = new PendingErrorStrategy() {
        public void handleError(Throwable throwable) {
        }

        public String toString() {
            return "PASSING";
        }
    };

    /**
     * Strategy that fails core upon pending errors
     */
    PendingErrorStrategy FAILING = new PendingErrorStrategy() {
        public void handleError(Throwable throwable) throws Throwable {
            throw throwable;
        }
        public String toString() {
            return "FAILING";
        }
    };
}
