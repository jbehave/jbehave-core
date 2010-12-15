package org.jbehave.core.model;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.steps.ParameterConverters;

import static org.apache.commons.lang.StringUtils.isBlank;

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
 * <b>NOTE</b>: Users needing parameter conversion
 * in the ExamplesTable, i.e. invoking {@link ExamplesTable#getRowAsRecord(int)}, will need
 * to use a factory constructor providing explicitly the ParameterConverters instance
 * configured in the {@link Configuration#useParameterConverters(ParameterConverters)}.  
 * </p>
 */
public class ExamplesTableFactory {

    private final Keywords keywords;
    private final ResourceLoader resourceLoader;
    private final ParameterConverters parameterConverters;

    public ExamplesTableFactory() {
        this(new LocalizedKeywords());
    }

    public ExamplesTableFactory(Keywords keywords) {
        this(keywords, new LoadFromURL(), new ParameterConverters());
    }

    public ExamplesTableFactory(ParameterConverters parameterConverters) {
        this(new LocalizedKeywords(), new LoadFromURL(), parameterConverters);
    }

    public ExamplesTableFactory(ResourceLoader resourceLoader) {
        this(new LocalizedKeywords(), resourceLoader, new ParameterConverters());
    }

    public ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader,
            ParameterConverters parameterConverters) {
        this.keywords = keywords;
        this.resourceLoader = resourceLoader;
        this.parameterConverters = parameterConverters;
    }

    public ExamplesTable createExamplesTable(String input) {
        String tableAsString;
        if (isBlank(input) || isTable(input)) {
            tableAsString = input;
        } else {
            tableAsString = resourceLoader.loadResourceAsText(input);
        }
        return new ExamplesTable(tableAsString, keywords.examplesTableHeaderSeparator(),
                keywords.examplesTableValueSeparator(), keywords.examplesTableIgnorableSeparator(), parameterConverters);
    }

    protected boolean isTable(String input) {
        return input.contains(keywords.examplesTableHeaderSeparator());
    }

}
