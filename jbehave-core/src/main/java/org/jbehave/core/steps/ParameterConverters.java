package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.BooleanUtils;
import org.jbehave.core.annotations.AsJson;
import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.io.LoadFromClasspath;
import org.jbehave.core.io.ResourceLoader;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.JsonFactory;
import org.jbehave.core.model.TableTransformers;

import static java.util.Arrays.asList;

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
 * <li>{@link ParameterConverters.StringListConverter StringListConverter}</li>
 * <li>{@link ParameterConverters.DateConverter DateConverter}</li>
 * <li>{@link ParameterConverters.ExamplesTableConverter ExamplesTableConverter}
 * </li>
 * <li>{@link ParameterConverters.ExamplesTableParametersConverter
 * ExamplesTableParametersConverter}</li>
 * <li>{@link ParameterConverters.MethodReturningConverter
 * MethodReturningConverter}</li>
 * </ul>
 * </p>
 */
public class ParameterConverters {

    public static final StepMonitor DEFAULT_STEP_MONITOR = new SilentStepMonitor();
    public static final Locale DEFAULT_NUMBER_FORMAT_LOCAL = Locale.ENGLISH;
    public static final String DEFAULT_LIST_SEPARATOR = ",";
    public static final boolean DEFAULT_THREAD_SAFETY = true;

    private static final String NEWLINES_PATTERN = "(\n)|(\r\n)";
    private static final String SYSTEM_NEWLINE = System.getProperty("line.separator");
    private static final String DEFAULT_TRUE_VALUE = "true";
    private static final String DEFAULT_FALSE_VALUE = "false";

    private final StepMonitor monitor;
    private final List<ParameterConverter> converters;
    private final boolean threadSafe;


    /**
     * Creates a non-thread-safe instance of ParameterConverters using the default table transformers,
     * default dependencies, a SilentStepMonitor, English as Locale and "," as list
     * separator.
     */
    public ParameterConverters() {
        this(new LoadFromClasspath(), new TableTransformers());
    }

    /**
     * Creates a non-thread-safe instance of ParameterConverters using default
     * dependencies, a SilentStepMonitor, English as Locale and "," as list
     * separator.
     * 
     * @param resourceLoader the resource loader
     * @param tableTransformers the table transformers
     */
    public ParameterConverters(ResourceLoader resourceLoader, TableTransformers tableTransformers) {
        this(DEFAULT_STEP_MONITOR, resourceLoader, tableTransformers);
    }

    /**
     * Creates a ParameterConverters using given StepMonitor
     * 
     * @param monitor the StepMonitor to use
     * @param resourceLoader the resource loader
     * @param tableTransformers the table transformers
     */
    public ParameterConverters(StepMonitor monitor, ResourceLoader resourceLoader,
            TableTransformers tableTransformers) {
        this(monitor, resourceLoader, tableTransformers, DEFAULT_NUMBER_FORMAT_LOCAL, DEFAULT_LIST_SEPARATOR,
                DEFAULT_THREAD_SAFETY);
    }

    /**
     * Create a ParameterConverters with given thread-safety
     * 
     * @param resourceLoader the resource loader
     * @param tableTransformers the table transformers
     * @param threadSafe the boolean flag to determine if access to
     * {@link ParameterConverter} should be thread-safe
     */
    public ParameterConverters(ResourceLoader resourceLoader, TableTransformers tableTransformers, boolean threadSafe) {
        this(DEFAULT_STEP_MONITOR, resourceLoader, tableTransformers, DEFAULT_NUMBER_FORMAT_LOCAL,
                DEFAULT_LIST_SEPARATOR, threadSafe);
    }

    /**
     * Creates a ParameterConverters for the given StepMonitor, Locale, list
     * separator and thread-safety. When selecting a listSeparator, please make
     * sure that this character doesn't have a special meaning in your Locale
     * (for instance "," is used as decimal separator in some Locale)
     * 
     * @param monitor the StepMonitor reporting the conversions
     * @param resourceLoader the resource loader
     * @param tableTransformers the table transformers
     * @param locale the Locale to use when reading numbers
     * @param listSeparator the String to use as list separator
     * @param threadSafe the boolean flag to determine if modification of
     * {@link ParameterConverter} should be thread-safe
     */
    public ParameterConverters(StepMonitor monitor, ResourceLoader resourceLoader, TableTransformers tableTransformers,
            Locale locale, String listSeparator, boolean threadSafe) {
        this(monitor, new ArrayList<ParameterConverter>(), threadSafe);
        this.addConverters(defaultConverters(resourceLoader, tableTransformers, locale, listSeparator));
    }

