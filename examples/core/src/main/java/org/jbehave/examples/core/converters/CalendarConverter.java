package org.jbehave.examples.core.converters;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.steps.ParameterConverters.FromStringParameterConverter;

public class CalendarConverter extends FromStringParameterConverter<Calendar> {
    
    private final SimpleDateFormat dateFormat;
 
    public CalendarConverter(String dateFormat) {
        this.dateFormat = new SimpleDateFormat(dateFormat);
    }
 
    @Override
    public Calendar convertValue(String value, Type type) {
        try {
            if (StringUtils.isBlank(value) || "none".equals(value)) {
                return null;
            }
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateFormat.parse(value));
            return calendar;
        } catch (ParseException e) {
            throw new RuntimeException("Could not convert value "+value + " with format " +dateFormat.toPattern());
        }
    }

}

