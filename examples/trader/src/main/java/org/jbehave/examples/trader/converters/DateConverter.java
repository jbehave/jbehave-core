package org.jbehave.examples.trader.converters;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jbehave.scenario.steps.ParameterConverters.ParameterConverter;

public class DateConverter implements ParameterConverter {

    private final SimpleDateFormat dateFormat;

	public DateConverter(String dateFormat) {
		this.dateFormat = new SimpleDateFormat(dateFormat);
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
			throw new RuntimeException("Could not convert value "+value+" with format "+dateFormat.toPattern());
		}
    }

}
