package org.jbehave.core.steps;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;

import static java.util.Arrays.asList;

/**
 * <p>
 * Facade responsible for converting parameter values to Java objects.
 * </p>
 * <p>
 * Several converters are provided out-of-the-box:
 * <ul>
 * <li>{@link ParameterConverters.NumberConverter NumberConverter}</li>
 * <li>{@link ParameterConverters.NumberListConverter NumberListConverter}</li>
 * <li>{@link ParameterConverters.StringListConverter StringListConverter}</li>
 * <li>{@link ParameterConverters.DateConverter DateConverter}</li>
 * <li>{@link ParameterConverters.ExamplesTableConverter ExamplesTableConverter}</li>
 * <li>{@link ParameterConverters.MethodReturningConverter MethodReturningConverter}</li>
 * </ul>
 * </p>
 */
public class ParameterConverters {

    static final Locale DEFAULT_NUMBER_FORMAT_LOCAL = Locale.ENGLISH;
    static final String DEFAULT_COMMA = ",";
	
    private static final String NEWLINES_PATTERN = "(\n)|(\r\n)";
    private static final String SYSTEM_NEWLINE = System.getProperty("line.separator");
    
    private final StepMonitor monitor;
    private final List<ParameterConverter> converters = new ArrayList<ParameterConverter>();

    /** 
     * Default Parameters use a SilentStepMonitor, has English as Locale and use "," as list separator. 
     */
    public ParameterConverters() {
        this(new SilentStepMonitor());
    }

    /** 
     * Default ParameterConverters have English as Locale and use "," as list separator. 
     */
    public ParameterConverters(StepMonitor monitor) {
    	this(monitor , DEFAULT_NUMBER_FORMAT_LOCAL, DEFAULT_COMMA);
    }

    /** 
     * Creates an ParameterConvertors for a given Locale.  When selecting
     * a listSeparator, please make sure that this character doesn't have a special
     * meaning in your Locale (for instance "," is used as decimal separator in some Locale)
     * @param monitor Monitor reporting the conversions
     * @param locale The Locale to use when reading numbers
     * @param listSeparator The list separator 
     */
    public ParameterConverters(StepMonitor monitor, Locale locale, String listSeparator) {
        this.monitor = monitor;
        this.addConverters(defaultConverters(locale, listSeparator));    	
    }
    
    protected ParameterConverter[] defaultConverters(Locale locale, String listSeparator) {
        String escapedListSeparator = escapeRegexPunctuation(listSeparator);
        ParameterConverter[] defaultConverters = {
            new NumberConverter(NumberFormat.getInstance(locale)),
            new NumberListConverter(NumberFormat.getInstance(locale), escapedListSeparator), 
            new StringListConverter(escapedListSeparator),
            new DateConverter(), 
            new ExamplesTableConverter(new ExamplesTableFactory(this))
        };
        return defaultConverters;
    }

    //TODO : This is a duplicate from RegExpPrefixCapturing
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
     * <li>BigDecimal: {@link BigDecimal#valueOf(Double)}</li></li>
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

        public NumberConverter() {
            this(NumberFormat.getInstance(DEFAULT_NUMBER_FORMAT_LOCAL));
        }

        public NumberConverter(NumberFormat numberFormat) {
            this.numberFormat = numberFormat;
        }

        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return Number.class.isAssignableFrom((Class<?>) type) || primitiveTypes.contains(type);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            try {
                Number n = numberFormat.parse(value);
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
                } else {
                    return n;
                }
            } catch (ParseException e) {
                throw new ParameterConvertionFailed(value, e);
            }
        }

        private String canonicalize(String value) {
            char decimalPointSeparator = numberFormat.format(1.01).charAt(1);
            int decimalPointPosition = value.lastIndexOf(decimalPointSeparator);
            value = value.trim();
            if (decimalPointPosition != -1) {
                String sf = value.substring(0, decimalPointPosition).replace("[^0-9]", "");
                String dp = value.substring(decimalPointPosition+1);
                return sf + "." + dp;
            }
            return value.replace(' ', ',');
        }

    }

    /**
     * Converts value to list of numbers. Splits value to a list, using an
     * injectable value separator (defaulting to ",") and converts each element
     * of list via the {@link NumberConverter}, using the {@link NumberFormat}
     * provided (defaulting to {@link NumberFormat#getInstance(Locale.ENGLISH)}).
     */
    public static class NumberListConverter implements ParameterConverter {

        private final NumberConverter numberConverter;
        private final String valueSeparator;

        public NumberListConverter() {
            this(NumberFormat.getInstance(DEFAULT_NUMBER_FORMAT_LOCAL), DEFAULT_COMMA);
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
            this(DEFAULT_COMMA);
        }

        /**
         * @param numberFormat Specific NumberFormat to use.
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
                throw new ParameterConvertionFailed("Could not convert value " + value + " with date format "
                        + (dateFormat instanceof SimpleDateFormat ? ((SimpleDateFormat) dateFormat).toPattern() : dateFormat), e);
            }
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
                return valueOfMethod.invoke(enumClass, new Object[] { value });
            } catch (Exception e) {
                throw new ParameterConvertionFailed("Conversion error: " + value + " on Enum: " + typeClass, e);
            }
        }
    }
 
    /**
     * Parses value to list of the same {@link Enum}, using an injectable
     * value separator (defaults to ",") and trimming each element of the
     * list.
     */
    public static class EnumListConverter implements ParameterConverter {
        private final EnumConverter enumConverter;
        private String valueSeparator;

        public EnumListConverter() {
            this(DEFAULT_COMMA);
        }
        
        public EnumListConverter(String valueSeparator) {
            this.enumConverter = new EnumConverter();
            this.valueSeparator = valueSeparator;
        }

        public boolean accept(Type type) {
            if (type instanceof ParameterizedType) {
                Type rawType = rawType(type);
                Type argumentType = argumentType(type);
                return List.class.isAssignableFrom((Class<?>) rawType)
                        && enumConverter.accept(argumentType);
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
     * Converts value to {@link ExamplesTable} using a {@link ExamplesTableFactory}.
     */
    public static class ExamplesTableConverter implements ParameterConverter {

        private final ExamplesTableFactory factory;
        
        public ExamplesTableConverter(){
            this(new ExamplesTableFactory());           
        }

        public ExamplesTableConverter(ExamplesTableFactory factory){
            this.factory = factory;            
        }

        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return ExamplesTable.class.isAssignableFrom((Class<?>) type);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            return factory.createExamplesTable(value);
        }

    }

    /**
     * Invokes method on instance to return value.
     */
    public static class MethodReturningConverter implements ParameterConverter {
        private Object instance;
        private Method method;

        public MethodReturningConverter(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }

        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return method.getReturnType().isAssignableFrom((Class<?>) type);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            try {
                return method.invoke(instance, value);
            } catch (Exception e) {
                throw new ParameterConvertionFailed("Failed to invoke method " + method + " with value " + value
                        + " in " + instance, e);
            }
        }

    }
}
