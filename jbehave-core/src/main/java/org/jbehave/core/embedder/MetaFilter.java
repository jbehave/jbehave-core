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
 * new Filter("+author Mauro -theme smoke testing +map UI -author Paul")
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
        Matcher matcher = findAllPrefixedWords(prefix).matcher(filterAsString);
        Set<String> found = new HashSet<String>();
        while (matcher.find()) {
            found.add(matcher.group().trim());
        }
        return found;
    }

    private Pattern findAllPrefixedWords(String prefix) {
        return Pattern.compile("(\\" + prefix + "(\\w|\\s)*)", Pattern.DOTALL);
    }

    public boolean allow(Meta meta) {
        boolean allowed = true;
        if (!include.isEmpty() && exclude.isEmpty()) {
            allowed = match(include, meta);
        } else if (include.isEmpty() && !exclude.isEmpty()) {
            allowed = !match(exclude, meta);
        }
        if (!allowed) {
            monitor.metaNotAllowed(meta, this);
        }
        return allowed;
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
