package org.jbehave.core.reporters;

import java.util.Properties;

import org.jbehave.core.io.StoryNameResolver;
import org.jbehave.core.io.UnderscoredToCapitalized;

/**
 * <p>
 * Freemarker-based {@link ViewGenerator}, which uses the configured FTL
 * templates for the views. The default view properties are overridable via the
 * method {@link Properties} parameter. To override, specify the path to the new
 * template under the appropriate key:
 * <p>
 * The view generator provides the following default properties:
 * 
 * <pre>
 * &quot;views&quot;: &quot;ftl/jbehave-views.ftl&quot;
 * &quot;maps&quot;: &quot;ftl/jbehave-maps.ftl&quot;
 * &quot;navigator&quot;: &quot;ftl/jbehave-navigator.ftl&quot;
 * &quot;reports&quot;: &quot;ftl/jbehave-reports-with-totals.ftl&quot;
 * &quot;decorated&quot;: &quot;ftl/jbehave-report-decorated.ftl&quot;
 * &quot;nonDecorated&quot;: &quot;ftl/jbehave-report-non-decorated.ftl&quot;
 * &quot;decorateNonHtml&quot;: &quot;true&quot;
 * &quot;defaultFormats&quot;: &quot;stats&quot;
 * &quot;viewDirectory&quot;: &quot;view&quot;
 * </pre>
 * 
 * </p>
 * <p>
 * The view generator can also specify the {@link StoryNameResolver} (defaulting
 * to {@link UnderscoredToCapitalized}) and the class whose ClassLoader
 * Freemarker uses to load the templates from (defaulting to
 * {@link FreemarkerProcessor}).
 * </p>
 * 
 * @author Mauro Talevi
 */
public class FreemarkerViewGenerator extends TemplateableViewGenerator {

    public FreemarkerViewGenerator() {
        this(new UnderscoredToCapitalized());
    }

    public FreemarkerViewGenerator(StoryNameResolver nameResolver) {
        super(nameResolver, new FreemarkerProcessor());
    }

    public FreemarkerViewGenerator(Class<?> templateLoadingFrom) {
        this(new UnderscoredToCapitalized(), templateLoadingFrom);
    }

    public FreemarkerViewGenerator(StoryNameResolver nameResolver, Class<?> templateLoadingFrom) {
        super(nameResolver, new FreemarkerProcessor(templateLoadingFrom));
    }

    public Properties defaultViewProperties() {
        Properties properties = new Properties(super.defaultViewProperties());
        properties.setProperty("views", "ftl/jbehave-views.ftl");
        properties.setProperty("maps", "ftl/jbehave-maps.ftl");
        properties.setProperty("navigator", "ftl/jbehave-navigator.ftl");
        properties.setProperty("reports", "ftl/jbehave-reports-with-totals.ftl");
        properties.setProperty("decorated", "ftl/jbehave-report-decorated.ftl");
        properties.setProperty("nonDecorated", "ftl/jbehave-report-non-decorated.ftl");
        return properties;
    }

}
