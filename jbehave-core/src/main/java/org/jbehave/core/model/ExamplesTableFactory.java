package org.jbehave.core.model;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.steps.ParameterConverters;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Factory that creates instances of ExamplesTable from different type of
 * inputs:
 * <ul>
 * <li>table text input, i.e. any input that contains a
 * {@link Keywords#examplesTableHeaderSeparator()}</li>
 * <li>resource path input, the table as text is loaded via the
 * {@link ResourceLoader} (defaulting to {@link LoadFromClasspath}).</li>
 * </ul>
 * Factory also supports optional specification of {@link ParameterConverters}
 * to allow the ExamplesTable to convert row values.
 * <p>
 * <b>NOTE</b>: Users needing parameter conversion in the ExamplesTable, i.e.
 * invoking {@link ExamplesTable#getRowAsParameters(int)}, will need to use a
 * factory constructor providing explicitly the ParameterConverters instance
 * configured in the
 * {@link Configuration#useParameterConverters(ParameterConverters)}.
 * </p>
 */
public class ExamplesTableFactory {

    private Keywords keywords;
    private final ResourceLoader resourceLoader;
    private final ParameterConverters parameterConverters;
    private final TableTransformers tableTransformers;

    public ExamplesTableFactory() {
        this(new LocalizedKeywords());
    }

    public ExamplesTableFactory(Keywords keywords) {
        this(keywords, new LoadFromClasspath(), new ParameterConverters(), new TableTransformers());
    }

    public ExamplesTableFactory(ResourceLoader resourceLoader) {
        this(new LocalizedKeywords(), resourceLoader, new ParameterConverters(), new TableTransformers());
    }

    public ExamplesTableFactory(ParameterConverters parameterConverters) {
        this(new LocalizedKeywords(), new LoadFromClasspath(), parameterConverters, new TableTransformers());
    }

    public ExamplesTableFactory(TableTransformers tableTransformers) {
        this(new LocalizedKeywords(), new LoadFromClasspath(), new ParameterConverters(), tableTransformers);
    }

    public ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader,
            ParameterConverters parameterConverters) {
        this(keywords, resourceLoader, parameterConverters, new TableTransformers());
    }

    public ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader,
            ParameterConverters parameterConverters, TableTransformers tableTranformers) {
        this.keywords = keywords;
        this.resourceLoader = resourceLoader;
        this.parameterConverters = parameterConverters;
        this.tableTransformers = tableTranformers;
    }
    
    public ExamplesTableFactory(Configuration configuration) {
    	this.keywords = configuration.keywords();
    	this.resourceLoader = configuration.storyLoader();
    	this.parameterConverters = configuration.parameterConverters();
    	this.tableTransformers = new TableTransformers();
    }

    public ExamplesTable createExamplesTable(String input) {
        String tableAsString;
        if (isBlank(input) || isTable(input)) {
            tableAsString = input;
        } else {
            tableAsString = resourceLoader.loadResourceAsText(input);
        }
        return new ExamplesTable(tableAsString, keywords.examplesTableHeaderSeparator(),
                keywords.examplesTableValueSeparator(), keywords.examplesTableIgnorableSeparator(),
                parameterConverters, tableTransformers);
    }

    protected boolean isTable(String input) {
        return input.trim().startsWith(keywords.examplesTableHeaderSeparator())
                || ExamplesTable.INLINED_PROPERTIES_PATTERN.matcher(input).matches();
    }

    public void useKeywords(Keywords keywords){
    	this.keywords = keywords;
    }

	public Keywords keywords() {
		return this.keywords;
	}
}
