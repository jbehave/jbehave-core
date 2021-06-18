package org.jbehave.example.spring.security.util;

import java.util.Date;

public abstract class DateUtils {

    public static long getElapsedDays(Date earlyDate, Date laterDate) {
        long durationMillis = laterDate.getTime() - earlyDate.getTime();
        return durationMillis / (1000 * 60 * 60 * 24);
    }
}
