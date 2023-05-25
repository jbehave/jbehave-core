package org.jbehave.core.steps;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.Validate.isTrue;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.jbehave.core.annotations.AsJson;
import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformers;
import org.jbehave.core.model.Verbatim;

/**
 * <p>
 * Facade responsible for converting parameter values to Java objects. It allows
 * the registration of several {@link ParameterConverter} instances, and the
 * first one that is found to matches the appropriate parameter type is used.
 * </p>
 * <p>
 * Converters for several Java types are provided out-of-the-box:
 * <ul>
 * <li>{@link ParameterConverters.NumberConverter NumberConverter}</li>
 * <li>{@link ParameterConverters.NumberListConverter NumberListConverter}</li>
 * <li>{@link ParameterConverters.StringConverter StringConverter}</li>
 * <li>{@link ParameterConverters.StringListConverter StringListConverter}</li>
 * <li>{@link ParameterConverters.DateConverter DateConverter}</li>
 * <li>{@link ParameterConverters.ExamplesTableConverter ExamplesTableConverter}</li>
 * <li>{@link ParameterConverters.ExamplesTableParametersConverter ExamplesTableParametersConverter}</li>
 * <li>{@link ParameterConverters.MethodReturningConverter MethodReturningConverter}</li>
 * <li>{@link ParameterConverters.JsonConverter JsonConverter}</li>
 * </ul>
 * </p>
 */
public class ParameterConverters {

    public static final StepMonitor DEFAULT_STEP_MONITOR = new SilentStepMonitor();
    public static final Locale DEFAULT_NUMBER_FORMAT_LOCAL = Locale.ENGLISH;
    public static final String DEFAULT_COLLECTION_SEPARATOR = ",";

    public static final boolean DEFAULT_THREAD_SAFETY = true;

    private static final String DEFAULT_TRUE_VALUE = "true";
    private static final String DEFAULT_FALSE_VALUE = "false";

    private final StepMonitor monitor;
    private final List<ParameterConverter> converters;
    private final boolean threadSafe;
    private String escapedCollectionSeparator;


    /**
     * Creates a ParameterConverters using the default resource loader and table transformers,
     * a SilentStepMonitor, English as Locale and "," as collection separator.
     */
    public ParameterConverters() {
        this(new LoadFromClasspath(), new TableTransformers());
    }

    /**
     * Creates a ParameterConverters using the given table transformers.
     *
     * @param tableTransformers the table transformers
     */
    public ParameterConverters(TableTransformers tableTransformers) {
        this(new LoadFromClasspath(), tableTransformers);
    }

    /**
     * Creates a ParameterConverters of ParameterConverters using the given resource loader.
     *
     * @param resourceLoader the resource loader
     */
    public ParameterConverters(ResourceLoader resourceLoader) {
        this(resourceLoader, new TableTransformers());
    }

    /**
     * Creates a ParameterConverters given resource loader and table transformers.
     *
     * @param resourceLoader the resource loader
     * @param tableTransformers the table transformers
     */
    public ParameterConverters(ResourceLoader resourceLoader, TableTransformers tableTransformers) {
        this(DEFAULT_STEP_MONITOR, resourceLoader, new ParameterControls(), tableTransformers);
    }

    /**
     * Creates a ParameterConverters using given StepMonitor, resource loader and table transformers.
     *
     * @param monitor the StepMonitor to use
     * @param resourceLoader the resource loader
     * @param parameterControls the parameter controls
     * @param tableTransformers the table transformers
     */
    public ParameterConverters(StepMonitor monitor, ResourceLoader resourceLoader, ParameterControls parameterControls,
            TableTransformers tableTransformers) {
        this(monitor, resourceLoader, parameterControls, tableTransformers, DEFAULT_NUMBER_FORMAT_LOCAL,
                DEFAULT_COLLECTION_SEPARATOR, DEFAULT_THREAD_SAFETY);
    }

    /**
     * Creates a ParameterConverters using given StepMonitor, keywords, resource loader and table transformers.
     *
     * @param monitor the StepMonitor to use
     * @param keywords the keywords to use
     * @param resourceLoader the resource loader
     * @param parameterControls the parameter controls
     * @param tableTransformers the table transformers
     */
    public ParameterConverters(StepMonitor monitor, Keywords keywords, ResourceLoader resourceLoader,
            ParameterControls parameterControls, TableTransformers tableTransformers) {
        this(monitor, keywords, resourceLoader, parameterControls, tableTransformers, DEFAULT_NUMBER_FORMAT_LOCAL,
                DEFAULT_COLLECTION_SEPARATOR, DEFAULT_THREAD_SAFETY);
    }

    /**
     * Create a ParameterConverters with given thread-safety
     *
     * @param resourceLoader    the resource loader
     * @param parameterControls the parameter controls
     * @param tableTransformers the table transformers
     * @param threadSafe        the boolean flag to determine if access to {@link ParameterConverter} should be
     *                          thread-safe
     */
    public ParameterConverters(ResourceLoader resourceLoader, ParameterControls parameterControls,
            TableTransformers tableTransformers, boolean threadSafe) {
        this(DEFAULT_STEP_MONITOR, resourceLoader, parameterControls, tableTransformers, DEFAULT_NUMBER_FORMAT_LOCAL,
                DEFAULT_COLLECTION_SEPARATOR, threadSafe);
    }

