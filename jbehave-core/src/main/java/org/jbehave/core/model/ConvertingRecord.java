package org.jbehave.core.model;


public class ConvertingRecord implements Record {
    private final Record delegate;
    private final ValueConverter parameterConverters;

    /**
     * Class constructor.
     * 
     * @param data the data.
     * @param converter the {@link ValueConverter}.
     */
    public ConvertingRecord(Record delegate, ValueConverter converter) {
        this.delegate = delegate;
        this.parameterConverters = converter;
    }

    /**
     * Returns the value in a column.
     * 
     * @param name the name of the column.
     * @return the value.
     */
    public String value(String name) {
        return delegate.value(name);
    }

    /**
     * {@inheritDoc}
     */
    public boolean contains(String name) {
        return delegate.contains(name);
    }

    /**
     * Returns the value in a column.
     * 
     * @param name the name of the column.
     * @param defaultValue the default value if the column doesn't exist.
     * @return the value.
     */
    public String value(String name, String defaultValue) {
        if (contains(name))
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
        if (contains(name))
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