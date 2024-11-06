package org.jbehave.core.model;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Deque;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTable.TablePropertiesQueue;
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
    private final TableParsers tableParsers;
    private final TableTransformers tableTransformers;
    private final TableTransformerMonitor tableTransformerMonitor;

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
        this(keywords, resourceLoader, parameterConverters, parameterControls, tableParsers, tableTransformers,
                new NullTableTransformerMonitor());
    }

    public ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader,
                                ParameterConverters parameterConverters, ParameterControls parameterControls,
                                TableParsers tableParsers, TableTransformers tableTransformers,
                                TableTransformerMonitor tableTransformerMonitor) {
        this.keywords = keywords;
        this.resourceLoader = resourceLoader;
        this.parameterConverters = parameterConverters;
        this.parameterControls = parameterControls;
        this.tableParsers = tableParsers;
        this.tableTransformers = tableTransformers;
        this.tableTransformerMonitor = tableTransformerMonitor;
    }
    
    public ExamplesTableFactory(Configuration configuration) {
        this.keywords = configuration.keywords();
        this.resourceLoader = configuration.storyLoader();
        this.parameterConverters = configuration.parameterConverters();
        this.parameterControls = configuration.parameterControls();
        this.tableParsers = configuration.tableParsers();
        this.tableTransformers = configuration.tableTransformers();
        this.tableTransformerMonitor = configuration.tableTransformerMonitor();
    }

    public ExamplesTable createExamplesTable(String input) {
        TablePropertiesQueue tablePropertiesQueue = tableParsers.parseProperties(input);

        String tableAsString = tablePropertiesQueue.getTable().trim();
        Deque<TableProperties> properties = tablePropertiesQueue.getProperties();

        if (isExternal(tableAsString, properties.peekFirst())) {
            checkResourceProperties(input, properties.peekFirst());
            String loadedTable = resourceLoader.loadResourceAsText(tableAsString.trim());
            tablePropertiesQueue = tableParsers.parseProperties(loadedTable);
            Deque<TableProperties> target = tablePropertiesQueue.getProperties();
            TableProperties headProperties = target.peekFirst();
            for (TableProperties outerProperties : properties) {
                outerProperties.overrideSeparatorsFrom(headProperties);
                target.addLast(outerProperties);
            }

            String table = tablePropertiesQueue.getTable();
            if (isExternal(tablePropertiesQueue.getTable(), headProperties)) {
                table = target.stream().map(TableProperties::asString)
                                       .collect(Collectors.joining(
                                               System.lineSeparator(),
                                               "",
                                               System.lineSeparator() + table));
                return createExamplesTable(table);
            }

            boolean hasTransformers = target.getFirst().getTransformer() != null;
            if (hasTransformers) {
                loadedTable = TableTransformersExecutor.applyTransformers(tableTransformers,
                        tablePropertiesQueue.getTable(), tableParsers, target, tableTransformerMonitor);
                tablePropertiesQueue = tableParsers.parseProperties(loadedTable);
                TableProperties parsedProperties = tablePropertiesQueue.getProperties().getFirst();
                if (StringUtils.isEmpty(parsedProperties.getPropertiesAsString())) {
                    TableProperties newProperties = new TableProperties("", keywords, parameterConverters);
                    newProperties.overrideSeparatorsFrom(target.peekLast());
                    target.clear();
                    target.addFirst(newProperties);
                } else {
                    target = tablePropertiesQueue.getProperties();
                }
            }

            tablePropertiesQueue = new TablePropertiesQueue(tablePropertiesQueue.getTable(), target);
        }

        return new ExamplesTable(tablePropertiesQueue, parameterConverters, parameterControls, tableParsers,
                tableTransformers, tableTransformerMonitor);
    }

    private boolean isExternal(String tableAsString, TableProperties properties) {
        return !isTable(tableAsString, properties) && !tableAsString.isEmpty();
    }

    private void checkResourceProperties(String table, TableProperties properties) {
        isTrue(keywords.examplesTableHeaderSeparator().equals(properties.getHeaderSeparator())
                && keywords.examplesTableValueSeparator().equals(properties.getValueSeparator()) 
                && keywords.examplesTableIgnorableSeparator().equals(properties.getIgnorableSeparator()),
                "Examples table parsing separators are not allowed to be applied outside of external table they "
                        + "belong to:%n%s", table);
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
