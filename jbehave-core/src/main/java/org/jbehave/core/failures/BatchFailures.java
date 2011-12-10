package org.jbehave.core.failures;

import java.util.HashMap;

@SuppressWarnings("serial")
public class BatchFailures extends HashMap<String, Throwable> {

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (String name : keySet()) {
            Throwable cause = get(name);
            sb.append("\n");
            sb.append(name);
            sb.append(": ");
            if (cause != null && cause.getMessage() != null) {
                sb.append(cause.getMessage());
            } else if (cause != null) {
                sb.append(cause);
            } else {
                sb.append("N/A");
            }
        }
        return sb.toString();
    }

}