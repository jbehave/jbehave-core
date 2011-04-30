package org.jbehave.core.configuration;

import java.io.PrintStream;
import java.lang.annotation.Annotation;

public class PrintStreamAnnotationMonitor implements AnnotationMonitor {

    private final PrintStream output;

    public PrintStreamAnnotationMonitor() {
        this(System.out);
    }

    public PrintStreamAnnotationMonitor(PrintStream output) {
        this.output = output;
    }

    public void elementCreationFailed(Class<?> elementClass, Exception cause) {
        output.println("Element creation failed: " + elementClass);
        cause.printStackTrace(output);
    }

    public void annotationNotFound(Class<? extends Annotation> annotation, Object annotatedInstance) {
        output.println("Annotation " + annotation + " not found in "+annotatedInstance);        
    }

}
