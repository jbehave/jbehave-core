package org.jbehave.core.steps;

import static java.util.Arrays.asList;

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

import org.jbehave.core.model.ExamplesTable;

/**
 * <p>
 * Facade responsible for converting parameter values to Java objects.
 * </p>
 * <p>
 * A number of default converters are provided:
 * <ul>
 * <li>{@link ParameterConverters#NumberConverter}</li>
 * <li>{@link ParameterConverters#NumberListConverter}</li>
 * <li>{@link ParameterConverters#StringListConverter}</li>
 * <li>{@link ParameterConverters#DateConverter}</li>
 * <li>{@link ParameterConverters#ExamplesTableConverter}</li>
 * </ul>
 * </p>
 */
public class ParameterConverters {

	private static final String NEWLINES_PATTERN = "(\n)|(\r\n)";
	private static final String SYSTEM_NEWLINE = System
			.getProperty("line.separator");
	private static final String COMMA = ",";
	private static final ParameterConverter[] DEFAULT_CONVERTERS = {
			new NumberConverter(), new NumberListConverter(),
			new StringListConverter(), new DateConverter(),
			new ExamplesTableConverter() };
	private final StepMonitor monitor;
	private final List<ParameterConverter> converters = new ArrayList<ParameterConverter>();

	public ParameterConverters() {
		this(new SilentStepMonitor());
	}

	public ParameterConverters(StepMonitor monitor) {
		this.monitor = monitor;
		this.addConverters(DEFAULT_CONVERTERS);
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
				monitor.convertedValueOfType(value, type, converted, converter
						.getClass());
				return converted;
			}
		}
		// default to String
		return replaceNewlinesWithSystemNewlines(value);
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

		public ParameterConvertionFailed(String message, Throwable cause) {
			super(message, cause);
		}

	}

	/**
	 * Converts values to numbers. Supports
	 * <ul>
	 * <li>Integer, int</li>
	 * <li>Long, long</li>
	 * <li>Double, double</li>
	 * <li>Float, float</li>
	 * <li>BigDecimale, BigInteger</li>
	 * </ul>
	 */
	public static class NumberConverter implements ParameterConverter {
		@SuppressWarnings("unchecked")
		private static List<Class> acceptedClasses = asList(new Class[] {
				Integer.class, int.class, Long.class, long.class, Double.class,
				double.class, Float.class, float.class, BigDecimal.class,
				BigInteger.class });

		public boolean accept(Type type) {
			if (type instanceof Class<?>) {
				return acceptedClasses.contains(type);
			}
			return false;
		}

		public Object convertValue(String value, Type type) {
			if (type == Integer.class || type == int.class) {
				return Integer.valueOf(value);
			} else if (type == Long.class || type == long.class) {
				return Long.valueOf(value);
			} else if (type == Double.class || type == double.class) {
				return Double.valueOf(value);
			} else if (type == Float.class || type == float.class) {
				return Float.valueOf(value);
			} else if (type == BigDecimal.class) {
				return new BigDecimal(value);
			} else if (type == BigInteger.class) {
				return new BigInteger(value);
			}
			return value;
		}

	}

	/**
	 * Converts value to list of numbers. Splits value to a list, using an
	 * injectable value separator (defaults to ",") and converts each element of
	 * list via the {@link NumberCoverter}.
	 */
	public static class NumberListConverter implements ParameterConverter {

		private NumberConverter numberConverter = new NumberConverter();
		private NumberFormat numberFormat;
		private String valueSeparator;

		public NumberListConverter() {
			this(NumberFormat.getInstance(), COMMA);
		}

		public NumberListConverter(NumberFormat numberFormat,
				String valueSeparator) {
			this.numberFormat = numberFormat;
			this.valueSeparator = valueSeparator;
		}

		public boolean accept(Type type) {
			if (type instanceof ParameterizedType) {
				Type rawType = rawType(type);
				Type argumentType = argumentType(type);
				return List.class.isAssignableFrom((Class<?>) rawType)
						&& Number.class
								.isAssignableFrom((Class<?>) argumentType);
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
			if (argumentType.equals(Number.class)) {
				return convertWithNumberFormat(values);
			}
			return convertWithNumberConverter(values, argumentType);
		}

		private List<Number> convertWithNumberConverter(List<String> values,
				Class<? extends Number> type) {
			List<Number> numbers = new ArrayList<Number>();
			for (String value : values) {
				numbers.add((Number) numberConverter.convertValue(value, type));
			}
			return numbers;
		}

		private List<Number> convertWithNumberFormat(List<String> values) {
			List<Number> numbers = new ArrayList<Number>();
			for (String numberValue : values) {
				try {
					numbers.add(numberFormat.parse(numberValue));
				} catch (ParseException e) {
					throw new ParameterConvertionFailed(numberValue, e);
				}
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
			this(COMMA);
		}

		public StringListConverter(String valueSeparator) {
			this.valueSeparator = valueSeparator;
		}

		public boolean accept(Type type) {
			if (type instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type rawType = parameterizedType.getRawType();
				Type argumentType = parameterizedType.getActualTypeArguments()[0];
				return List.class.isAssignableFrom((Class<?>) rawType)
						&& String.class
								.isAssignableFrom((Class<?>) argumentType);
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

		private final DateFormat dateFormat;

		public DateConverter() {
			this(new SimpleDateFormat("dd/MM/yyyy"));
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
				throw new ParameterConvertionFailed("Could not convert value "
						+ value + " with date format " + dateFormat, e);
			}
		}

	}

	public static class ExamplesTableConverter implements ParameterConverter {

		private String headerSeparator;
		private String valueSeparator;

		public ExamplesTableConverter() {
			this("|", "|");
		}

		public ExamplesTableConverter(String headerSeparator,
				String valueSeparator) {
			this.headerSeparator = headerSeparator;
			this.valueSeparator = valueSeparator;
		}

		public boolean accept(Type type) {
			if (type instanceof Class<?>) {
				return ExamplesTable.class.isAssignableFrom((Class<?>) type);
			}
			return false;
		}

		public Object convertValue(String value, Type type) {
			return new ExamplesTable(value, headerSeparator, valueSeparator);
		}

	}

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
				throw new ParameterConvertionFailed("Failed to invoke method "
						+ method + " with value " + value + " in " + instance,
						e);
			}
		}

	}
}
