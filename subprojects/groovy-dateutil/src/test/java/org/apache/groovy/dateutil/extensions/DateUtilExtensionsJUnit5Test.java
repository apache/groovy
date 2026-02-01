/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.groovy.dateutil.extensions;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for DateUtilExtensions class.
 */
class DateUtilExtensionsJUnit5Test {

    private Calendar calendar;
    private Date date;

    @BeforeEach
    void setUp() {
        calendar = Calendar.getInstance();
        calendar.set(2020, Calendar.JUNE, 15, 10, 30, 45);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
    }

    // getAt tests for Date
    @Test
    void testGetAtDateWithYear() {
        assertEquals(2020, DateUtilExtensions.getAt(date, Calendar.YEAR));
    }

    @Test
    void testGetAtDateWithMonth() {
        assertEquals(Calendar.JUNE, DateUtilExtensions.getAt(date, Calendar.MONTH));
    }

    @Test
    void testGetAtDateWithDayOfMonth() {
        assertEquals(15, DateUtilExtensions.getAt(date, Calendar.DAY_OF_MONTH));
    }

    @Test
    void testGetAtDateWithHour() {
        assertEquals(10, DateUtilExtensions.getAt(date, Calendar.HOUR_OF_DAY));
    }

    @Test
    void testGetAtDateWithCollection() {
        List<Integer> fields = Arrays.asList(Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH);
        List<Integer> result = DateUtilExtensions.getAt(date, fields);
        assertEquals(3, result.size());
        assertEquals(2020, result.get(0));
        assertEquals(Calendar.JUNE, result.get(1));
        assertEquals(15, result.get(2));
    }

    // toCalendar tests
    @Test
    void testToCalendar() {
        Calendar result = DateUtilExtensions.toCalendar(date);
        assertEquals(2020, result.get(Calendar.YEAR));
        assertEquals(Calendar.JUNE, result.get(Calendar.MONTH));
        assertEquals(15, result.get(Calendar.DAY_OF_MONTH));
    }

    // getAt tests for Calendar
    @Test
    void testGetAtCalendarWithYear() {
        assertEquals(2020, DateUtilExtensions.getAt(calendar, Calendar.YEAR));
    }

    @Test
    void testGetAtCalendarWithMonth() {
        assertEquals(Calendar.JUNE, DateUtilExtensions.getAt(calendar, Calendar.MONTH));
    }

    @Test
    void testGetAtCalendarWithCollection() {
        List<Integer> fields = Arrays.asList(Calendar.YEAR, Calendar.MONTH);
        List<Integer> result = DateUtilExtensions.getAt(calendar, fields);
        assertEquals(2, result.size());
        assertEquals(2020, result.get(0));
        assertEquals(Calendar.JUNE, result.get(1));
    }

    @Test
    void testGetAtCalendarWithNestedCollection() {
        Collection<Object> fields = Arrays.asList(
            Calendar.YEAR,
            Arrays.asList(Calendar.MONTH, Calendar.DAY_OF_MONTH)
        );
        List<Integer> result = DateUtilExtensions.getAt(calendar, fields);
        assertEquals(3, result.size());
        assertEquals(2020, result.get(0));
        assertEquals(Calendar.JUNE, result.get(1));
        assertEquals(15, result.get(2));
    }

    // putAt tests
    @Test
    void testPutAtCalendar() {
        DateUtilExtensions.putAt(calendar, Calendar.YEAR, 2025);
        assertEquals(2025, calendar.get(Calendar.YEAR));
    }

