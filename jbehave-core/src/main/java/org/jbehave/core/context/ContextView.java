package org.jbehave.core.context;

/**
 * ContextView allows the viewing of context-related messages.
 */
public interface ContextView {

    void show(String story, String scenario, String step);
    
    void close();
    
    public static class NULL implements ContextView {
        @Override
        public void show(String story, String scenario, String step) {
        }

        @Override
        public void close() {
        }
    }

}
