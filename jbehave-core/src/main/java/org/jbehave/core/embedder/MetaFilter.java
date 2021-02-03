package org.jbehave.core.embedder;

import groovy.lang.GroovyClassLoader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
 * MetaFilter filter = new MetaFilter(
 * 		&quot;+author Mauro -theme smoke testing +map *API -skip&quot;);
 * filter.allow(new Meta(asList(&quot;map someAPI&quot;)));
 * </pre>
 * 
 * </p>
 * <p>
 * The use of the {@link GroovyMetaMatcher} is triggered by the prefix "groovy:"
 * and allows the filter to be interpreted as a Groovy expression.
 * </p>
 * 
 * <pre>
 * MetaFilter filter = new MetaFilter(
 * 		&quot;groovy: (a == '11' | a == '22') &amp;&amp; b == '33'&quot;);
 * </pre>
 * <p>
 * Custom {@link MetaMatcher} instances can also be provided as a map, indexed
 * by the prefix used in the filter content:
 * </p>
 * 
 * <pre>
 * Map&lt;String, MetaMatcher&gt; customMatchers = new HashMap&lt;&gt;();
 * customMatchers.put(&quot;ruby:&quot;, new RubyMetaMatcher());
 * MetaFilter filter = new MetaFilter(&quot;ruby: # some ruby script&quot;, customMatchers);
 * </pre>
 * <p>
 * Custom MetaMatcher instances, when they are matched by the prefix of the
 * filter content, will take precedence over the Groovy or default matchers.
 * </p>
 */
public class MetaFilter {

    private static final String NO_FILTER = "";
	private static final String GROOVY = "groovy:";

	public static final MetaFilter EMPTY = new MetaFilter();

    private final String filterAsString;
    private final EmbedderMonitor monitor;
	private final Map<String, MetaMatcher> metaMatchers;
    private final MetaMatcher filterMatcher;

    public MetaFilter() {
        this(NO_FILTER);
    }

    public MetaFilter(String filterAsString) {
        this(filterAsString, new PrintStreamEmbedderMonitor());
    }

    public MetaFilter(String filterAsString, EmbedderMonitor monitor) {
    	this(filterAsString, monitor, new HashMap<String, MetaMatcher>());
    }

    public MetaFilter(String filterAsString, Map<String,MetaMatcher> metaMatchers) {
    	this(filterAsString, new PrintStreamEmbedderMonitor(), metaMatchers);
    }

    public MetaFilter(String filterAsString, EmbedderMonitor monitor, Map<String,MetaMatcher> metaMatchers) {
		this.filterAsString = filterAsString == null ? NO_FILTER : filterAsString;
        this.monitor = monitor;
        this.metaMatchers = metaMatchers;
        this.filterMatcher = createMetaMatcher(this.filterAsString, this.metaMatchers);
        this.filterMatcher.parse(filterAsString);
    }

    /**
     * Creates a MetaMatcher based on the filter content.  
     * 
     * @param filterAsString the String representation of the filter
     * @param metaMatchers the Map of custom MetaMatchers 
     * @return A MetaMatcher used to match the filter content
     */
    protected MetaMatcher createMetaMatcher(String filterAsString, Map<String, MetaMatcher> metaMatchers) {
    	for ( String key : metaMatchers.keySet() ){
    		if ( filterAsString.startsWith(key)){
    			return metaMatchers.get(key);
    		}
    	}
        if (filterAsString.startsWith(GROOVY)) {
            return new GroovyMetaMatcher();
        }
        return new DefaultMetaMatcher();
    }

    public boolean allow(Meta meta) {
        boolean allowed = this.filterMatcher.match(meta);
        if (!allowed) {
            monitor.metaExcluded(meta, this);
        }
        return allowed;
    }

    public MetaMatcher metaMatcher() {
        return filterMatcher;
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

        @Override
        public void parse(String filterAsString) {
            parse(include, "+");
            parse(exclude, "-");
        }

        @Override
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
            Set<String> found = new HashSet<>();
            while (matcher.find()) {
                found.add(matcher.group().trim());
            }
            return found;
        }

        private Pattern findAllPrefixed(String prefix) {
            return Pattern.compile("((^|\\s)\\" + prefix + "([^\\s](\\s[^\\-\\+])?)*)", Pattern.DOTALL);
        }

        private Properties merge(Properties include, Properties exclude) {
            Set<Object> in = new HashSet<>(include.keySet());
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

        @Override
        public void parse(String filterAsString) {
            String groovyString = "public class GroovyMatcher {\n" +
                    "public org.jbehave.core.model.Meta meta\n" +
                    "  public boolean match() {\n" +
                    "    return (" + filterAsString.substring(GROOVY.length()) + ")\n" +
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

        @Override
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

	public boolean isEmpty() {
		return EMPTY == this;
	}

}