    private ParameterConverters(StepMonitor monitor, List<ParameterConverter> converters, boolean threadSafe) {
        this.monitor = monitor;
        this.threadSafe = threadSafe;
        this.converters = threadSafe ? new CopyOnWriteArrayList<ParameterConverter>(converters)
                : new ArrayList<ParameterConverter>(converters);
    }

    protected ParameterConverter[] defaultConverters(ResourceLoader resourceLoader, TableTransformers tableTransformers,
            Locale locale, String listSeparator) {
        String escapedListSeparator = escapeRegexPunctuation(listSeparator);
        ExamplesTableFactory tableFactory = new ExamplesTableFactory(resourceLoader, this, tableTransformers);
        JsonFactory jsonFactory = new JsonFactory();
        return new ParameterConverter[] { new BooleanConverter(),
                new NumberConverter(NumberFormat.getInstance(locale)),
                new NumberListConverter(NumberFormat.getInstance(locale), escapedListSeparator),
                new StringListConverter(escapedListSeparator), new DateConverter(), new EnumConverter(),
                new EnumListConverter(), new ExamplesTableConverter(tableFactory),
                new ExamplesTableParametersConverter(tableFactory), new JsonConverter(jsonFactory) };
    }

    // TODO : This is a duplicate from RegExpPrefixCapturing
    private String escapeRegexPunctuation(String matchThis) {
        return matchThis.replaceAll("([\\[\\]\\{\\}\\?\\^\\.\\*\\(\\)\\+\\\\])", "\\\\$1");
    }

    public ParameterConverters addConverters(ParameterConverter... converters) {
        return addConverters(asList(converters));
    }

    public ParameterConverters addConverters(List<ParameterConverter> converters) {
        this.converters.addAll(0, converters);
        return this;
    }

    public Object convert(String value, Type type) {

        // check if any converters accepts type
        for (ParameterConverter converter : converters) {
            if (converter.accept(type)) {
                Object converted = converter.convertValue(value, type);
                monitor.convertedValueOfType(value, type, converted, converter.getClass());
                return converted;
            }
        }

        if (type == String.class) {
            return replaceNewlinesWithSystemNewlines(value);
        }

        throw new ParameterConvertionFailed("No parameter converter for " + type);
    }

    private Object replaceNewlinesWithSystemNewlines(String value) {
        return value.replaceAll(NEWLINES_PATTERN, SYSTEM_NEWLINE);
    }

    public ParameterConverters newInstanceAdding(ParameterConverter converter) {
        List<ParameterConverter> convertersForNewInstance = new ArrayList<ParameterConverter>(converters);
        convertersForNewInstance.add(converter);
        return new ParameterConverters(monitor, convertersForNewInstance, threadSafe);
    }

    public static interface ParameterConverter {

        boolean accept(Type type);

        Object convertValue(String value, Type type);

    }

    @SuppressWarnings("serial")
    public static class ParameterConvertionFailed extends RuntimeException {

        public ParameterConvertionFailed(String message) {
            super(message);
        }

