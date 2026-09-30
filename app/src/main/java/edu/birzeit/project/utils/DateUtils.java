package edu.birzeit.project.utils;

import java.text.DateFormat;
import java.util.Date;

public final class DateUtils {
    private DateUtils() { }

    public static String format(long timestamp) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(timestamp));
    }
}
