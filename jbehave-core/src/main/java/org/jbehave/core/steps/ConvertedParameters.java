package org.jbehave.core.steps;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.jbehave.core.annotations.Parameter;

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
     * @param values the Map&lt;String,String&gt; of values
     * @param parameterConverters the ParameterConverters used for conversion
     */
    public ConvertedParameters(Map<String, String> values, ParameterConverters parameterConverters) {
        this.values = values;
        this.parameterConverters = parameterConverters;
    }

    @Override
    public <T> T valueAs(String name, Type type) {
        return convert(valueFor(name), type);
    }

    @Override
    public <T> T valueAs(String name, Type type, T defaultValue) {
        if (values.containsKey(name)) {
            return valueAs(name, type);
        }
        return defaultValue;
    }

    @Override
    public <T> T mapTo(Class<T> type) {
        return mapTo(type, Collections.emptyMap());
    }

    @Override
    public <T> T mapTo(Class<T> type, Map<String, String> fieldNameMapping) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();

            for (Entry<String, Field> mappedField : findFields(type, values().keySet(), fieldNameMapping).entrySet()) {
                Field field = mappedField.getValue();
                Object value = valueAs(mappedField.getKey(), field.getGenericType());
                field.setAccessible(true);
                field.set(instance, value);
            }

            return instance;
        } catch (ReflectiveOperationException e) {
            throw new ParametersNotMappableToType(e);
        }
    }

    private static Map<String, Field> findFields(Class<?> type, Set<String> fieldNames,
            Map<String, String> fieldNameMapping) {

        Map<String, Field> mappedFields = new HashMap<>();
        List<String> unmappableFields = new ArrayList<>();

        for (String fieldName : fieldNames) {
            Optional<Field> fieldWrapper = findField(type, fieldName, fieldNameMapping);
            if (fieldWrapper.isPresent()) {
                mappedFields.put(fieldName, fieldWrapper.get());
            } else {
                unmappableFields.add(fieldName);
            }
        }

        if (unmappableFields.isEmpty()) {
            return mappedFields;
        }

        throw new ParametersNotMappableToType(String.format("Unable to map %s field(s) for type %s", unmappableFields,
                type));
    }

    private static <T> Optional<Field> findField(Class<T> type, String name, Map<String, String> fieldNameMapping) {

        String mapping = fieldNameMapping.get(name);
        String fieldName = mapping == null ? name : mapping;

        Optional<Field> field = Stream.of(type.getDeclaredFields())
                                      .filter(f -> f.isAnnotationPresent(Parameter.class))
                                      .filter(f -> fieldName.equals(f.getAnnotation(Parameter.class).name()))
                                      .findFirst();

        return field.isPresent() ? field : findField(type, fieldName);
    }

    private static Optional<Field> findField(Class<?> type, String fieldName) {
        for (Field field : type.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                return Optional.of(field);
            }
        }

        if (type.getSuperclass() != null) {
            return findField(type.getSuperclass(), fieldName);
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> T convert(String value, Type type) {
        return (T) parameterConverters.convert(value, type);
    }

    private String valueFor(String name) {
        if (!values.containsKey(name)) {
            throw new ValueNotFound(name);
        }
        return values.get(name);
    }

    @Override
    public Map<String, String> values() {
        return values;
    }

    @SuppressWarnings("serial")
    public static class ValueNotFound extends RuntimeException {

        public ValueNotFound(String name) {
            super(name);
        }

    }

    @SuppressWarnings("serial")
    public static class ParametersNotMappableToType extends RuntimeException {

        public ParametersNotMappableToType(String message) {
            super(message);
        }

        public ParametersNotMappableToType(Exception cause) {
            super(cause);
        }

    }
}