    @Test
    void testPutAtDate() {
        DateUtilExtensions.putAt(date, Calendar.YEAR, 2025);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2025, cal.get(Calendar.YEAR));
    }

    // set with Map tests
    @Test
    void testSetCalendarWithMap() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put(Calendar.YEAR, 2025);
        updates.put(Calendar.MONTH, Calendar.DECEMBER);
        DateUtilExtensions.set(calendar, updates);
        assertEquals(2025, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH));
    }

    @Test
    void testSetCalendarWithStringKeys() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put("year", 2025);
        updates.put("month", Calendar.DECEMBER);
        updates.put("date", 25);
        DateUtilExtensions.set(calendar, updates);
        assertEquals(2025, calendar.get(Calendar.YEAR));
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH));
        assertEquals(25, calendar.get(Calendar.DATE));
    }

    @Test
    void testSetCalendarWithDayOfMonth() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put("dayOfMonth", 20);
        DateUtilExtensions.set(calendar, updates);
        assertEquals(20, calendar.get(Calendar.DATE));
    }

    @Test
    void testSetCalendarWithHourOfDay() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put("hourOfDay", 14);
        DateUtilExtensions.set(calendar, updates);
        assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    void testSetCalendarWithMinute() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put("minute", 45);
        DateUtilExtensions.set(calendar, updates);
        assertEquals(45, calendar.get(Calendar.MINUTE));
    }

    @Test
    void testSetCalendarWithSecond() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put("second", 30);
        DateUtilExtensions.set(calendar, updates);
        assertEquals(30, calendar.get(Calendar.SECOND));
    }

    @Test
    void testSetDate() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put("year", 2025);
        DateUtilExtensions.set(date, updates);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(2025, cal.get(Calendar.YEAR));
    }

    // updated/copyWith tests
    @Test
    void testUpdatedCalendar() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put(Calendar.YEAR, 2025);
        Calendar result = DateUtilExtensions.updated(calendar, updates);
        assertEquals(2025, result.get(Calendar.YEAR));
        assertEquals(2020, calendar.get(Calendar.YEAR)); // original unchanged
    }

    @Test
    void testCopyWithCalendar() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put(Calendar.YEAR, 2025);
        Calendar result = DateUtilExtensions.copyWith(calendar, updates);
        assertEquals(2025, result.get(Calendar.YEAR));
        assertEquals(2020, calendar.get(Calendar.YEAR)); // original unchanged
    }

    @Test
    void testUpdatedDate() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put("year", 2025);
        Date result = DateUtilExtensions.updated(date, updates);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(2025, cal.get(Calendar.YEAR));
    }

    @Test
    void testCopyWithDate() {
        Map<Object, Integer> updates = new HashMap<>();
        updates.put("year", 2025);
        Date result = DateUtilExtensions.copyWith(date, updates);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(2025, cal.get(Calendar.YEAR));
    }

    // next/previous tests for Date
    @Test
    void testNextDate() {
        Date result = DateUtilExtensions.next(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void testPreviousDate() {
        Date result = DateUtilExtensions.previous(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH));
    }

    // next/previous tests for Calendar
    @Test
    void testNextCalendar() {
        Calendar result = DateUtilExtensions.next(calendar);
        assertEquals(16, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH)); // original unchanged
    }

    @Test
    void testPreviousCalendar() {
        Calendar result = DateUtilExtensions.previous(calendar);
        assertEquals(14, result.get(Calendar.DAY_OF_MONTH));
        assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH)); // original unchanged
    }

    // next/previous tests for java.sql.Date
    @Test
    void testNextSqlDate() {
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        java.sql.Date result = DateUtilExtensions.next(sqlDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void testPreviousSqlDate() {
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        java.sql.Date result = DateUtilExtensions.previous(sqlDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH));
    }

    // plus/minus tests for Date
    @Test
    void testPlusDate() {
        Date result = DateUtilExtensions.plus(date, 5);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void testMinusDate() {
        Date result = DateUtilExtensions.minus(date, 5);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(10, cal.get(Calendar.DAY_OF_MONTH));
    }

    // plus/minus tests for java.sql.Date
    @Test
    void testPlusSqlDate() {
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        java.sql.Date result = DateUtilExtensions.plus(sqlDate, 5);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
    }

    @Test
    void testMinusSqlDate() {
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        java.sql.Date result = DateUtilExtensions.minus(sqlDate, 5);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(10, cal.get(Calendar.DAY_OF_MONTH));
    }

    // plus/minus tests for Timestamp
    @Test
    void testPlusTimestamp() {
        Timestamp ts = new Timestamp(date.getTime());
        ts.setNanos(123456789);
        Timestamp result = DateUtilExtensions.plus(ts, 5);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH));
        assertEquals(123456789, result.getNanos()); // nanos preserved
    }

    @Test
    void testMinusTimestamp() {
        Timestamp ts = new Timestamp(date.getTime());
        Timestamp result = DateUtilExtensions.minus(ts, 5);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(10, cal.get(Calendar.DAY_OF_MONTH));
    }

    // minus (date - date) tests
    @Test
    void testMinusCalendarFromCalendar() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2020, Calendar.JUNE, 20);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2020, Calendar.JUNE, 15);
        
        int days = DateUtilExtensions.minus(cal1, cal2);
        assertEquals(5, days);
    }

    @Test
    void testMinusCalendarFromCalendarNegative() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2020, Calendar.JUNE, 10);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2020, Calendar.JUNE, 15);
        
        int days = DateUtilExtensions.minus(cal1, cal2);
        assertEquals(-5, days);
    }

    @Test
    void testMinusCalendarAcrossYears() {
        Calendar cal1 = Calendar.getInstance();
        cal1.set(2021, Calendar.JANUARY, 1);
        Calendar cal2 = Calendar.getInstance();
        cal2.set(2020, Calendar.DECEMBER, 31);
        
        int days = DateUtilExtensions.minus(cal1, cal2);
        assertEquals(1, days);
    }

    @Test
    void testMinusDateFromDate() {
        Date date1 = DateUtilExtensions.plus(date, 10);
        int days = DateUtilExtensions.minus(date1, date);
        assertEquals(10, days);
    }

    // format tests
    @Test
    void testFormatDate() {
        TimeZone originalTz = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            date = calendar.getTime();
            String result = DateUtilExtensions.format(date, "yyyy-MM-dd");
            assertEquals("2020-06-15", result);
        } finally {
            TimeZone.setDefault(originalTz);
        }
    }

    @Test
    void testFormatDateWithTimezone() {
        String result = DateUtilExtensions.format(date, "yyyy-MM-dd", TimeZone.getTimeZone("UTC"));
        assertNotNull(result);
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"));
    }

    @Test
    void testFormatCalendar() {
        String result = DateUtilExtensions.format(calendar, "HH:mm:ss");
        assertEquals("10:30:45", result);
    }

    // getDateString, getTimeString, getDateTimeString tests
    @Test
    void testGetDateString() {
        String result = DateUtilExtensions.getDateString(date);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetTimeString() {
        String result = DateUtilExtensions.getTimeString(date);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGetDateTimeString() {
        String result = DateUtilExtensions.getDateTimeString(date);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // clearTime tests
    @Test
    void testClearTimeDate() {
        Date result = DateUtilExtensions.clearTime(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, cal.get(Calendar.MINUTE));
        assertEquals(0, cal.get(Calendar.SECOND));
        assertEquals(0, cal.get(Calendar.MILLISECOND));
    }

    @Test
    void testClearTimeSqlDate() {
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        java.sql.Date result = DateUtilExtensions.clearTime(sqlDate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(result);
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY));
    }

    @Test
    void testClearTimeCalendar() {
        Calendar result = DateUtilExtensions.clearTime(calendar);
        assertEquals(0, result.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, result.get(Calendar.MINUTE));
        assertEquals(0, result.get(Calendar.SECOND));
        assertSame(calendar, result); // modifies in place
    }

    // upto tests
    @Test
    void testUptoDate() {
        Date start = date;
        Date end = DateUtilExtensions.plus(date, 3);
        
        List<Date> collected = new ArrayList<>();
        DateUtilExtensions.upto(start, end, new Closure<Void>(null) {
            @Override
            public Void call(Object... args) {
                collected.add((Date) args[0]);
                return null;
            }
        });
        
        assertEquals(4, collected.size()); // inclusive: 15, 16, 17, 18
    }

    @Test
    void testUptoDateThrowsWhenEndBeforeStart() {
        Date start = date;
        Date end = DateUtilExtensions.minus(date, 1);
        
        assertThrows(GroovyRuntimeException.class, () ->
            DateUtilExtensions.upto(start, end, new Closure<Void>(null) {
                @Override
                public Void call(Object... args) {
                    return null;
                }
            }));
    }

    @Test
    void testUptoCalendar() {
        Calendar end = DateUtilExtensions.next(DateUtilExtensions.next(calendar));
        
        List<Calendar> collected = new ArrayList<>();
        DateUtilExtensions.upto(calendar, end, new Closure<Void>(null) {
            @Override
            public Void call(Object... args) {
                collected.add((Calendar) args[0]);
                return null;
            }
        });
        
        assertEquals(3, collected.size());
    }

    @Test
    void testUptoCalendarThrowsWhenEndBeforeStart() {
        Calendar end = DateUtilExtensions.previous(calendar);
        
        assertThrows(GroovyRuntimeException.class, () ->
            DateUtilExtensions.upto(calendar, end, new Closure<Void>(null) {
                @Override
                public Void call(Object... args) {
                    return null;
                }
            }));
    }

    // downto tests
    @Test
    void testDowntoDate() {
        Date start = date;
        Date end = DateUtilExtensions.minus(date, 2);
        
        List<Date> collected = new ArrayList<>();
        DateUtilExtensions.downto(start, end, new Closure<Void>(null) {
            @Override
            public Void call(Object... args) {
                collected.add((Date) args[0]);
                return null;
            }
        });
        
        assertEquals(3, collected.size()); // inclusive: 15, 14, 13
    }

    @Test
    void testDowntoDateThrowsWhenEndAfterStart() {
        Date start = date;
        Date end = DateUtilExtensions.plus(date, 1);
        
        assertThrows(GroovyRuntimeException.class, () ->
            DateUtilExtensions.downto(start, end, new Closure<Void>(null) {
                @Override
                public Void call(Object... args) {
                    return null;
                }
            }));
    }

    @Test
    void testDowntoCalendar() {
        Calendar end = DateUtilExtensions.previous(DateUtilExtensions.previous(calendar));
        
        List<Calendar> collected = new ArrayList<>();
        DateUtilExtensions.downto(calendar, end, new Closure<Void>(null) {
            @Override
            public Void call(Object... args) {
                collected.add((Calendar) args[0]);
                return null;
            }
        });
        
        assertEquals(3, collected.size());
    }

    @Test
    void testDowntoCalendarThrowsWhenEndAfterStart() {
        Calendar end = DateUtilExtensions.next(calendar);
        
        assertThrows(GroovyRuntimeException.class, () ->
            DateUtilExtensions.downto(calendar, end, new Closure<Void>(null) {
                @Override
                public Void call(Object... args) {
                    return null;
                }
            }));
    }

    // Edge case tests
    @Test
    void testPlusZeroDays() {
        Date result = DateUtilExtensions.plus(date, 0);
        assertEquals(date.getTime(), result.getTime());
    }

    @Test
    void testMinusNegativeDays() {
        Date result = DateUtilExtensions.minus(date, -5);
        Date expected = DateUtilExtensions.plus(date, 5);
        assertEquals(expected.getTime(), result.getTime());
    }

    @Test
    void testUptoSameDate() {
        List<Date> collected = new ArrayList<>();
        DateUtilExtensions.upto(date, date, new Closure<Void>(null) {
            @Override
            public Void call(Object... args) {
                collected.add((Date) args[0]);
                return null;
            }
        });
        assertEquals(1, collected.size());
    }

    @Test
    void testDowntoSameDate() {
        List<Date> collected = new ArrayList<>();
        DateUtilExtensions.downto(date, date, new Closure<Void>(null) {
            @Override
            public Void call(Object... args) {
                collected.add((Date) args[0]);
                return null;
            }
        });
        assertEquals(1, collected.size());
    }
}
