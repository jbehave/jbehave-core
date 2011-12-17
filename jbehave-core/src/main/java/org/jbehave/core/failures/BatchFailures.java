package org.jbehave.core.failures;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

@SuppressWarnings("serial")
public class BatchFailures extends HashMap<String, Throwable> {

    private final boolean verbose;

    public BatchFailures(){
        this(false);
    }

    public BatchFailures(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String name : keySet()) {
            Throwable failure = get(name);
            sb.append("\n");
            sb.append(name);
            sb.append(": ");
            sb.append( verbose ? stackTraceOf(failure) : failure);
        }
        return sb.toString();
    }

    private String stackTraceOf(Throwable failure) {
        StringWriter writer = new StringWriter();
        failure.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

}