package org.jbehave.core.failures;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("serial")
public class BatchFailures extends ConcurrentHashMap<String, Throwable> {

    private final boolean verbose;

    public BatchFailures() {
        this(false);
    }

    public BatchFailures(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String name : keySet()) {
            Throwable failure = get(name);
            sb.append("\n");
            sb.append(name);
            sb.append(": ");
            sb.append(verbose ? stackTraceOf(failure) : failure);
        }
        return sb.toString();
    }

    private String stackTraceOf(Throwable failure) {
        StringWriter writer = new StringWriter();
        failure.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

}
