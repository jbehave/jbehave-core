package org.jbehave.core.steps;

/**
 * ContextView allows the viewing of context-related messages.
 */
public interface ContextView {

    void show(String scenario, String step);
    
    void close();
    
    public static class NULL implements ContextView {
        public void show(String scenario, String step) {
        }

        public void close() {
        }
    }

}
