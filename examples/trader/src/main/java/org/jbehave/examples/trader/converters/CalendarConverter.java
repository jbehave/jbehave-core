package org.jbehave.examples.trader.converters;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.jbehave.scenario.steps.ParameterConverters;
import org.jbehave.scenario.steps.ParameterConverters.ParameterConverter;

public class CalendarConverter implements ParameterConverter {
    
    private final SimpleDateFormat dateFormat;
 
    public CalendarConverter(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
    }
 
    public boolean accept(Type type) {
        if (type instanceof Class<?>) {
            return Calendar.class.isAssignableFrom((Class<?>) type);
        }
        return false;
    }
 
    public Object convertValue(String value, Type type) {
        try {
            if (StringUtils.isBlank(value) || "none".equals(value)) return null;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(value));
            return calendar;
        } catch (ParseException e) {
            throw new RuntimeException("Could not convert value "+value+" with format "+dateFormat.toPattern());
        }
    }

    public static ParameterConverter monthDayYear() {
        return new CalendarConverter("MM/dd/yyyy");
    }
    
    public static ParameterConverters monthDayYearWrapped() {
        return new ParameterConverters(monthDayYear());
    }
 
}

