package org.jbehave.core.embedder;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Meta.Property;

/**
 * <p>
 * Allows filtering on meta info.
 * </p>
 * 
 * <p>
 * Filters are represented as a sequence of any name-value properties (separated
 * by a space), prefixed by "+" for inclusion and "-" for exclusion. E.g.:
 * 
 * <pre>
 * new Filter(&quot;+author Mauro -theme smoke testing +map *API -skip&quot;)
 * </pre>
 * 
 * </p>
 */
public class MetaFilter {

    public static final MetaFilter EMPTY = new MetaFilter();

    private final Properties include = new Properties();
    private final Properties exclude = new Properties();
    private final String filterAsString;
    private final EmbedderMonitor monitor;

    public MetaFilter() {
        this("");
    }

    public MetaFilter(String filterAsString) {
        this(filterAsString, new PrintStreamEmbedderMonitor());
    }

    public MetaFilter(String filterAsString, EmbedderMonitor monitor) {
        this.filterAsString = filterAsString;
        this.monitor = monitor;
        parse(include, "+");
        parse(exclude, "-");
    }

    private void parse(Properties properties, String prefix) {
        properties.clear();
        for (String found : found(prefix)) {
            Property property = new Property(StringUtils.removeStartIgnoreCase(found, prefix));
            properties.setProperty(property.getName(), property.getValue());
        }
    }

    private Set<String> found(String prefix) {
        Matcher matcher = findAllPrefixed(prefix).matcher(filterAsString);
        Set<String> found = new HashSet<String>();
        while (matcher.find()) {
            found.add(matcher.group().trim());
        }
        return found;
    }

    private Pattern findAllPrefixed(String prefix) {
        return Pattern.compile("(\\" + prefix + "(\\w|\\s|\\*)*)", Pattern.DOTALL);
    }

    public boolean allow(Meta meta) {
        boolean allowed;
        if (!include.isEmpty() && exclude.isEmpty()) {
            allowed = match(include, meta);
        } else if (include.isEmpty() && !exclude.isEmpty()) {
            allowed = !match(exclude, meta);
        } else if (!include.isEmpty() && !exclude.isEmpty()) {
            allowed = match(merge(include, exclude), meta) && !match(exclude, meta);
        } else {
            allowed = true;
        }
        if (!allowed) {
            monitor.metaNotAllowed(meta, this);
        }
        return allowed;
    }

    private Properties merge(Properties include, Properties exclude) {
        Set<Object> in = new HashSet<Object>(include.keySet());
        in.addAll(exclude.keySet());
        Properties merged = new Properties();
        for (Object key : in) {
            if (include.containsKey(key)) {
                merged.put(key, include.get(key));
            } else if (exclude.containsKey(key)) {
                merged.put(key, exclude.get(key));
            }
        }
        return merged;
    }

    private boolean match(Properties properties, Meta meta) {
        for (Object key : properties.keySet()) {
            String property = (String) properties.get(key);
            for (String metaName : meta.getPropertyNames()) {
                if (key.equals(metaName)) {
                    String value = meta.getProperty(metaName);
                    if (StringUtils.isBlank(value)) {
                        return true;
                    } else if (property.contains("*")) {
                        return value.matches(property.replace("*", ".*"));
                    }
                    return properties.get(key).equals(value);
                }
            }
        }
        return false;
    }

    public Properties include() {
        return include;
    }

    public Properties exclude() {
        return exclude;
    }

    public String asString() {
        return filterAsString;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
