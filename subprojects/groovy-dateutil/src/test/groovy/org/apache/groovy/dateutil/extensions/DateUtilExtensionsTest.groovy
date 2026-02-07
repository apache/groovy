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
package org.apache.groovy.dateutil.extensions

import groovy.lang.Closure
import groovy.lang.GroovyRuntimeException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.sql.Timestamp
import java.text.SimpleDateFormat

import static org.junit.jupiter.api.Assertions.*

/**
 * Tests for DateUtilExtensions class.
 */
class DateUtilExtensionsTest {

    private Calendar calendar
    private Date date

    @BeforeEach
    void setUp() {
        calendar = Calendar.getInstance()
        calendar.set(2020, Calendar.JUNE, 15, 10, 30, 45)
        calendar.set(Calendar.MILLISECOND, 0)
        date = calendar.getTime()
    }

    @Test
    void testPlus() {
        def sdf = new SimpleDateFormat("yyyyMMdd")
        def dec31 = sdf.parse("20171231")
        assertEquals("20180101", sdf.format(DateUtilExtensions.plus(dec31, 1)))
        assertEquals("20180101", sdf.format(DateUtilExtensions.plus(new Timestamp(dec31.getTime()), 1)))
    }

    @Test
    void testMinus() {
        def sdf = new SimpleDateFormat("yyyyMMdd")
        def jan01 = sdf.parse("20180101")
        assertEquals("20171231", sdf.format(DateUtilExtensions.minus(jan01, 1)))
        assertEquals("20171231", sdf.format(DateUtilExtensions.minus(new Timestamp(jan01.getTime()), 1)))
    }

    @Test
    void testNext() {
        def sdf = new SimpleDateFormat("yyyyMMdd")
        def cal = Calendar.getInstance()
        cal.setTime(sdf.parse("20171231"))
        assertEquals("20180101", sdf.format(DateUtilExtensions.next(cal).getTime()))
    }

    @Test
    void testPrevious() {
        def sdf = new SimpleDateFormat("yyyyMMdd")
        def cal = Calendar.getInstance()
        cal.setTime(sdf.parse("20180101"))
        assertEquals("20171231", sdf.format(DateUtilExtensions.previous(cal).getTime()))
    }

    @Test
    void testCalendarCollectGetAt() {
        def cal = Calendar.getInstance()
        def result = DateUtilExtensions.getAt(cal, [Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH])
        assertTrue(result.get(0) >= 2022, "Year")
        assertTrue(result.get(1) <= 11, "Month")
        assertTrue(result.get(2) <= 31, "Day")
    }

    // getAt tests for Date
    @Test
    void testGetAtDateWithYear() {
        assertEquals(2020, DateUtilExtensions.getAt(date, Calendar.YEAR))
    }

    @Test
    void testGetAtDateWithMonth() {
        assertEquals(Calendar.JUNE, DateUtilExtensions.getAt(date, Calendar.MONTH))
    }

    @Test
    void testGetAtDateWithDayOfMonth() {
        assertEquals(15, DateUtilExtensions.getAt(date, Calendar.DAY_OF_MONTH))
    }

    @Test
    void testGetAtDateWithHour() {
        assertEquals(10, DateUtilExtensions.getAt(date, Calendar.HOUR_OF_DAY))
    }

    @Test
    void testGetAtDateWithCollection() {
        def fields = [Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH]
        def result = DateUtilExtensions.getAt(date, fields)
        assertEquals(3, result.size())
        assertEquals(2020, result.get(0))
        assertEquals(Calendar.JUNE, result.get(1))
        assertEquals(15, result.get(2))
    }

    // toCalendar tests
    @Test
    void testToCalendar() {
        def result = DateUtilExtensions.toCalendar(date)
        assertEquals(2020, result.get(Calendar.YEAR))
        assertEquals(Calendar.JUNE, result.get(Calendar.MONTH))
        assertEquals(15, result.get(Calendar.DAY_OF_MONTH))
    }

    // getAt tests for Calendar
    @Test
    void testGetAtCalendarWithYear() {
        assertEquals(2020, DateUtilExtensions.getAt(calendar, Calendar.YEAR))
    }

    @Test
    void testGetAtCalendarWithMonth() {
        assertEquals(Calendar.JUNE, DateUtilExtensions.getAt(calendar, Calendar.MONTH))
    }

