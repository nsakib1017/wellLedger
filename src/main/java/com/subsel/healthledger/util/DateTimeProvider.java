package com.subsel.healthledger.util;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeProvider {

    static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String getDateTime(String timezone) {

        Date date = new Date();
        df.setTimeZone(TimeZone.getTimeZone(timezone));
        return df.format(date);
    }

    public static String getDateTime() {
        return getDateTime("Asia/Dhaka");
    }
}