    /**
     * Creates a ParameterConverters for the given StepMonitor, Locale, list
     * separator and thread-safety. When selecting a collectionSeparator, please make
     * sure that this character doesn't have a special meaning in your Locale
     * (for instance "," is used as decimal separator in some Locale)
     *
     * @param monitor             the StepMonitor reporting the conversions
     * @param resourceLoader      the resource loader
     * @param parameterControls   the parameter controls
     * @param tableTransformers   the table transformers
     * @param locale              the Locale to use when reading numbers
     * @param collectionSeparator the String to use as collection separator
     * @param threadSafe          the boolean flag to determine if modification of {@link ParameterConverter} should be
     *                            thread-safe
     */
    public ParameterConverters(StepMonitor monitor, ResourceLoader resourceLoader, ParameterControls parameterControls,
            TableTransformers tableTransformers, Locale locale, String collectionSeparator, boolean threadSafe) {
        this(monitor, new LocalizedKeywords(), resourceLoader, parameterControls, tableTransformers, locale,
                collectionSeparator, threadSafe);

    }

    /**
     * Creates a ParameterConverters for the given StepMonitor, keywords, Locale, list
     * separator and thread-safety. When selecting a collectionSeparator, please make
     * sure that this character doesn't have a special meaning in your Locale
     * (for instance "," is used as decimal separator in some Locale)
     *
     * @param monitor             the StepMonitor reporting the conversions
     * @param resourceLoader      the resource loader
     * @param keywords            the keywords
     * @param parameterControls   the parameter controls
     * @param tableTransformers   the table transformers
     * @param locale              the Locale to use when reading numbers
     * @param collectionSeparator the String to use as collection separator
     * @param threadSafe          the boolean flag to determine if modification of {@link ParameterConverter} should be
     *                            thread-safe
     */
    public ParameterConverters(StepMonitor monitor, Keywords keywords, ResourceLoader resourceLoader,
            ParameterControls parameterControls, TableTransformers tableTransformers, Locale locale,
            String collectionSeparator, boolean threadSafe) {
        this(monitor, new ArrayList<>(), threadSafe);
        this.addConverters(
                defaultConverters(keywords, resourceLoader, parameterControls, new TableParsers(keywords, this),
                        tableTransformers, locale, collectionSeparator));
    }

    /**
     * Creates a ParameterConverters for the given StepMonitor, keywords, Locale, list
     * separator and thread-safety. When selecting a collectionSeparator, please make
     * sure that this character doesn't have a special meaning in your Locale
     * (for instance "," is used as decimal separator in some Locale)
     *
     * @param monitor             the StepMonitor reporting the conversions
     * @param resourceLoader      the resource loader
     * @param keywords            the keywords
     * @param parameterControls   the parameter controls
     * @param tableParsers        the table parsers
     * @param tableTransformers   the table transformers
     * @param locale              the Locale to use when reading numbers
     * @param collectionSeparator the String to use as collection separator
     * @param threadSafe          the boolean flag to determine if modification of {@link ParameterConverter} should be
     *                            thread-safe
     */
    public ParameterConverters(StepMonitor monitor, Keywords keywords, ResourceLoader resourceLoader,
            ParameterControls parameterControls, TableParsers tableParsers, TableTransformers tableTransformers,
            Locale locale, String collectionSeparator, boolean threadSafe) {
        this(monitor, new ArrayList<>(), threadSafe);
        this.addConverters(
                defaultConverters(keywords, resourceLoader, parameterControls, tableParsers, tableTransformers, locale,
                        collectionSeparator));
    }

    private ParameterConverters(StepMonitor monitor, List<ParameterConverter> converters, boolean threadSafe) {
        this.monitor = monitor;
        this.threadSafe = threadSafe;
        this.converters = threadSafe ? new CopyOnWriteArrayList<>(converters)
                : new ArrayList<>(converters);
    }

