package org.jbehave.core.model;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.ExamplesTable.ExamplesTableData;
import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;

/**
 * Factory that creates instances of ExamplesTable from different type of
 * inputs:
 * <ul>
 * <li>table text input, i.e. any input that contains a
 * {@link Keywords#examplesTableHeaderSeparator()}</li>
 * <li>resource path input, the table as text is loaded via the
 * {@link ResourceLoader}.</li>
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
    private final ParameterControls parameterControls;
    private final TableTransformers tableTransformers;

    public ExamplesTableFactory(ResourceLoader resourceLoader, TableTransformers tableTransformers) {
        this(new LocalizedKeywords(), resourceLoader, tableTransformers);
    }

    public ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader, TableTransformers tableTransformers) {
        this(keywords, resourceLoader, new ParameterConverters(resourceLoader, tableTransformers),
                new ParameterControls(), tableTransformers);
    }

    public ExamplesTableFactory(ResourceLoader resourceLoader, ParameterConverters parameterConverters,
            ParameterControls parameterControls, TableTransformers tableTransformers) {
        this(new LocalizedKeywords(), resourceLoader, parameterConverters, parameterControls, tableTransformers);
    }

    public ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader,
            ParameterConverters parameterConverters, ParameterControls parameterControls,
            TableTransformers tableTransformers) {
        this.keywords = keywords;
        this.resourceLoader = resourceLoader;
        this.parameterConverters = parameterConverters;
        this.parameterControls = parameterControls;
        this.tableTransformers = tableTransformers;
    }
    
    public ExamplesTableFactory(Configuration configuration) {
        this.keywords = configuration.keywords();
        this.resourceLoader = configuration.storyLoader();
        this.parameterConverters = configuration.parameterConverters();
        this.parameterControls = configuration.parameterControls();
        this.tableTransformers = configuration.tableTransformers();
    }

    public ExamplesTable createExamplesTable(String input) {
        ExamplesTableData data = getExamplesTableData(input);

        String tableAsString = data.getTable().trim();
        ExamplesTableProperties properties = data.getProperties().peekLast();

        if (!isTable(tableAsString, properties) && !tableAsString.isEmpty()) {
            String loadedTable = resourceLoader.loadResourceAsText(tableAsString.trim());
            data = getExamplesTableData(loadedTable);
            data.getProperties().addFirst(properties);
        }

        return new ExamplesTable(data, keywords.examplesTableHeaderSeparator(),
                keywords.examplesTableValueSeparator(), keywords.examplesTableIgnorableSeparator(),
                parameterConverters, parameterControls, tableTransformers);
    }

    protected boolean isTable(String table, ExamplesTableProperties properties) {
        String headerSeparator = properties == null ? keywords.examplesTableHeaderSeparator()
                : properties.getHeaderSeparator();
        return table.startsWith(headerSeparator);
    }

    private ExamplesTableData getExamplesTableData(String input) {
        return TableUtils.parseData(input, keywords);
    }

    public void useKeywords(Keywords keywords){
        this.keywords = keywords;
    }

    public Keywords keywords() {
        return this.keywords;
    }
}
