package org.jbehave.core.model;

import java.lang.reflect.Type;

import com.google.gson.Gson;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.steps.ParameterConverters;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class JsonFactory {

    private Keywords keywords;
    private final ResourceLoader resourceLoader;
    private final ParameterConverters parameterConverters;

    public JsonFactory() {
        this(new LocalizedKeywords());
    }

    public JsonFactory(final Keywords keywords) {
        this(keywords, new LoadFromClasspath(), new ParameterConverters());
    }

    public JsonFactory(final ResourceLoader resourceLoader) {
        this(new LocalizedKeywords(), resourceLoader, new ParameterConverters());
    }

    public JsonFactory(final ParameterConverters parameterConverters) {
        this(new LocalizedKeywords(), new LoadFromClasspath(), parameterConverters);
    }

    public JsonFactory(final Keywords keywords, final ResourceLoader resourceLoader,
            final ParameterConverters parameterConverters) {
        this.keywords = keywords;
        this.resourceLoader = resourceLoader;
        this.parameterConverters = parameterConverters;
    }

    public JsonFactory(final Configuration configuration) {
        this.keywords = configuration.keywords();
        this.resourceLoader = configuration.storyLoader();
        this.parameterConverters = configuration.parameterConverters();
    }

    public Object createJson(final String input, final Type type) {
        String jsonAsString;
        if (isBlank(input) || isJson(input)) {
            jsonAsString = input;
        } else {
            jsonAsString = resourceLoader.loadResourceAsText(input);
        }
        return new Gson().fromJson(jsonAsString, type);
    }

    protected boolean isJson(final String input) {
        return (input.startsWith("[") && input.endsWith("]")) || (input.startsWith("{") && input.endsWith("}"));
    }

    public void useKeywords(final Keywords keywords) {
        this.keywords = keywords;
    }

    public Keywords keywords() {
        return this.keywords;
    }

}