    protected ParameterConverter[] defaultConverters(Keywords keywords, ResourceLoader resourceLoader,
            ParameterControls parameterControls, TableParsers tableParsers, TableTransformers tableTransformers,
            Locale locale, String collectionSeparator) {
        this.escapedCollectionSeparator = escapeRegexPunctuation(collectionSeparator);
        ExamplesTableFactory tableFactory = new ExamplesTableFactory(keywords, resourceLoader, this, parameterControls,
                tableParsers, tableTransformers);
        JsonFactory jsonFactory = new JsonFactory();
        return new ParameterConverter[] {
            new BooleanConverter(),
            new NumberConverter(NumberFormat.getInstance(locale)),
            new StringConverter(),
            new StringListConverter(escapedCollectionSeparator),
            new DateConverter(),
            new EnumConverter(),
            new ExamplesTableConverter(tableFactory),
            new ExamplesTableParametersConverter(tableFactory),
            new JsonConverter(jsonFactory),
            new FunctionalParameterConverter<>(String.class, Path.class, Paths::get),
            new FunctionalParameterConverter<>(String.class, Currency.class, Currency::getInstance),
            new FunctionalParameterConverter<>(String.class, Pattern.class, Pattern::compile),
            new FunctionalParameterConverter<>(String.class, File.class, File::new),
            new FunctionalParameterConverter<>(String.class, Verbatim.class, Verbatim::new),

            // java.time.* converters
            new FunctionalParameterConverter<>(String.class, Duration.class, Duration::parse),
            new FunctionalParameterConverter<>(String.class, Instant.class, Instant::parse),
            new FunctionalParameterConverter<>(String.class, LocalDate.class, LocalDate::parse),
            new FunctionalParameterConverter<>(String.class, LocalDateTime.class, LocalDateTime::parse),
            new FunctionalParameterConverter<>(String.class, LocalTime.class, LocalTime::parse),
            new FunctionalParameterConverter<>(String.class, MonthDay.class, MonthDay::parse),
            new FunctionalParameterConverter<>(String.class, OffsetDateTime.class, OffsetDateTime::parse),
            new FunctionalParameterConverter<>(String.class, OffsetTime.class, OffsetTime::parse),
            new FunctionalParameterConverter<>(String.class, Period.class, Period::parse),
            new FunctionalParameterConverter<>(String.class, Year.class, Year::parse),
            new FunctionalParameterConverter<>(String.class, YearMonth.class, YearMonth::parse),
            new FunctionalParameterConverter<>(String.class, ZonedDateTime.class, ZonedDateTime::parse),
            new FunctionalParameterConverter<>(String.class, ZoneId.class, ZoneId::of),
            new FunctionalParameterConverter<>(String.class, ZoneOffset.class, ZoneOffset::of),

            // Converters for Optional types
            new FunctionalParameterConverter<>(String.class, OptionalDouble.class,
                    value -> OptionalDouble.of((double) this.convert(value, double.class))
            ),
            new FunctionalParameterConverter<>(String.class, OptionalInt.class,
                    value -> OptionalInt.of((int) this.convert(value, int.class))
            ),
            new FunctionalParameterConverter<>(String.class, OptionalLong.class,
                    value -> OptionalLong.of((long) this.convert(value, long.class))
            )
        };
    }

    // TODO : This is a duplicate from RegExpPrefixCapturing
    private String escapeRegexPunctuation(String matchThis) {
        return matchThis.replaceAll("([\\[\\]\\{\\}\\?\\^\\.\\*\\(\\)\\+\\\\])", "\\\\$1");
    }

    public ParameterConverters addConverters(ParameterConverter... converters) {
        return addConverters(asList(converters));
    }

    public ParameterConverters addConverters(List<? extends ParameterConverter> converters) {
        this.converters.addAll(0, converters);
        return this;
    }

    private static boolean isChainComplete(Queue<ParameterConverter> convertersChain,
            Predicate<Type> sourceTypePredicate) {
        return !convertersChain.isEmpty() && sourceTypePredicate.test(convertersChain.peek().getSourceType());
    }

    private static Object applyConverters(Object value, Type basicType, Queue<ParameterConverter> convertersChain) {
        Object identity = convertersChain.peek().convertValue(value, basicType);
        return convertersChain.stream().skip(1).reduce(identity,
                (v, c) -> c.convertValue(v, c.getTargetType()), (l, r) -> l);
    }

    public Object convert(String value, Type type) {
        return convert(value, String.class, type);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object convert(Object value, Class<?> source, Type type) {
        Predicate<Type> sourceTypePredicate = t -> source.isAssignableFrom((Class<?>) t);
        Queue<ParameterConverter> converters = findConverters(type, sourceTypePredicate);
        if (isChainComplete(converters, sourceTypePredicate)) {
            Object converted = applyConverters(value, type, converters);
            Queue<Class<?>> classes = converters.stream().map(ParameterConverter::getClass)
                    .collect(Collectors.toCollection(LinkedList::new));
            monitor.convertedValueOfType(String.valueOf(value), type, converted, classes);
            return converted;
        }

        if (isAssignableFromRawType(Optional.class, type)) {
            Type elementType = argumentType(type);
            return Optional.of(convert(value, source, elementType));
        }

        if (isAssignableFromRawType(Collection.class, type)) {
            Type elementType = argumentType(type);
            Collection collection = createCollection(rawClass(type));

            if (collection != null) {
                Queue<ParameterConverter> typeConverters = findConverters(elementType, sourceTypePredicate);

                if (!typeConverters.isEmpty()) {
                    Type sourceType = typeConverters.peek().getSourceType();

                    if (String.class.isAssignableFrom((Class<?>) sourceType)) {
                        fillCollection((String) value, escapedCollectionSeparator, typeConverters, elementType,
                                collection);
                    } else if (isAssignableFrom(Parameters.class, sourceType)) {
                        ExamplesTable table = (ExamplesTable) findBaseConverter(ExamplesTable.class).convertValue(value,
                                String.class);
                        fillCollection(table.getRowsAsParameters(), typeConverters, elementType, collection);
                    }

                    return collection;
                }
            }
        }

        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (clazz.isArray()) {
                String[] elements = parseElements((String) value, escapedCollectionSeparator);
                Class elementType = clazz.getComponentType();
                ParameterConverter elementConverter = findBaseConverter(elementType);
                Object array = createArray(elementType, elements.length);

                if (elementConverter != null && array != null) {
                    fillArray(elements, elementConverter, elementType, array);
                    return array;
                }                
            }
        }

        throw new ParameterConversionFailed("No parameter converter for " + type);
    }

