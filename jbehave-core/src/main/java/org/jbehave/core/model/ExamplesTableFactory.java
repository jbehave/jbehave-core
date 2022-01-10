package org.jbehave.core.model;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TablePropertiesQueue;
import org.jbehave.core.steps.ParameterControls;
import org.jbehave.core.steps.ParameterConverters;

import java.util.Deque;

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
    private final TableParsers tableParsers;
    private final TableTransformers tableTransformers;

    public ExamplesTableFactory(ResourceLoader resourceLoader, TableTransformers tableTransformers) {
        this(new LocalizedKeywords(), resourceLoader, tableTransformers);
    }

    public ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader, TableTransformers tableTransformers) {
        this(keywords, resourceLoader, new ParameterConverters(resourceLoader, tableTransformers),
                tableTransformers);
    }

    private ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader,
            ParameterConverters parameterConverters, TableTransformers tableTransformers) {
        this(keywords, resourceLoader, parameterConverters, new ParameterControls(),
                new TableParsers(keywords, parameterConverters), tableTransformers);
    }

    public ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader,
                                ParameterConverters parameterConverters, ParameterControls parameterControls,
                                TableParsers tableParsers, TableTransformers tableTransformers) {
        this.keywords = keywords;
        this.resourceLoader = resourceLoader;
        this.parameterConverters = parameterConverters;
        this.parameterControls = parameterControls;
        this.tableParsers = tableParsers;
        this.tableTransformers = tableTransformers;
    }
    
    public ExamplesTableFactory(Configuration configuration) {
        this.keywords = configuration.keywords();
        this.resourceLoader = configuration.storyLoader();
        this.parameterConverters = configuration.parameterConverters();
        this.parameterControls = configuration.parameterControls();
        this.tableParsers = configuration.tableParsers();
        this.tableTransformers = configuration.tableTransformers();
    }

    public ExamplesTable createExamplesTable(String input) {
        TablePropertiesQueue tablePropertiesQueue = tableParsers.parseProperties(input);

        String tableAsString = tablePropertiesQueue.getTable().trim();
        Deque<TableProperties> properties = tablePropertiesQueue.getProperties();

        if (!isTable(tableAsString, properties.peekFirst()) && !tableAsString.isEmpty()) {
            String loadedTable = resourceLoader.loadResourceAsText(tableAsString.trim());
            tablePropertiesQueue = tableParsers.parseProperties(loadedTable);
            tablePropertiesQueue.getProperties().addAll(properties);
        }

        return new ExamplesTable(tablePropertiesQueue, parameterConverters, parameterControls, tableParsers,
                tableTransformers);
    }

    protected boolean isTable(String table, TableProperties properties) {
        String headerSeparator = properties == null ? keywords.examplesTableHeaderSeparator()
                : properties.getHeaderSeparator();
        return table.startsWith(headerSeparator);
    }

    public void useKeywords(Keywords keywords) {
        this.keywords = keywords;
    }

    public Keywords keywords() {
        return this.keywords;
    }
}
