package org.jbehave.core.model;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromURL;
import org.jbehave.core.io.ResourceLoader;

import static org.apache.commons.lang.StringUtils.isBlank;


/**
 * Factory that creates instances of ExamplesTable from different type of inputs:
 * <ul>
 * <li>table text input, i.e. any input that contains a {@link Keywords#examplesTableHeaderSeparator()}</li>
 * <li>resource path input, the table as text is loaded via the {@link ResourceLoader}.</li>
 * </ul>
 */
public class ExamplesTableFactory {

    private final Keywords keywords;
    private final ResourceLoader resourceLoader;

    public ExamplesTableFactory() {
        this(new LocalizedKeywords());
    }

    public ExamplesTableFactory(Keywords keywords) {
        this(keywords, new LoadFromURL());
    }
    
    public ExamplesTableFactory(ResourceLoader resourceLoader) {
        this(new LocalizedKeywords(), resourceLoader);
    }

    public ExamplesTableFactory(Keywords keywords, ResourceLoader resourceLoader) {
        this.keywords = keywords;
        this.resourceLoader = resourceLoader;
    }

    public ExamplesTable createExamplesTable(String input) {
        String tableAsString;
        if ( isBlank(input) || isTable(input)) {
            tableAsString = input;
        } else {
            tableAsString = resourceLoader.loadResourceAsText(input);
        }
        return new ExamplesTable(tableAsString, keywords.examplesTableHeaderSeparator(),
                keywords.examplesTableValueSeparator(), keywords.examplesTableIgnorableSeparator());
    }

    protected boolean isTable(String input) {
        return input.contains(keywords.examplesTableHeaderSeparator());
    }

}