        public ParameterConvertionFailed(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * <p>
     * Converts values to numbers, supporting any subclass of {@link Number}
     * (including generic Number type), and it unboxed counterpart, using a
     * {@link NumberFormat} to parse to a {@link Number} and to convert it to a
     * specific number type:
     * <ul>
     * <li>Byte, byte: {@link Number#byteValue()}</li>
     * <li>Short, short: {@link Number#shortValue()}</li>
     * <li>Integer, int: {@link Number#intValue()}</li>
     * <li>Float, float: {@link Number#floatValue()}</li>
     * <li>Long, long: {@link Number#longValue()}</li>
     * <li>Double, double: {@link Number#doubleValue()}</li>
     * <li>BigInteger: {@link BigInteger#valueOf(Long)}</li>
     * <li>BigDecimal: {@link BigDecimal#valueOf(Double)}</li>
     * </ul>
     * If no number format is provided, it defaults to
     * {@link NumberFormat#getInstance(Locale.ENGLISH)}.
     * <p>
     * The localized instance {@link NumberFormat#getInstance(Locale)} can be
     * used to convert numbers in specific locales.
     * </p>
     */
    public static class NumberConverter implements ParameterConverter {
        private static List<Class<?>> primitiveTypes = asList(new Class<?>[] { byte.class, short.class, int.class,
                float.class, long.class, double.class });

        private final NumberFormat numberFormat;
        private ThreadLocal<NumberFormat> threadLocalNumberFormat = new ThreadLocal<NumberFormat>();

        public NumberConverter() {
            this(NumberFormat.getInstance(DEFAULT_NUMBER_FORMAT_LOCAL));
        }

        public NumberConverter(NumberFormat numberFormat) {
            synchronized (this) {
                this.numberFormat = numberFormat;
                this.threadLocalNumberFormat.set((NumberFormat) this.numberFormat.clone());
            }
        }

        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return Number.class.isAssignableFrom((Class<?>) type) || primitiveTypes.contains(type);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
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
            } catch (NumberFormatException e) {
                throw new ParameterConvertionFailed(value, e);
            } catch (ParseException e) {
                throw new ParameterConvertionFailed(value, e);
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
         * Canonicalize a number representation to a format suitable for the
         * {@link BigDecimal(String)} constructor, taking into account the
         * settings of the currently configured DecimalFormat.
         * 
         * @param value a localized number value
         * @return A canonicalized string value suitable for consumption by
         * BigDecimal
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
     * provided (defaulting to {@link NumberFormat#getInstance(Locale.ENGLISH)}
     * ).
     */
    public static class NumberListConverter implements ParameterConverter {

        private final NumberConverter numberConverter;
        private final String valueSeparator;

        public NumberListConverter() {
            this(NumberFormat.getInstance(DEFAULT_NUMBER_FORMAT_LOCAL), DEFAULT_LIST_SEPARATOR);
        }

        /**
         * @param numberFormat Specific NumberFormat to use.
         * @param valueSeparator A regexp to use as list separate
         */
        public NumberListConverter(NumberFormat numberFormat, String valueSeparator) {
            this.numberConverter = new NumberConverter(numberFormat);
            this.valueSeparator = valueSeparator;
        }

        public boolean accept(Type type) {
            if (type instanceof ParameterizedType) {
                Type rawType = rawType(type);
                Type argumentType = argumentType(type);
                return List.class.isAssignableFrom((Class<?>) rawType)
                        && Number.class.isAssignableFrom((Class<?>) argumentType);
            }
            return false;
        }

        private Type rawType(Type type) {
            return ((ParameterizedType) type).getRawType();
        }

        private Type argumentType(Type type) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        @SuppressWarnings("unchecked")
        public Object convertValue(String value, Type type) {
            Class<? extends Number> argumentType = (Class<? extends Number>) argumentType(type);
            List<String> values = trim(asList(value.split(valueSeparator)));
            List<Number> numbers = new ArrayList<Number>();
            for (String numberValue : values) {
                numbers.add((Number) numberConverter.convertValue(numberValue, argumentType));
            }
            return numbers;
        }

    }

    /**
     * Converts value to list of String. Splits value to a list, using an
     * injectable value separator (defaults to ",") and trimming each element of
     * the list.
     */
    public static class StringListConverter implements ParameterConverter {

        private String valueSeparator;

        public StringListConverter() {
            this(DEFAULT_LIST_SEPARATOR);
        }

        /**
         * @param valueSeparator A regexp to use as list separate
         */
        public StringListConverter(String valueSeparator) {
            this.valueSeparator = valueSeparator;
        }

        public boolean accept(Type type) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                Type argumentType = parameterizedType.getActualTypeArguments()[0];
                return List.class.isAssignableFrom((Class<?>) rawType)
                        && String.class.isAssignableFrom((Class<?>) argumentType);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            if (value.trim().length() == 0)
                return asList();
            return trim(asList(value.split(valueSeparator)));
        }

    }

    public static List<String> trim(List<String> values) {
        List<String> trimmed = new ArrayList<String>();
        for (String value : values) {
            trimmed.add(value.trim());
        }
        return trimmed;
    }

    /**
     * Parses value to a {@link Date} using an injectable {@link DateFormat}
     * (defaults to <b>new SimpleDateFormat("dd/MM/yyyy")</b>)
     */
    public static class DateConverter implements ParameterConverter {

        public static final DateFormat DEFAULT_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

        private final DateFormat dateFormat;

        public DateConverter() {
            this(DEFAULT_FORMAT);
        }

        public DateConverter(DateFormat dateFormat) {
            this.dateFormat = dateFormat;
        }

        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return Date.class.isAssignableFrom((Class<?>) type);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            try {
                return dateFormat.parse(value);
            } catch (ParseException e) {
                throw new ParameterConvertionFailed("Failed to convert value "
                        + value
                        + " with date format "
                        + (dateFormat instanceof SimpleDateFormat ? ((SimpleDateFormat) dateFormat).toPattern()
                                : dateFormat), e);
            }
        }

    }

    public static class BooleanConverter implements ParameterConverter {
        private String trueValue;
        private String falseValue;

        public BooleanConverter() {
            this(DEFAULT_TRUE_VALUE, DEFAULT_FALSE_VALUE);
        }

        public BooleanConverter(String trueValue, String falseValue) {
            this.trueValue = trueValue;
            this.falseValue = falseValue;
        }

        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return Boolean.class.isAssignableFrom((Class<?>) type) || Boolean.TYPE.isAssignableFrom((Class<?>) type);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            try {
                return BooleanUtils.toBoolean(value, trueValue, falseValue);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    }

    public static class BooleanListConverter implements ParameterConverter {
        private final BooleanConverter booleanConverter;
        private String valueSeparator;

        public BooleanListConverter() {
            this(DEFAULT_LIST_SEPARATOR, DEFAULT_TRUE_VALUE, DEFAULT_FALSE_VALUE);
        }

        public BooleanListConverter(String valueSeparator) {
            this(valueSeparator, DEFAULT_TRUE_VALUE, DEFAULT_FALSE_VALUE);
        }

        public BooleanListConverter(String valueSeparator, String trueValue, String falseValue) {
            this.valueSeparator = valueSeparator;
            booleanConverter = new BooleanConverter(trueValue, falseValue);
        }

        public boolean accept(Type type) {
            if (type instanceof ParameterizedType) {
                Type rawType = rawType(type);
                Type argumentType = argumentType(type);
                return List.class.isAssignableFrom((Class<?>) rawType)
                        && Boolean.class.isAssignableFrom((Class<?>) argumentType);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            List<String> values = trim(asList(value.split(valueSeparator)));
            List<Boolean> booleans = new ArrayList<Boolean>();

            for (String booleanValue : values) {
                booleans.add((Boolean) booleanConverter.convertValue(booleanValue, type));
            }
            return booleans;
        }

        private Type rawType(Type type) {
            return ((ParameterizedType) type).getRawType();
        }

        private Type argumentType(Type type) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        }
    }

    /**
     * Parses value to any {@link Enum}
     */
    public static class EnumConverter implements ParameterConverter {

        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return ((Class<?>) type).isEnum();
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            String typeClass = ((Class<?>) type).getName();
            Class<?> enumClass = (Class<?>) type;
            Method valueOfMethod = null;
            try {
                valueOfMethod = enumClass.getMethod("valueOf", new Class[] { String.class });
                valueOfMethod.setAccessible(true);
                return valueOfMethod.invoke(enumClass, new Object[] { value });
            } catch (Exception e) {
                throw new ParameterConvertionFailed("Failed to convert " + value + " for Enum " + typeClass, e);
            }
        }
    }

    /**
     * An {@link EnumConverter} allowing stories prose to be more natural.
     * Before performing the actual conversion, it transforms values to upper-case,
     * with any non-alphanumeric character replaced by an underscore ('_').
     * <p>
     * <b>Example</b>:
     * assuming we have defined the step "{@code Given I am on the $page}"
     * which is mapped to the method {@code iAmOnPage(PageEnum page)},
     * we can then write in a scenario:
     * <pre>{@code
     * Given I am on the login page
     * }</pre>
     * instead of:
     * <pre>{@code
     * Given I am on the LOGIN_PAGE
     * }</pre>
     * <p>
     * <b>Warning</b>. This <i>requires</i> enum constants to follow the
     * <a href="https://google-styleguide.googlecode.com/svn/trunk/javaguide.html#s5.2.4-constant-names">
     * standard conventions for constant names</a>, i.e. all uppercase letters,
     * with words separated by underscores.
     */
    public static class FluentEnumConverter extends EnumConverter {

        @Override
        public Object convertValue(String value, Type type) {
            return super.convertValue(value.replaceAll("\\W", "_").toUpperCase(), type);
        }
    }

    /**
     * Parses value to list of the same {@link Enum}, using an injectable value
     * separator (defaults to ",") and trimming each element of the list.
     */
    public static class EnumListConverter implements ParameterConverter {
        private final EnumConverter enumConverter;
        private String valueSeparator;

        public EnumListConverter() {
            this(DEFAULT_LIST_SEPARATOR);
        }

        public EnumListConverter(String valueSeparator) {
            this.enumConverter = new EnumConverter();
            this.valueSeparator = valueSeparator;
        }

        public boolean accept(Type type) {
            if (type instanceof ParameterizedType) {
                Type rawType = rawType(type);
                Type argumentType = argumentType(type);
                return List.class.isAssignableFrom((Class<?>) rawType) && enumConverter.accept(argumentType);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            Type argumentType = argumentType(type);
            List<String> values = trim(asList(value.split(valueSeparator)));
            List<Enum<?>> enums = new ArrayList<Enum<?>>();
            for (String string : values) {
                enums.add((Enum<?>) enumConverter.convertValue(string, argumentType));
            }
            return enums;
        }

        private Type rawType(Type type) {
            return ((ParameterizedType) type).getRawType();
        }

        private Type argumentType(Type type) {
            return ((ParameterizedType) type).getActualTypeArguments()[0];
        }
    }

    /**
     * Converts value to {@link ExamplesTable} using a
     * {@link ExamplesTableFactory}.
     */
    public static class ExamplesTableConverter implements ParameterConverter {

        private final ExamplesTableFactory factory;

        public ExamplesTableConverter(ExamplesTableFactory factory) {
            this.factory = factory;
        }

        @Override
        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return ExamplesTable.class.isAssignableFrom((Class<?>) type);
            }
            return false;
        }

        @Override
        public Object convertValue(String value, Type type) {
            return factory.createExamplesTable(value);
        }

    }

    /**
     * Converts ExamplesTable to list of parameters, mapped to annotated custom
     * types.
     */
    public static class ExamplesTableParametersConverter implements ParameterConverter {

        private final ExamplesTableFactory factory;

        public ExamplesTableParametersConverter(ExamplesTableFactory factory) {
            this.factory = factory;
        }

        @Override
        public boolean accept(Type type) {
            if (type instanceof ParameterizedType) {
                Class<?> rawClass = rawClass(type);
                Class<?> argumentClass = argumentClass(type);
                if (rawClass.isAnnotationPresent(AsParameters.class)
                        || argumentClass.isAnnotationPresent(AsParameters.class)) {
                    return true;
                }
            } else if (type instanceof Class) {
                return ((Class<?>) type).isAnnotationPresent(AsParameters.class);
            }
            return false;
        }

        private Class<?> rawClass(Type type) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }

        private Class<?> argumentClass(Type type) {
            if (type instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                return (Class<?>) type;
            }
        }

        @Override
        public Object convertValue(String value, Type type) {
            List<?> rows = factory.createExamplesTable(value).getRowsAs(argumentClass(type));
            if (type instanceof ParameterizedType) {
                return rows;
            }
            return rows.iterator().next();
        }

    }

    public static class JsonConverter implements ParameterConverter {

        private final JsonFactory factory;

        public JsonConverter() {
            this(new JsonFactory());
        }

        public JsonConverter(final JsonFactory factory) {
            this.factory = factory;
        }

        public boolean accept(final Type type) {
            if (type instanceof ParameterizedType) {
                Class<?> rawClass = rawClass(type);
                Class<?> argumentClass = argumentClass(type);
                if (rawClass.isAnnotationPresent(AsJson.class) || argumentClass.isAnnotationPresent(AsJson.class)) {
                    return true;
                }
            } else if (type instanceof Class) {
                return ((Class<?>) type).isAnnotationPresent(AsJson.class);
            }
            return false;
        }

        public Object convertValue(final String value, final Type type) {
            return factory.createJson(value, type);
        }

        private Class<?> rawClass(final Type type) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        }

        private Class<?> argumentClass(final Type type) {
            if (type instanceof ParameterizedType) {
                return (Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0];
            } else {
                return (Class<?>) type;
            }
        }

    }

    /**
     * Invokes method on instance to return value.
     */
    public static class MethodReturningConverter implements ParameterConverter {
        private Method method;
        private Class<?> stepsType;
        private InjectableStepsFactory stepsFactory;

        public MethodReturningConverter(Method method, Object instance) {
            this.method = method;
            this.stepsType = instance.getClass();
            this.stepsFactory = new InstanceStepsFactory(new MostUsefulConfiguration(), instance);
        }

        public MethodReturningConverter(Method method, Class<?> stepsType, InjectableStepsFactory stepsFactory) {
            this.method = method;
            this.stepsType = stepsType;
            this.stepsFactory = stepsFactory;
        }

        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return method.getReturnType().isAssignableFrom((Class<?>) type);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            try {
                Object instance = instance();
                return method.invoke(instance, value);
            } catch (Exception e) {
                throw new ParameterConvertionFailed("Failed to invoke method " + method + " with value " + value
                        + " in " + type, e);
            }
        }

        private Object instance() {
            return stepsFactory.createInstanceOfType(stepsType);
        }

    }
}
