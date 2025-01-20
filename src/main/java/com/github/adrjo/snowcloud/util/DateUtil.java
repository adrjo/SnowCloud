package com.github.adrjo.snowcloud.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss z",
            Locale.US);

    public static String format(long epoch) {
        return dateFormat.format(new Date(epoch));
    }

}
