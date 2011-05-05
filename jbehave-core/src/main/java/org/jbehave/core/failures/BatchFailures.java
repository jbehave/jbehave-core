package org.jbehave.core.failures;

import java.util.HashMap;

@SuppressWarnings("serial")
public class BatchFailures extends HashMap<String,Throwable> {

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String name : keySet()) {
            Throwable cause = get(name);
            sb.append("\n");
            sb.append(name);
            sb.append(": ");
            sb.append(cause.getMessage() != null ? cause.getMessage() : "N/A");
        }
        return sb.toString();
    }

}