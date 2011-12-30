package org.jbehave.core.configuration;

import java.io.PrintStream;
import java.lang.annotation.Annotation;

import org.jbehave.core.reporters.Format;

public class PrintStreamAnnotationMonitor extends NullAnnotationMonitor {

    private final PrintStream output;

    public PrintStreamAnnotationMonitor() {
        this(System.out);
    }

    public PrintStreamAnnotationMonitor(PrintStream output) {
        this.output = output;
    }

    public void elementCreationFailed(Class<?> elementClass, Exception cause) {
        Format.println(output, "Element creation failed: " + elementClass);
        cause.printStackTrace(output);
    }

    public void annotationNotFound(Class<? extends Annotation> annotation, Object annotatedInstance) {
        Format.println(output, "Annotation " + annotation + " not found in " + annotatedInstance);
    }

}
