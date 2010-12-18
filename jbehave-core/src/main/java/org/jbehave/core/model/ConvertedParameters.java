package org.jbehave.core.model;

import java.util.Map;

import org.jbehave.core.steps.ParameterConverters;

/**
 * Implementation of Parameters that uses {@link ParameterConverters} to convert
 * values.
 */
public class ConvertedParameters implements Parameters {

    private final Map<String, String> values;
    private final ParameterConverters parameterConverters;

    /**
     * Creates an instance of ConvertedParameters
     * 
     * @param values the Map<String,String> of values
     * @param parameterConverters the ParameterConverters used for conversion
     */
    public ConvertedParameters(Map<String, String> values, ParameterConverters parameterConverters) {
        this.values = values;
        this.parameterConverters = parameterConverters;
    }

    public boolean hasValue(String name) {
        return values.containsKey(name);
    }

    public <T> T valueAs(String name, Class<T> type) {
        return convert(type, values.get(name));
    }

    public <T> T valueAs(String name, Class<T> type, T defaultValue) {
        if (values.containsKey(name)) {
            return valueAs(name, type);
        }
        return defaultValue;
    }

    private <T> T convert(Class<T> type, String value) {
        return type.cast(parameterConverters.convert(value, type));
    }

    public Map<String, String> values() {
        return values;
    }

}