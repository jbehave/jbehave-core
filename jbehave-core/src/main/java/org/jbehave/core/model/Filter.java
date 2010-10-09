package org.jbehave.core.model;

import java.io.PrintStream;
import java.util.Properties;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * <p>
 * Represents a filter on meta info.
 * <p/>
 * 
 * <pre>
 * </pre>
 */
public class Filter {

    public static final Filter EMPTY = new Filter();
    private final Properties include = new Properties();
    private final Properties exclude = new Properties();
    private final String filterAsString;
    private final FilterMonitor monitor;

    public Filter() {
        this("", new PrintStreamFilterMonitor());
    }

    public Filter(String filterAsString, FilterMonitor monitor) {
        this.filterAsString = filterAsString;
        this.monitor = monitor;
        parse();
    }

    private void parse() {
        include.clear();
        exclude.clear();
        // TODO parse filterAsString
    }

    public boolean allow(Meta meta) {
        boolean allow = true;
        if (!include.isEmpty() && exclude.isEmpty()) {
            allow = match(include, meta);
        } else if (include.isEmpty() && !exclude.isEmpty()) {
            allow = !match(exclude, meta);
        }
        if (!allow) {
            monitor.notAllowed(this, meta);
        }
        return allow;
    }

    private boolean match(Properties properties, Meta meta) {
        for (Object key : properties.keySet()) {
            for (String metaName : meta.getPropertyNames()) {
                if (key.equals(metaName)) {
                    return properties.get(key).equals(meta.getProperty(metaName));
                }
            }
        }
        return false;
    }

    public String asString() {
        return filterAsString;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static interface FilterMonitor {

        void notAllowed(Filter filter, Meta meta);

    }

    public static class PrintStreamFilterMonitor implements FilterMonitor {

        private PrintStream out;

        public PrintStreamFilterMonitor() {
            this(System.out);
        }

        public PrintStreamFilterMonitor(PrintStream out) {
            this.out = out;
        }

        public void notAllowed(Filter filter, Meta meta) {
            out.println(filter + " does not allow "+meta);
        }

    }
}
