package org.jbehave.scenario.steps;

import static java.util.Arrays.asList;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.jbehave.scenario.definition.ExamplesTable;

/**
 * Facade responsible for converting parameter values to Java objects.
 * 
 * @author Elizabeth Keogh
 * @author Mauro Talevi
 */
public class ParameterConverters {

	private static final String NEWLINES_PATTERN = "(\n)|(\r\n)";
	private static final String SYSTEM_NEWLINE = System.getProperty("line.separator");
	private static final String COMMA = ",";
	private static final List<ParameterConverter> DEFAULT_CONVERTERS = asList(
			new NumberConverter(), new NumberListConverter(),
			new StringListConverter(), new ExamplesTableConverter());
	private final StepMonitor monitor;
	private final List<ParameterConverter> converters = new ArrayList<ParameterConverter>();

	public ParameterConverters() {
		this(new SilentStepMonitor());
	}

	public ParameterConverters(ParameterConverter... customConverters) {
		this(new SilentStepMonitor(), customConverters);
	}

	public ParameterConverters(StepMonitor monitor,
			ParameterConverter... customConverters) {
		this.monitor = monitor;
		this.converters.addAll(asList(customConverters));
		this.converters.addAll(DEFAULT_CONVERTERS);
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
	public static class InvalidParameterException extends RuntimeException {

		public InvalidParameterException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	public static class NumberConverter implements ParameterConverter {
		@SuppressWarnings("unchecked")
		private static List<Class> acceptedClasses = asList(new Class[] {
				Integer.class, int.class, Long.class, long.class, Double.class,
				double.class, Float.class, float.class, BigDecimal.class, BigInteger.class });

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
			if ( argumentType.equals(Number.class) ){
	            return convertWithNumberFormat(values);			    
			}
			return convertWithNumberConverter(values, argumentType);
		}

        private List<Number> convertWithNumberConverter(List<String> values, Class<? extends Number> type) {
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
					throw new InvalidParameterException(numberValue, e);
				}
			}
            return numbers;
        }

	}

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
			if (value.trim().length() == 0) return asList();
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

    public static class ExamplesTableConverter implements ParameterConverter {
        public boolean accept(Type type) {
            if (type instanceof Class<?>) {
                return ExamplesTable.class.isAssignableFrom((Class<?>) type);
            }
            return false;
        }

        public Object convertValue(String value, Type type) {
            return new ExamplesTable(value);
        }

    }
	
}
