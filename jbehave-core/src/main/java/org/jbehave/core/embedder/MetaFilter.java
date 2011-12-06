package org.jbehave.core.embedder;

import groovy.lang.GroovyClassLoader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
 * A filter is uniquely identified by its String representation which is parsed
 * and matched by the {@link MetaMatcher} to determine if the {@link Meta} is
 * allowed or not.
 * </p>
 * 
 * <p>
 * The {@link DefaultMetaMatcher} interprets the filter as a sequence of any
 * name-value properties (separated by a space), prefixed by "+" for inclusion
 * and "-" for exclusion. E.g.:
 * 
 * <pre>
 * MetaFilter filter = new MetaFilter("+author Mauro -theme smoke testing +map *API -skip");
 * filter.allow(new Meta(asList("map someAPI")));
 * </pre>
 * 
 * </p>
 * <p>
 * The use of the {@link GroovyMetaMatcher} is triggered by the prefix "groovy:" and 
 * allows the filter to be interpreted as a Groovy expression.
 * </p>
 * <pre>
 * MetaFilter filter = new MetaFilter("groovy: (a == '11' | a == '22') && b == '33'");
 * </pre>
 */
public class MetaFilter {

    public static final MetaFilter EMPTY = new MetaFilter();

    private final String filterAsString;
    private final EmbedderMonitor monitor;

    private MetaMatcher metaMatcher;

    public MetaFilter() {
        this("");
    }

    public MetaFilter(String filterAsString) {
        this(filterAsString, new PrintStreamEmbedderMonitor());
    }

    public MetaFilter(String filterAsString, EmbedderMonitor monitor) {
        this.filterAsString = filterAsString == null ? "" : filterAsString;
        this.monitor = monitor;
        this.metaMatcher = createMetaMatcher(this.filterAsString);
        this.metaMatcher.parse(filterAsString);
    }

    /**
     * Creates a MetaMatcher based on the filter content.  
     * 
     * @param filterAsString the String representation of the filter
     * @return A MetaMatcher
     */
    protected MetaMatcher createMetaMatcher(String filterAsString) {
        if (filterAsString.startsWith("groovy: ")) {
            return new GroovyMetaMatcher();
        }
        return new DefaultMetaMatcher();
    }

    public boolean allow(Meta meta) {
        boolean allowed = this.metaMatcher.match(meta);
        if (!allowed) {
            monitor.metaNotAllowed(meta, this);
        }
        return allowed;
    }

    public MetaMatcher metaMatcher() {
        return metaMatcher;
    }

    public String asString() {
        return filterAsString;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public interface MetaMatcher {

        void parse(String filterAsString);

        boolean match(Meta meta);

    }

    public class DefaultMetaMatcher implements MetaMatcher {

        private final Properties include = new Properties();
        private final Properties exclude = new Properties();

        public Properties include() {
            return include;
        }

        public Properties exclude() {
            return exclude;
        }

        public void parse(String filterAsString) {
            parse(include, "+");
            parse(exclude, "-");
        }

        public boolean match(Meta meta) {
            boolean matched;
            if (!include.isEmpty() && exclude.isEmpty()) {
                matched = match(include, meta);
            } else if (include.isEmpty() && !exclude.isEmpty()) {
                matched = !match(exclude, meta);
            } else if (!include.isEmpty() && !exclude.isEmpty()) {
                matched = match(merge(include, exclude), meta) && !match(exclude, meta);
            } else {
                matched = true;
            }
            return matched;
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
            boolean matches = false;
            for (Object key : properties.keySet()) {
                String property = (String) properties.get(key);
                for (String metaName : meta.getPropertyNames()) {
                    if (key.equals(metaName)) {
                        String value = meta.getProperty(metaName);
                        if (StringUtils.isBlank(value)) {
                            matches = true;
                        } else if (property.contains("*")) {
                            matches = value.matches(property.replace("*", ".*"));
                        } else {
                            matches = properties.get(key).equals(value);
                        }
                    }
                    if (matches) {
                        break;
                    }
                }
            }
            return matches;
        }

    }

    public class GroovyMetaMatcher implements MetaMatcher {

        private Class<?> groovyClass;
        private Field metaField;
        private Method match;

        public void parse(String filterAsString) {
            String groovyString = "public class GroovyMatcher {\n" +
                    "public org.jbehave.core.model.Meta meta\n" +
                    "  public boolean match() {\n" +
                    "    return (" + filterAsString.substring("groovy: ".length()) + ")\n" +
                    "  }\n" +
                    "  def propertyMissing(String name) {\n" +
                    "    if (!meta.hasProperty(name)) {\n" +
                    "      return false\n" +
                    "    }\n" +
                    "    def v = meta.getProperty(name)\n" +
                    "    if (v.equals('')) {\n" +
                    "      return true\n" +
                    "    }\n" +
                    "    return v\n" +
                    "  }\n" +
                    "}";

            GroovyClassLoader loader = new GroovyClassLoader(getClass().getClassLoader());
            groovyClass = loader.parseClass(groovyString);
            try {
                match = groovyClass.getDeclaredMethod("match");
                metaField = groovyClass.getField("meta");
            } catch (NoSuchFieldException e) {
                // can never occur as we control the groovy string
            } catch (NoSuchMethodException e) {
                // can never occur as we control the groovy string
            }
        }

        public boolean match(Meta meta) {
            try {
                Object matcher = groovyClass.newInstance();
                metaField.set(matcher, meta);
                return (Boolean) match.invoke(matcher);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