    private ParameterConverter findBaseConverter(Type type) {
        for (ParameterConverter converter : converters) {
            if (converter.canConvertFrom(String.class) && converter.canConvertTo(type)) {
                return converter;
            }
        }
        return null;
    }

    private Queue<ParameterConverter> findConverters(Type type, Predicate<Type> sourceTypePredicate) {
        LinkedList<ParameterConverter> convertersChain = new LinkedList<>();
        putConverters(type, convertersChain, sourceTypePredicate);
        return convertersChain;
    }

    private void putConverters(Type type, LinkedList<ParameterConverter> container,
            Predicate<Type> sourceTypePredicate) {
        for (ParameterConverter converter : converters) {
            if (converter.canConvertTo(type)) {
                container.addFirst(converter);
                Type sourceType = converter.getSourceType();
                if (sourceTypePredicate.test(sourceType)) {
                    break;
                }
                putConverters(sourceType, container, sourceTypePredicate);
            }
        }
    }

    private static boolean isAssignableFrom(Class<?> clazz, Type type) {
        return type instanceof Class<?> && clazz.isAssignableFrom((Class<?>) type);
    }

    private static boolean isAssignableFromRawType(Class<?> clazz, Type type) {
        return type instanceof ParameterizedType && isAssignableFrom(clazz, ((ParameterizedType) type).getRawType());
    }

    private static Class<?> rawClass(Type type) {
        return (Class<?>) ((ParameterizedType) type).getRawType();
    }

    private static Class<?> argumentClass(Type type) {
        if (type instanceof ParameterizedType) {
            Type typeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
            return typeArgument instanceof ParameterizedType ? rawClass(typeArgument) : (Class<?>) typeArgument;
        } else {
            return (Class<?>) type;
        }
    }

    private static Type argumentType(Type type) {
        return ((ParameterizedType) type).getActualTypeArguments()[0];
    }

    private static boolean isAnnotationPresent(Type type, Class<? extends Annotation> annotationClass) {
        if (type instanceof ParameterizedType) {
            return rawClass(type).isAnnotationPresent(annotationClass) || argumentClass(type).isAnnotationPresent(
                    annotationClass);
        }
        return type instanceof Class && ((Class<?>) type).isAnnotationPresent(annotationClass);
    }

    private static String[] parseElements(String value, String elementSeparator) {
        String[] elements = value.trim().isEmpty() ? new String[0] : value.split(elementSeparator);
        Arrays.setAll(elements, i -> elements[i].trim());
        return elements;
    }

    private static void fillCollection(String value, String elementSeparator, Queue<ParameterConverter> convertersChain,
            Type elementType, Collection convertedValues) {
        fillCollection(asList(parseElements(value, elementSeparator)), convertersChain, elementType, convertedValues);
    }

    private static void fillCollection(Collection elements, Queue<ParameterConverter> convertersChain,
            Type elementType, Collection convertedValues) {
        for (Object element : elements) {
            Object convertedValue = applyConverters(element, elementType, convertersChain);
            convertedValues.add(convertedValue);
        }
    }

