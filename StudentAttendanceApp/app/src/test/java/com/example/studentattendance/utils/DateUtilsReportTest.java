package com.example.studentattendance.utils;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DateUtilsReportTest {

    @Test
    public void datesInclusiveCoversCurrentWeekSpan() {
        List<String> week = DateUtils.datesInclusive("2026-09-01", "2026-09-07");
        assertEquals(7, week.size());
        assertEquals("2026-09-01", week.get(0));
        assertEquals("2026-09-07", week.get(6));
    }

    @Test
    public void datesInclusiveCoversCurrentMonthLength() {
        List<String> month = DateUtils.datesInclusive("2026-09-01", "2026-09-30");
        assertEquals(30, month.size());
        assertEquals("2026-09-30", month.get(29));
    }

    @Test
    public void invertedRangeIsEmpty() {
        assertTrue(DateUtils.datesInclusive("2026-09-07", "2026-09-01").isEmpty());
    }

    @Test
    public void reportingPeriodSpansSemesterMonths() {
        String expected = DateUtils.monthYear("2026-01-05") + " – " + DateUtils.monthYear("2026-06-03");
        assertEquals(expected, DateUtils.reportingPeriod("2026-01-05", "2026-06-03"));
    }

    @Test
    public void daysAgoFromDoesNotMutateBeyondRequestedSpan() {
        assertEquals("2026-06-01", DateUtils.daysAgoFrom("2026-06-07", 6));
    }
}