    @Test
    void testGetAtCalendarWithCollection() {
        def fields = [Calendar.YEAR, Calendar.MONTH]
        def result = DateUtilExtensions.getAt(calendar, fields)
        assertEquals(2, result.size())
        assertEquals(2020, result.get(0))
        assertEquals(Calendar.JUNE, result.get(1))
    }

    @Test
    void testGetAtCalendarWithNestedCollection() {
        Collection<Object> fields = [
            Calendar.YEAR,
            [Calendar.MONTH, Calendar.DAY_OF_MONTH]
        ]
        def result = DateUtilExtensions.getAt(calendar, fields)
        assertEquals(3, result.size())
        assertEquals(2020, result.get(0))
        assertEquals(Calendar.JUNE, result.get(1))
        assertEquals(15, result.get(2))
    }

    // putAt tests
    @Test
    void testPutAtCalendar() {
        DateUtilExtensions.putAt(calendar, Calendar.YEAR, 2025)
        assertEquals(2025, calendar.get(Calendar.YEAR))
    }

    @Test
    void testPutAtDate() {
        DateUtilExtensions.putAt(date, Calendar.YEAR, 2025)
        def cal = Calendar.getInstance()
        cal.setTime(date)
        assertEquals(2025, cal.get(Calendar.YEAR))
    }

