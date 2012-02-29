package org.jbehave.core.steps;

import java.util.Map;

/**
 * Implementation of Parameters that uses {@link ParameterConverters} to convert
 * values.
 */
public class ConvertedParameters implements Parameters {

    private final Map<String, String> values;
    private final ParameterConverters parameterConverters;

    /**
     * Creates an instance of ConvertedParameters from a Row which provides the
     * values
     * 
     * @param row the Row to get the values from
     * @param parameterConverters the ParameterConverters used for conversion
     */
    public ConvertedParameters(Row row, ParameterConverters parameterConverters) {
        this(row.values(), parameterConverters);
    }

    /**
     * Creates an instance of ConvertedParameters with given values
     * 
     * @param values the Map<String,String> of values
     * @param parameterConverters the ParameterConverters used for conversion
     */
    public ConvertedParameters(Map<String, String> values, ParameterConverters parameterConverters) {
        this.values = values;
        this.parameterConverters = parameterConverters;
    }

    public <T> T valueAs(String name, Class<T> type) {
        return convert(valueFor(name), type);
    }

    public <T> T valueAs(String name, Class<T> type, T defaultValue) {
        if (values.containsKey(name)) {
            return valueAs(name, type);
        }
        return defaultValue;
    }

    private <T> T convert(String value, Class<T> type) {
        return type.cast(parameterConverters.convert(value, type));
    }

    private String valueFor(String name) {
        if ( !values.containsKey(name) ){
            throw new ValueNotFound(name);
        }
        return values.get(name);
    }

    public Map<String, String> values() {
        return values;
    }
    
    @SuppressWarnings("serial")
    public static class ValueNotFound extends RuntimeException {

        public ValueNotFound(String name) {
            super(name);
        }
        
    }

}
