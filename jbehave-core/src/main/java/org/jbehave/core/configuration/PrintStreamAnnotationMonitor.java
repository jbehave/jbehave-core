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

    @Override
    public void elementCreationFailed(Class<?> elementClass, Exception cause) {
        print("Element creation failed: %s", elementClass);
        printStackTrace(cause);
    }

    @Override
    public void annotationNotFound(Class<? extends Annotation> annotation, Object annotatedInstance) {
        print("Annotation %s not found in %s", annotation, annotatedInstance);
    }

    private void print(String format, Object... args) {
        Format.println(output, format, args);
    }

    private void printStackTrace(Throwable e) {
        e.printStackTrace(output);
    }
}