    // set with Map tests
    @Test
    void testSetCalendarWithMap() {
        def updates = [:]
        updates.put(Calendar.YEAR, 2025)
        updates.put(Calendar.MONTH, Calendar.DECEMBER)
        DateUtilExtensions.set(calendar, updates)
        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH))
    }

    @Test
    void testSetCalendarWithStringKeys() {
        def updates = [:]
        updates.put("year", 2025)
        updates.put("month", Calendar.DECEMBER)
        updates.put("date", 25)
        DateUtilExtensions.set(calendar, updates)
        assertEquals(2025, calendar.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, calendar.get(Calendar.MONTH))
        assertEquals(25, calendar.get(Calendar.DATE))
    }

    @Test
    void testSetCalendarWithDayOfMonth() {
        def updates = [:]
        updates.put("dayOfMonth", 20)
        DateUtilExtensions.set(calendar, updates)
        assertEquals(20, calendar.get(Calendar.DATE))
    }

    @Test
    void testSetCalendarWithHourOfDay() {
        def updates = [:]
        updates.put("hourOfDay", 14)
        DateUtilExtensions.set(calendar, updates)
        assertEquals(14, calendar.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    void testSetCalendarWithMinute() {
        def updates = [:]
        updates.put("minute", 45)
        DateUtilExtensions.set(calendar, updates)
        assertEquals(45, calendar.get(Calendar.MINUTE))
    }

    @Test
    void testSetCalendarWithSecond() {
        def updates = [:]
        updates.put("second", 30)
        DateUtilExtensions.set(calendar, updates)
        assertEquals(30, calendar.get(Calendar.SECOND))
    }

    @Test
    void testSetDate() {
        def updates = [:]
        updates.put("year", 2025)
        DateUtilExtensions.set(date, updates)
        def cal = Calendar.getInstance()
        cal.setTime(date)
        assertEquals(2025, cal.get(Calendar.YEAR))
    }

    // updated/copyWith tests
    @Test
    void testUpdatedCalendar() {
        def updates = [:]
        updates.put(Calendar.YEAR, 2025)
        def result = DateUtilExtensions.updated(calendar, updates)
        assertEquals(2025, result.get(Calendar.YEAR))
        assertEquals(2020, calendar.get(Calendar.YEAR)) // original unchanged
    }

    @Test
    void testCopyWithCalendar() {
        def updates = [:]
        updates.put(Calendar.YEAR, 2025)
        def result = DateUtilExtensions.copyWith(calendar, updates)
        assertEquals(2025, result.get(Calendar.YEAR))
        assertEquals(2020, calendar.get(Calendar.YEAR)) // original unchanged
    }

    @Test
    void testUpdatedDate() {
        def updates = [:]
        updates.put("year", 2025)
        def result = DateUtilExtensions.updated(date, updates)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(2025, cal.get(Calendar.YEAR))
    }

    @Test
    void testCopyWithDate() {
        def updates = [:]
        updates.put("year", 2025)
        def result = DateUtilExtensions.copyWith(date, updates)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(2025, cal.get(Calendar.YEAR))
    }

    // next/previous tests for Date
    @Test
    void testNextDate() {
        def result = DateUtilExtensions.next(date)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    void testPreviousDate() {
        def result = DateUtilExtensions.previous(date)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH))
    }

    // next/previous tests for Calendar
    @Test
    void testNextCalendar() {
        def result = DateUtilExtensions.next(calendar)
        assertEquals(16, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH)) // original unchanged
    }

    @Test
    void testPreviousCalendar() {
        def result = DateUtilExtensions.previous(calendar)
        assertEquals(14, result.get(Calendar.DAY_OF_MONTH))
        assertEquals(15, calendar.get(Calendar.DAY_OF_MONTH)) // original unchanged
    }

    // next/previous tests for java.sql.Date
    @Test
    void testNextSqlDate() {
        def sqlDate = new java.sql.Date(date.getTime())
        def result = DateUtilExtensions.next(sqlDate)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(16, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    void testPreviousSqlDate() {
        def sqlDate = new java.sql.Date(date.getTime())
        def result = DateUtilExtensions.previous(sqlDate)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH))
    }

    // plus/minus tests for Date
    @Test
    void testPlusDate() {
        def result = DateUtilExtensions.plus(date, 5)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    void testMinusDate() {
        def result = DateUtilExtensions.minus(date, 5)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(10, cal.get(Calendar.DAY_OF_MONTH))
    }

    // plus/minus tests for java.sql.Date
    @Test
    void testPlusSqlDate() {
        def sqlDate = new java.sql.Date(date.getTime())
        def result = DateUtilExtensions.plus(sqlDate, 5)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    void testMinusSqlDate() {
        def sqlDate = new java.sql.Date(date.getTime())
        def result = DateUtilExtensions.minus(sqlDate, 5)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(10, cal.get(Calendar.DAY_OF_MONTH))
    }

    // plus/minus tests for Timestamp
    @Test
    void testPlusTimestamp() {
        def ts = new Timestamp(date.getTime())
        ts.setNanos(123456789)
        def result = DateUtilExtensions.plus(ts, 5)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(123456789, result.getNanos()) // nanos preserved
    }

    @Test
    void testMinusTimestamp() {
        def ts = new Timestamp(date.getTime())
        def result = DateUtilExtensions.minus(ts, 5)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(10, cal.get(Calendar.DAY_OF_MONTH))
    }

    // minus (date - date) tests
    @Test
    void testMinusCalendarFromCalendar() {
        def cal1 = Calendar.getInstance()
        cal1.set(2020, Calendar.JUNE, 20)
        def cal2 = Calendar.getInstance()
        cal2.set(2020, Calendar.JUNE, 15)

        def days = DateUtilExtensions.minus(cal1, cal2)
        assertEquals(5, days)
    }

    @Test
    void testMinusCalendarFromCalendarNegative() {
        def cal1 = Calendar.getInstance()
        cal1.set(2020, Calendar.JUNE, 10)
        def cal2 = Calendar.getInstance()
        cal2.set(2020, Calendar.JUNE, 15)

        def days = DateUtilExtensions.minus(cal1, cal2)
        assertEquals(-5, days)
    }

    @Test
    void testMinusCalendarAcrossYears() {
        def cal1 = Calendar.getInstance()
        cal1.set(2021, Calendar.JANUARY, 1)
        def cal2 = Calendar.getInstance()
        cal2.set(2020, Calendar.DECEMBER, 31)

        def days = DateUtilExtensions.minus(cal1, cal2)
        assertEquals(1, days)
    }

    @Test
    void testMinusDateFromDate() {
        def date1 = DateUtilExtensions.plus(date, 10)
        def days = DateUtilExtensions.minus(date1, date)
        assertEquals(10, days)
    }

    // format tests
    @Test
    void testFormatDate() {
        def originalTz = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT"))
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"))
            date = calendar.getTime()
            def result = DateUtilExtensions.format(date, "yyyy-MM-dd")
            assertEquals("2020-06-15", result)
        } finally {
            TimeZone.setDefault(originalTz)
        }
    }

    @Test
    void testFormatDateWithTimezone() {
        def result = DateUtilExtensions.format(date, "yyyy-MM-dd", TimeZone.getTimeZone("UTC"))
        assertNotNull(result)
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}"))
    }

    @Test
    void testFormatCalendar() {
        def result = DateUtilExtensions.format(calendar, "HH:mm:ss")
        assertEquals("10:30:45", result)
    }

    // getDateString, getTimeString, getDateTimeString tests
    @Test
    void testGetDateString() {
        def result = DateUtilExtensions.getDateString(date)
        assertNotNull(result)
        assertFalse(result.isEmpty())
    }

    @Test
    void testGetTimeString() {
        def result = DateUtilExtensions.getTimeString(date)
        assertNotNull(result)
        assertFalse(result.isEmpty())
    }

    @Test
    void testGetDateTimeString() {
        def result = DateUtilExtensions.getDateTimeString(date)
        assertNotNull(result)
        assertFalse(result.isEmpty())
    }

    // clearTime tests
    @Test
    void testClearTimeDate() {
        def result = DateUtilExtensions.clearTime(date)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test
    void testClearTimeSqlDate() {
        def sqlDate = new java.sql.Date(date.getTime())
        def result = DateUtilExtensions.clearTime(sqlDate)
        def cal = Calendar.getInstance()
        cal.setTime(result)
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    void testClearTimeCalendar() {
        def result = DateUtilExtensions.clearTime(calendar)
        assertEquals(0, result.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, result.get(Calendar.MINUTE))
        assertEquals(0, result.get(Calendar.SECOND))
        assertSame(calendar, result) // modifies in place
    }

    // upto tests
    @Test
    void testUptoDate() {
        def start = date
        def end = DateUtilExtensions.plus(date, 3)

        def collected = []
        DateUtilExtensions.upto(start, end, { args ->
            collected.add(args)
            return null
        } as Closure<Void>)

        assertEquals(4, collected.size()) // inclusive: 15, 16, 17, 18
    }

    @Test
    void testUptoDateThrowsWhenEndBeforeStart() {
        def start = date
        def end = DateUtilExtensions.minus(date, 1)

        assertThrows(GroovyRuntimeException, { ->
            DateUtilExtensions.upto(start, end, { args ->
                return null
            } as Closure<Void>)
        })
    }

    @Test
    void testUptoCalendar() {
        def end = DateUtilExtensions.next(DateUtilExtensions.next(calendar))

        def collected = []
        DateUtilExtensions.upto(calendar, end, { args ->
            collected.add(args)
            return null
        } as Closure<Void>)

        assertEquals(3, collected.size())
    }

    @Test
    void testUptoCalendarThrowsWhenEndBeforeStart() {
        def end = DateUtilExtensions.previous(calendar)

        assertThrows(GroovyRuntimeException, { ->
            DateUtilExtensions.upto(calendar, end, { args ->
                return null
            } as Closure<Void>)
        })
    }

    // downto tests
    @Test
    void testDowntoDate() {
        def start = date
        def end = DateUtilExtensions.minus(date, 2)

        def collected = []
        DateUtilExtensions.downto(start, end, { args ->
            collected.add(args)
            return null
        } as Closure<Void>)

        assertEquals(3, collected.size()) // inclusive: 15, 14, 13
    }

    @Test
    void testDowntoDateThrowsWhenEndAfterStart() {
        def start = date
        def end = DateUtilExtensions.plus(date, 1)

        assertThrows(GroovyRuntimeException, { ->
            DateUtilExtensions.downto(start, end, { args ->
                return null
            } as Closure<Void>)
        })
    }

    @Test
    void testDowntoCalendar() {
        def end = DateUtilExtensions.previous(DateUtilExtensions.previous(calendar))

        def collected = []
        DateUtilExtensions.downto(calendar, end, { args ->
            collected.add(args)
            return null
        } as Closure<Void>)

        assertEquals(3, collected.size())
    }

    @Test
    void testDowntoCalendarThrowsWhenEndAfterStart() {
        def end = DateUtilExtensions.next(calendar)

        assertThrows(GroovyRuntimeException, { ->
            DateUtilExtensions.downto(calendar, end, { args ->
                return null
            } as Closure<Void>)
        })
    }

    // Edge case tests
    @Test
    void testPlusZeroDays() {
        def result = DateUtilExtensions.plus(date, 0)
        assertEquals(date.getTime(), result.getTime())
    }

    @Test
    void testMinusNegativeDays() {
        def result = DateUtilExtensions.minus(date, -5)
        def expected = DateUtilExtensions.plus(date, 5)
        assertEquals(expected.getTime(), result.getTime())
    }

    @Test
    void testUptoSameDate() {
        def collected = []
        DateUtilExtensions.upto(date, date, { args ->
            collected.add(args)
            return null
        } as Closure<Void>)
        assertEquals(1, collected.size())
    }

    @Test
    void testDowntoSameDate() {
        def collected = []
        DateUtilExtensions.downto(date, date, { args ->
            collected.add(args)
            return null
        } as Closure<Void>)
        assertEquals(1, collected.size())
    }
}
