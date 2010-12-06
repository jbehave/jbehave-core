package org.jbehave.core.model;


import java.util.Map;

public class Record {
    private final Map<String, String> data;
    private final ValueConverter parameterConverters;

    /**
     * Class constructor.
     * 
     * @param data the data.
     * @param converter the {@link ValueConverter}.
     */
    public Record(Map<String, String> data, ValueConverter converter) {
        this.data = data;
        this.parameterConverters = converter;
    }

    /**
     * Returns the value in a column.
     * 
     * @param name the name of the column.
     * @return the value.
     */
    public String value(String name) {
        return data.get(name);
    }

    /**
     * Returns the value in a column.
     * 
     * @param name the name of the column.
     * @param defaultValue the default value if the column doesn't exist.
     * @return the value.
     */
    public String value(String name, String defaultValue) {
        if (data.containsKey(name))
            return value(name);

        return defaultValue;
    }

    /**
     * Returns the value in a column converted.
     * 
     * @param <T> the type to convert to.
     * @param type the type to convert to.
     * @param name the name of the column.
     * @return the value.
     */
    public <T> T valueAs(String name, Class<T> type) {
        return convert(type, value(name));
    }

    /**
     * Returns the value in a column converted.
     * 
     * @param <T> the type to convert to.
     * @param type the type to convert to.
     * @param name the name of the column.
     * @param defaultValue the default value if the column doesn't exist.
     * @return the value.
     */
    public <T> T valueAs(String name, Class<T> type, T defaultValue) {
        if (data.containsKey(name))
            return valueAs(name, type);

        return defaultValue;
    }

    /**
     * Returns the value in a column converted.
     * 
     * @param <T> the type to convert to.
     * @param type the type to convert to.
     * @param name the name of the column.
     * @param defaultValue the default value if the column doesn't exist.
     * @return the value.
     */
    public <T> T valueAs(String name, Class<T> type, String defaultValue) {
        return convert(type, value(name, defaultValue));
    }

    private <T> T convert(Class<T> type, String value) {
        return type.cast(parameterConverters.convert(value, type));
    }
}