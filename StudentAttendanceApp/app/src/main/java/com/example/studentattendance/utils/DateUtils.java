package com.example.studentattendance.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Centralised date formatting/parsing helpers so every screen renders and
 * stores dates identically.
 */
public final class DateUtils {

    private DateUtils() {
    }

    public static String today() {
        return format(Calendar.getInstance().getTime(), Constants.DATE_PATTERN);
    }

    public static String daysAgo(int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -Math.max(0, days));
        return format(calendar.getTime(), Constants.DATE_PATTERN);
    }

    public static String startOfCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        return format(calendar.getTime(), Constants.DATE_PATTERN);
    }

    public static String endOfCurrentWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        return format(calendar.getTime(), Constants.DATE_PATTERN);
    }

    public static String startOfCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return format(calendar.getTime(), Constants.DATE_PATTERN);
    }

    public static String endOfCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return format(calendar.getTime(), Constants.DATE_PATTERN);
    }

    public static java.util.List<String> datesInclusive(String fromInclusive, String toInclusive) {
        java.util.List<String> dates = new java.util.ArrayList<>();
        if (fromInclusive == null || toInclusive == null || fromInclusive.compareTo(toInclusive) > 0) {
            return dates;
        }
        Calendar calendar = toCalendar(fromInclusive);
        String cursor = format(calendar.getTime(), Constants.DATE_PATTERN);
        int guard = 0;
        while (cursor.compareTo(toInclusive) <= 0 && guard < 366) {
            dates.add(cursor);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            cursor = format(calendar.getTime(), Constants.DATE_PATTERN);
            guard++;
        }
        return dates;
    }

    public static long startOfCurrentMonthMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static boolean inInclusiveRange(String storageDate, String fromInclusive, String toInclusive) {
        if (storageDate == null || storageDate.isEmpty()) {
            return false;
        }
        if (fromInclusive != null && storageDate.compareTo(fromInclusive) < 0) {
            return false;
        }
        if (toInclusive != null && storageDate.compareTo(toInclusive) > 0) {
            return false;
        }
        return true;
    }

    public static String daysAgoFrom(String storageDate, int days) {
        Calendar calendar = toCalendar(storageDate);
        calendar.add(Calendar.DAY_OF_MONTH, -Math.max(0, days));
        return format(calendar.getTime(), Constants.DATE_PATTERN);
    }

    public static String monthYear(String storageDate) {
        if (storageDate == null || storageDate.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat input = new SimpleDateFormat(Constants.DATE_PATTERN, Locale.getDefault());
            Date date = input.parse(storageDate);
            if (date == null) {
                return storageDate;
            }
            return new SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return storageDate;
        }
    }

    public static String reportingPeriod(String fromInclusive, String toInclusive) {
        String from = monthYear(fromInclusive);
        String to = monthYear(toInclusive);
        if (from.isEmpty()) {
            return to;
        }
        if (to.isEmpty() || from.equals(to)) {
            return from;
        }
        return from + " – " + to;
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(date);
    }

    public static String toDisplayFormat(String storageDate) {
        if (storageDate == null || storageDate.isEmpty()) {
            return "";
        }
        try {
            SimpleDateFormat input = new SimpleDateFormat(Constants.DATE_PATTERN, Locale.getDefault());
            Date date = input.parse(storageDate);
            if (date == null) {
                return storageDate;
            }
            SimpleDateFormat output = new SimpleDateFormat(Constants.DATE_DISPLAY_PATTERN, Locale.getDefault());
            return output.format(date);
        } catch (ParseException e) {
            return storageDate;
        }
    }

    public static String fromYearMonthDay(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        return format(calendar.getTime(), Constants.DATE_PATTERN);
    }

    public static Calendar toCalendar(String storageDate) {
        Calendar calendar = Calendar.getInstance();
        try {
            SimpleDateFormat input = new SimpleDateFormat(Constants.DATE_PATTERN, Locale.getDefault());
            Date date = input.parse(storageDate);
            if (date != null) {
                calendar.setTime(date);
            }
        } catch (ParseException ignored) {
            // fall back to today
        }
        return calendar;
    }
}