    private static <T> void fillArray(String[] elements, ParameterConverter<String, T> elementConverter,
            Type elementType, Object convertedValues) {
        for (int i = 0; i < elements.length; i++) {
            T convertedValue = elementConverter.convertValue(elements[i], elementType);
            Array.set(convertedValues, i, convertedValue);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E> Collection<E> createCollection(Class<?> collectionType) {
        if (collectionType.isInterface()) {
            if (Set.class == collectionType) {
                return new HashSet<>();
            } else if (List.class == collectionType) {
                return new ArrayList<>();
            } else if (SortedSet.class == collectionType || NavigableSet.class == collectionType) {
                return new TreeSet<>();
            }
        }
        try {
            return (Collection<E>) collectionType.getConstructor().newInstance();
        } catch (@SuppressWarnings("unused") Throwable t) {
            // Could not instantiate Collection type, swallowing exception quietly
        }
        return null;
    }

    private static Object createArray(Class<?> elementType, int length) {
        try {
            return Array.newInstance(elementType, length);
        } catch (Throwable e) {
            // Could not instantiate array, swallowing exception quietly
        }

        return null;
    }

    public ParameterConverters newInstanceAdding(ParameterConverter converter) {
        List<ParameterConverter> convertersForNewInstance = new ArrayList<>(converters);
        convertersForNewInstance.add(converter);
        return new ParameterConverters(monitor, convertersForNewInstance, threadSafe);
    }

    /**
     * A parameter converter for generic type of source input and target output.
     * The converters can be chained to allow for the target of one converter
     * can be used as the source for another.
     *
     * @param <T> the target converted output
     * @param <S> the source input value
     */
    public interface ParameterConverter<S, T> {

        /**
         * Return {@code true} if the converter can convert to the desired target type.
         * @param type the type descriptor that describes the requested result type
         * @return {@code true} if that conversion can be performed
         */
        boolean canConvertTo(Type type);

        /**
         * Return {@code true} if the converter can convert from the desired target type.
         * @param type the type descriptor that describes the source type
         * @return {@code true} if that conversion can be performed
         */
        boolean canConvertFrom(Type type);

        /**
         * Convert the value from one type to another, for example from a {@code boolean} to a {@code String}.
         * @param value the value to be converted
         * @param type the type descriptor that supplies extra information about the requested result type
         * @return the converted value
         */
        T convertValue(S value, Type type);

        Type getSourceType();

        Type getTargetType();
    }

    @SuppressWarnings("serial")
    public static class ParameterConversionFailed extends RuntimeException {

        public ParameterConversionFailed(String message) {
            super(message);
        }

        public ParameterConversionFailed(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public abstract static class FromStringParameterConverter<T> extends AbstractParameterConverter<String, T> {
        public FromStringParameterConverter() {
        }

        public FromStringParameterConverter(Type targetType) {
            super(String.class, targetType);
        }
    }

    public abstract static class AbstractParameterConverter<S, T> implements ParameterConverter<S, T> {

        private final Type sourceType;
        private final Type targetType;

        public AbstractParameterConverter() {
            Map<TypeVariable<?>, Type> types = TypeUtils.getTypeArguments(getClass(), ParameterConverter.class);
            TypeVariable<?>[] typeVariables = ParameterConverter.class.getTypeParameters();
            this.sourceType = types.get(typeVariables[0]);
            this.targetType = types.get(typeVariables[1]);
        }

        public AbstractParameterConverter(Type sourceType, Type targetType) {
            this.sourceType = sourceType;
            this.targetType = targetType;
        }

        @Override
        public boolean canConvertTo(Type type) {
            return isAssignable(targetType, type);
        }

        @Override
        public boolean canConvertFrom(Type type) {
            return isAssignable(sourceType, type);
        }

        public Type getSourceType() {
            return sourceType;
        }

        public Type getTargetType() {
            return targetType;
        }

        private static boolean isAssignable(Type from, Type to) {
            if (from instanceof Class<?>) {
                return isAssignableFrom((Class<?>) from, to);
            }
            return from.equals(to);
        }
    }

    public static class FunctionalParameterConverter<S, T> extends AbstractParameterConverter<S, T> {

        private final Function<S, T> converterFunction;

        public FunctionalParameterConverter(Class<S> sourceType, Class<T> targetType,
                Function<S, T> converterFunction) {
            super(sourceType, targetType);
            this.converterFunction = converterFunction;
        }

        protected FunctionalParameterConverter(Function<S, T> converterFunction) {
            this.converterFunction = converterFunction;
        }

        @Override
        public T convertValue(S value, Type type) {
            return converterFunction.apply(value);
        }
    }

    public abstract static class AbstractListParameterConverter<T> extends FromStringParameterConverter<List<T>> {

        private final String valueSeparator;
        private final Queue<ParameterConverter> elementConverters;

        public AbstractListParameterConverter(String valueSeparator, FromStringParameterConverter<T> elementConverter) {
            this.valueSeparator = valueSeparator;
            this.elementConverters = new LinkedList<>();
            this.elementConverters.add(elementConverter);
        }

        @Override
        public boolean canConvertTo(Type type) {
            return isAssignableFromRawType(List.class, type) && elementConverters.peek().canConvertTo(
                    argumentType(type));
        }

        @Override
        public List<T> convertValue(String value, Type type) {
            Type elementType = argumentType(type);
            List<T> convertedValues = new ArrayList<>();
            fillCollection(value, valueSeparator, elementConverters, elementType, convertedValues);
            return convertedValues;
        }
    }

    /**
     * Converts values to numbers, supporting any subclass of {@link Number} (including generic Number type), and it
     * unboxed counterpart, using a {@link NumberFormat} to parse to a {@link Number} and to convert it to a specific
     * number type:
     * <ul>
     * <li>Byte, byte: {@link Number#byteValue()}</li>
     * <li>Short, short: {@link Number#shortValue()}</li>
     * <li>Integer, int: {@link Number#intValue()}</li>
     * <li>Float, float: {@link Number#floatValue()}</li>
     * <li>Long, long: {@link Number#longValue()}</li>
     * <li>Double, double: {@link Number#doubleValue()}</li>
     * <li>BigInteger: {@link BigInteger#valueOf(long)}</li>
     * <li>BigDecimal: {@link BigDecimal#valueOf(double)}</li>
     * </ul>
     * If no number format is provided, it defaults to {@link NumberFormat#getInstance()}.
     * <p>The localized instance {@link NumberFormat#getInstance(Locale)} can be used to convert numbers in specific
     * locales.</p>
     */
    public static class NumberConverter extends FromStringParameterConverter<Number> {
        private static List<Class<?>> primitiveTypes = asList(byte.class, short.class, int.class, float.class,
                long.class, double.class);

        private final NumberFormat numberFormat;
        private ThreadLocal<NumberFormat> threadLocalNumberFormat = new ThreadLocal<>();

        public NumberConverter() {
            this(NumberFormat.getInstance(DEFAULT_NUMBER_FORMAT_LOCAL));
        }

        public NumberConverter(NumberFormat numberFormat) {
            synchronized (this) {
                this.numberFormat = numberFormat;
                this.threadLocalNumberFormat.set((NumberFormat) this.numberFormat.clone());
            }
        }

        @Override
        public boolean canConvertTo(Type type) {
            return super.canConvertTo(type) || primitiveTypes.contains(type);
        }

        @Override
        public Number convertValue(String value, Type type) {
            try {
                Number n = numberFormat().parse(value);
                if (type == Byte.class || type == byte.class) {
                    return n.byteValue();
                } else if (type == Short.class || type == short.class) {
                    return n.shortValue();
                } else if (type == Integer.class || type == int.class) {
                    return n.intValue();
                } else if (type == Float.class || type == float.class) {
                    return n.floatValue();
                } else if (type == Long.class || type == long.class) {
                    return n.longValue();
                } else if (type == Double.class || type == double.class) {
                    return n.doubleValue();
                } else if (type == BigInteger.class) {
                    return BigInteger.valueOf(n.longValue());
                } else if (type == BigDecimal.class) {
                    return new BigDecimal(canonicalize(value));
                } else if (type == AtomicInteger.class) {
                    return new AtomicInteger(Integer.parseInt(value));
                } else if (type == AtomicLong.class) {
                    return new AtomicLong(Long.parseLong(value));
                } else {
                    return n;
                }
            } catch (NumberFormatException | ParseException e) {
                throw new ParameterConversionFailed(value, e);
            }
        }

        /**
         * Return NumberFormat instance with preferred locale threadsafe
         *
         * @return A threadlocal version of original NumberFormat instance
         */
        private NumberFormat numberFormat() {
            if (threadLocalNumberFormat.get() == null) {
                synchronized (this) {
                    threadLocalNumberFormat.set((NumberFormat) numberFormat.clone());
                }
            }
            return threadLocalNumberFormat.get();
        }

        /**
         * Canonicalize a number representation to a format suitable for the {@link BigDecimal#BigDecimal(String)}
         * constructor, taking into account the settings of the currently configured DecimalFormat.
         *
         * @param value a localized number value
         * @return A canonicalized string value suitable for consumption by BigDecimal
         */
        private String canonicalize(String value) {
            char decimalPointSeparator = '.'; // default
            char minusSign = '-'; // default
            String rxNotDigits = "[\\.,]";
            StringBuilder builder = new StringBuilder(value.length());

            // override defaults according to numberFormat's settings
            if (numberFormat() instanceof DecimalFormat) {
                DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) numberFormat()).getDecimalFormatSymbols();
                minusSign = decimalFormatSymbols.getMinusSign();
                decimalPointSeparator = decimalFormatSymbols.getDecimalSeparator();
            }

            value = value.trim();
            int decimalPointPosition = value.lastIndexOf(decimalPointSeparator);
            int firstDecimalPointPosition = value.indexOf(decimalPointSeparator);

            if (firstDecimalPointPosition != decimalPointPosition) {
                throw new NumberFormatException("Invalid format, more than one decimal point has been found.");
            }

            if (decimalPointPosition != -1) {
                String sf = value.substring(0, decimalPointPosition).replaceAll(rxNotDigits, "");
                String dp = value.substring(decimalPointPosition + 1).replaceAll(rxNotDigits, "");

                builder.append(sf);
                builder.append('.'); // fixed "." for BigDecimal constructor
                builder.append(dp);

            } else {
                builder.append(value.replaceAll(rxNotDigits, ""));
            }

            boolean isNegative = value.charAt(0) == minusSign;

            if (isNegative) {
                builder.setCharAt(0, '-'); // fixed "-" for BigDecimal constructor
            }
            return builder.toString();
        }
    }

    /**
     * Converts value to list of numbers. Splits value to a list, using an
     * injectable value separator (defaulting to ",") and converts each element
     * of list via the {@link NumberConverter}, using the {@link NumberFormat}
     * provided (defaulting to {@link NumberFormat#getInstance()}
     * ).
     */
    public static class NumberListConverter extends AbstractListParameterConverter<Number> {

        public NumberListConverter() {
            this(NumberFormat.getInstance(DEFAULT_NUMBER_FORMAT_LOCAL), DEFAULT_COLLECTION_SEPARATOR);
        }

        /**
         * Creates converter for a list of numbers using the specified number format and value separator
         *
         * @param numberFormat   Specific NumberFormat to use.
         * @param valueSeparator A regexp to use as list separate
         */
        public NumberListConverter(NumberFormat numberFormat, String valueSeparator) {
            super(valueSeparator, new NumberConverter(numberFormat));
        }
    }

    public static class StringConverter extends FromStringParameterConverter<String> {
        private static final String NEWLINES_PATTERN = "(\n)|(\r\n)";
        private static final String SYSTEM_NEWLINE = System.getProperty("line.separator");

        @Override
        public String convertValue(String value, Type type) {
            return value != null ? value.replaceAll(NEWLINES_PATTERN, SYSTEM_NEWLINE) : null;
        }
    }

    /**
     * Converts value to list of String. Splits value to a list, using an
     * injectable value separator (defaults to ",") and trimming each element of
     * the list.
     */
    public static class StringListConverter extends AbstractListParameterConverter<String> {

        public StringListConverter() {
            this(DEFAULT_COLLECTION_SEPARATOR);
        }

        /**
         * Creates converter for a list of strings using the value separator
         *
         * @param valueSeparator A regexp to use as list separator
         */
        public StringListConverter(String valueSeparator) {
            super(valueSeparator, new StringConverter());
        }

        @Override
        public List<String> convertValue(String value, Type type) {
            if (value.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return super.convertValue(value, type);
        }
    }

    /**
     * Parses value to a {@link Date} using an injectable {@link DateFormat}
     * (defaults to <b>new SimpleDateFormat("dd/MM/yyyy")</b>)
     */
    public static class DateConverter extends FromStringParameterConverter<Date> {

        public static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

        private final DateFormat dateFormat;

        public DateConverter() {
            this(DEFAULT_FORMAT);
        }

        public DateConverter(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        @Override
        public Date convertValue(String value, Type type) {
            try {
                return dateFormat.parse(value);
            } catch (ParseException e) {
                throw new ParameterConversionFailed("Failed to convert value " + value + " with date format "
                        + (dateFormat instanceof SimpleDateFormat ? ((SimpleDateFormat) dateFormat).toPattern()
                                : dateFormat), e);
            }
        }
    }

    public static class BooleanConverter extends FromStringParameterConverter<Boolean> {
        private final String trueValue;
        private final String falseValue;

        public BooleanConverter() {
            this(DEFAULT_TRUE_VALUE, DEFAULT_FALSE_VALUE);
        }

        public BooleanConverter(String trueValue, String falseValue) {
            this.trueValue = trueValue;
            this.falseValue = falseValue;
        }

        @Override
        public boolean canConvertTo(Type type) {
            return super.canConvertTo(type) || isAssignableFrom(Boolean.TYPE, type);
        }

        @Override
        public Boolean convertValue(String value, Type type) {
            try {
                return BooleanUtils.toBoolean(value, trueValue, falseValue);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    public static class BooleanListConverter extends AbstractListParameterConverter<Boolean> {

        public BooleanListConverter() {
            this(DEFAULT_COLLECTION_SEPARATOR, DEFAULT_TRUE_VALUE, DEFAULT_FALSE_VALUE);
        }

        public BooleanListConverter(String valueSeparator) {
            this(valueSeparator, DEFAULT_TRUE_VALUE, DEFAULT_FALSE_VALUE);
        }

        public BooleanListConverter(String valueSeparator, String trueValue, String falseValue) {
            super(valueSeparator, new BooleanConverter(trueValue, falseValue));
        }
    }

    /**
     * Parses value to any {@link Enum}
     */
    public static class EnumConverter extends FromStringParameterConverter<Enum<?>> {

        @Override
        public boolean canConvertTo(Type type) {
            return type instanceof Class<?> && ((Class<?>) type).isEnum();
        }

        @Override
        public Enum<?> convertValue(String value, Type type) {
            String typeClass = ((Class<?>) type).getName();
            Class<?> enumClass = (Class<?>) type;
            Method valueOfMethod = null;
            try {
                valueOfMethod = enumClass.getMethod("valueOf", String.class);
                valueOfMethod.setAccessible(true);
                return (Enum<?>) valueOfMethod.invoke(enumClass, new Object[] { value });
            } catch (Exception e) {
                throw new ParameterConversionFailed("Failed to convert " + value + " for Enum " + typeClass, e);
            }
        }
    }

    /**
     * <p>An {@link EnumConverter} allowing stories prose to be more natural. Before performing the actual conversion,
     * it ransforms values to upper-case, with any non-alphanumeric character replaced by an underscore ('_').</p>
     * <p><b>Example</b>: assuming we have defined the step "{@code Given I am on the $page}" which is mapped to the
     * method {@code iAmOnPage(PageEnum page)}, we can then write in a scenario:
     * <pre>{@code
     * Given I am on the login page
     * }</pre>
     * instead of:
     * <pre>{@code
     * Given I am on the LOGIN_PAGE
     * }</pre>
     * </p>
     * <p><b>Warning</b>. This <i>requires</i> enum constants to follow the
     * <a href="https://google-styleguide.googlecode.com/svn/trunk/javaguide.html#s5.2.4-constant-names">
     * standard conventions for constant names</a>, i.e. all uppercase letters, with words separated by underscores.</p>
     */
    public static class FluentEnumConverter extends EnumConverter {

        @Override
        public Enum<?> convertValue(String value, Type type) {
            return super.convertValue(value.replaceAll("\\W", "_").toUpperCase(), type);
        }
    }

    /**
     * Parses value to list of the same {@link Enum}, using an injectable value
     * separator (defaults to ",") and trimming each element of the list.
     */
    public static class EnumListConverter extends AbstractListParameterConverter<Enum<?>> {

        public EnumListConverter() {
            this(DEFAULT_COLLECTION_SEPARATOR);
        }

        public EnumListConverter(String valueSeparator) {
            super(valueSeparator, new EnumConverter());
        }
    }

    /**
     * Converts value to {@link ExamplesTable} using a
     * {@link ExamplesTableFactory}.
     */
    public static class ExamplesTableConverter extends FunctionalParameterConverter<String, ExamplesTable> {

        public ExamplesTableConverter(ExamplesTableFactory factory) {
            super(factory::createExamplesTable);
        }
    }

    /**
     * Converts ExamplesTable to list of parameters, mapped to annotated custom
     * types.
     */
    public static class ExamplesTableParametersConverter extends FromStringParameterConverter<Object> {

        private final ExamplesTableFactory factory;

        public ExamplesTableParametersConverter(ExamplesTableFactory factory) {
            this.factory = factory;
        }

        @Override
        public boolean canConvertTo(Type type) {
            return isExamplesTableParameters(type);
        }

        @Override
        public Object convertValue(String value, Type type) {
            List<?> rows = factory.createExamplesTable(value).getRowsAs(argumentClass(type));
            if (isAssignableFromRawType(List.class, type)) {
                return rows;
            }
            int rowCount = rows.size();
            isTrue(rowCount == 1,
                    "Exactly one row is expected in ExamplesTable in order to convert it to %s, but found %d row(s)",
                    type, rowCount);
            return rows.get(0);
        }

        public static boolean isExamplesTableParameters(Type type) {
            return isAnnotationPresent(type, AsParameters.class);
        }

    }

    public static class JsonConverter extends FromStringParameterConverter<Object> {

        private final JsonFactory factory;

        public JsonConverter() {
            this(new JsonFactory());
        }

        public JsonConverter(final JsonFactory factory) {
            this.factory = factory;
        }

        @Override
        public boolean canConvertTo(final Type type) {
            return isAnnotationPresent(type, AsJson.class);
        }

        @Override
        public Object convertValue(final String value, final Type type) {
            return factory.createJson(value, type);
        }
    }

    public static class JsonFactory {

        private Keywords keywords;
        private final ResourceLoader resourceLoader;

        public JsonFactory() {
            this(new LocalizedKeywords());
        }

        public JsonFactory(final Keywords keywords) {
            this(keywords, new LoadFromClasspath());
        }

        public JsonFactory(final ResourceLoader resourceLoader) {
            this(new LocalizedKeywords(), resourceLoader);
        }

        public JsonFactory(final Keywords keywords, final ResourceLoader resourceLoader) {
            this.keywords = keywords;
            this.resourceLoader = resourceLoader;
        }

        public JsonFactory(final Configuration configuration) {
            this.keywords = configuration.keywords();
            this.resourceLoader = configuration.storyLoader();
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

    /**
     * Invokes method on instance to return value.
     */
    public static class MethodReturningConverter extends FromStringParameterConverter<Object> {
        private Method method;
        private Class<?> stepsType;
        private InjectableStepsFactory stepsFactory;

        public MethodReturningConverter(Method method, Object instance) {
            this(method, instance.getClass(), new InstanceStepsFactory(new MostUsefulConfiguration(), instance));
        }

        public MethodReturningConverter(Method method, Class<?> stepsType, InjectableStepsFactory stepsFactory) {
            this.method = method;
            this.stepsType = stepsType;
            this.stepsFactory = stepsFactory;
        }

        @Override
        public boolean canConvertTo(Type type) {
            return isAssignableFrom(method.getReturnType(), type);
        }

        @Override
        public Object convertValue(String value, Type type) {
            try {
                Object instance = instance();
                return method.invoke(instance, value);
            } catch (Exception e) {
                throw new ParameterConversionFailed("Failed to invoke method " + method + " with value " + value
                        + " in " + type, e);
            }
        }

        private Object instance() {
            return stepsFactory.createInstanceOfType(stepsType);
        }

    }

}
