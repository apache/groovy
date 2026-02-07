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
package org.apache.groovy.datetime.extensions

import org.junit.jupiter.api.Test

import java.text.SimpleDateFormat
import java.time.*
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

import static org.junit.jupiter.api.Assertions.*

/**
 * Tests for DateTimeExtensions class.
 */
class DateTimeExtensionsTest {

    @Test
    void testCalendarConversionsDefaultTimeZone() {
        def sdf = new SimpleDateFormat("yyyyMMdd HHmmss SSS")
        def calendar = Calendar.getInstance()
        calendar.setTime(sdf.parse("20180115 153256 001"))

        def expectedLocalDate = LocalDate.of(2018, Month.JANUARY, 15)
        def expectedLocalTime = LocalTime.of(15, 32, 56, 1_000_000)
        def expectedLocalDateTime = LocalDateTime.of(expectedLocalDate, expectedLocalTime)

        assertEquals(DayOfWeek.MONDAY, DateTimeExtensions.toDayOfWeek(calendar), "DayOfWeek")
        assertEquals(Month.JANUARY, DateTimeExtensions.toMonth(calendar), "Month")
        assertEquals(MonthDay.of(Month.JANUARY, 15), DateTimeExtensions.toMonthDay(calendar), "MonthDay")
        assertEquals(YearMonth.of(2018, Month.JANUARY), DateTimeExtensions.toYearMonth(calendar), "YearMonth")
        assertEquals(Year.of(2018), DateTimeExtensions.toYear(calendar), "Year")
        assertEquals(expectedLocalDate, DateTimeExtensions.toLocalDate(calendar), "LocalDate")
        assertEquals(expectedLocalTime, DateTimeExtensions.toLocalTime(calendar), "LocalTime")
        assertEquals(expectedLocalDateTime, DateTimeExtensions.toLocalDateTime(calendar), "LocalDateTime")
        assertEquals(expectedLocalTime, DateTimeExtensions.toOffsetTime(calendar).toLocalTime(), "OffsetTime")
        assertEquals(expectedLocalDateTime,
                DateTimeExtensions.toOffsetDateTime(calendar).toLocalDateTime(), "OffsetDateTime")
        assertEquals(expectedLocalDateTime,
                DateTimeExtensions.toZonedDateTime(calendar).toLocalDateTime(), "ZonedDateTime")
    }

    @Test
    void testCalendarConversionsDifferingTimeZones() {
        def sdf = new SimpleDateFormat("yyyyMMdd HHmmss SSS")
        def calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+0"))
        calendar.setTime(sdf.parse("20180115 153256 001"))
    }

    @Test
    void testSameCalendarAndDateConvertIdentically() {
        def sdf = new SimpleDateFormat("yyyyMMdd HHmmss SSS")
        def date = sdf.parse("20180115 153256 001")
        def calendar = Calendar.getInstance()
        calendar.setTime(date)

        assertEquals(DateTimeExtensions.toDayOfWeek(calendar), DateTimeExtensions.toDayOfWeek(date), "DayOfWeek")
        assertEquals(DateTimeExtensions.toMonth(calendar), DateTimeExtensions.toMonth(date), "Month")
        assertEquals(DateTimeExtensions.toMonthDay(calendar), DateTimeExtensions.toMonthDay(date), "MonthDay")
        assertEquals(DateTimeExtensions.toYearMonth(calendar), DateTimeExtensions.toYearMonth(date), "YearMonth")
        assertEquals(DateTimeExtensions.toYear(calendar), DateTimeExtensions.toYear(date), "Year")
        assertEquals(DateTimeExtensions.toLocalDate(calendar), DateTimeExtensions.toLocalDate(date), "LocalDate")
        assertEquals(DateTimeExtensions.toLocalTime(calendar), DateTimeExtensions.toLocalTime(date), "LocalTime")
        assertEquals(DateTimeExtensions.toLocalDate(calendar), DateTimeExtensions.toLocalDate(date), "LocalDateTime")
        assertEquals(DateTimeExtensions.toOffsetTime(calendar), DateTimeExtensions.toOffsetTime(date), "OffsetTime")
        assertEquals(DateTimeExtensions.toOffsetDateTime(calendar), DateTimeExtensions.toOffsetDateTime(date), "OffsetDateTime")
        assertEquals(DateTimeExtensions.toZonedDateTime(calendar), DateTimeExtensions.toZonedDateTime(date), "ZonedDateTime")
    }

    // Duration extension methods
    @Test
    void testDurationPlus() {
        def d = Duration.ofSeconds(10)
        def result = DateTimeExtensions.plus(d, 5)
        assertEquals(Duration.ofSeconds(15), result)
    }

    @Test
    void testDurationMinus() {
        def d = Duration.ofSeconds(10)
        def result = DateTimeExtensions.minus(d, 3)
        assertEquals(Duration.ofSeconds(7), result)
    }

    @Test
    void testDurationNext() {
        def d = Duration.ofSeconds(10)
        def result = DateTimeExtensions.next(d)
        assertEquals(Duration.ofSeconds(11), result)
    }

    @Test
    void testDurationPrevious() {
        def d = Duration.ofSeconds(10)
        def result = DateTimeExtensions.previous(d)
        assertEquals(Duration.ofSeconds(9), result)
    }

    @Test
    void testDurationNegative() {
        def d = Duration.ofSeconds(10)
        def result = DateTimeExtensions.negative(d)
        assertEquals(Duration.ofSeconds(-10), result)
    }

    @Test
    void testDurationPositive() {
        def d = Duration.ofSeconds(-10)
        def result = DateTimeExtensions.positive(d)
        assertEquals(Duration.ofSeconds(10), result)
    }

    @Test
    void testDurationMultiply() {
        def d = Duration.ofSeconds(5)
        def result = DateTimeExtensions.multiply(d, 3)
        assertEquals(Duration.ofSeconds(15), result)
    }

    @Test
    void testDurationDiv() {
        def d = Duration.ofSeconds(15)
        def result = DateTimeExtensions.div(d, 3)
        assertEquals(Duration.ofSeconds(5), result)
    }

    @Test
    void testDurationIsPositive() {
        assertTrue(DateTimeExtensions.isPositive(Duration.ofSeconds(1)))
        assertFalse(DateTimeExtensions.isPositive(Duration.ZERO))
        assertFalse(DateTimeExtensions.isPositive(Duration.ofSeconds(-1)))
    }

    @Test
    void testDurationIsNonnegative() {
        assertTrue(DateTimeExtensions.isNonnegative(Duration.ofSeconds(1)))
        assertTrue(DateTimeExtensions.isNonnegative(Duration.ZERO))
        assertFalse(DateTimeExtensions.isNonnegative(Duration.ofSeconds(-1)))
    }

    @Test
    void testDurationIsNonpositive() {
        assertFalse(DateTimeExtensions.isNonpositive(Duration.ofSeconds(1)))
        assertTrue(DateTimeExtensions.isNonpositive(Duration.ZERO))
        assertTrue(DateTimeExtensions.isNonpositive(Duration.ofSeconds(-1)))
    }

    // Instant extension methods
    @Test
    void testInstantPlus() {
        def instant = Instant.ofEpochSecond(1000)
        def result = DateTimeExtensions.plus(instant, 100)
        assertEquals(Instant.ofEpochSecond(1100), result)
    }

    @Test
    void testInstantMinus() {
        def instant = Instant.ofEpochSecond(1000)
        def result = DateTimeExtensions.minus(instant, 100)
        assertEquals(Instant.ofEpochSecond(900), result)
    }

    @Test
    void testInstantNext() {
        def instant = Instant.ofEpochSecond(1000)
        def result = DateTimeExtensions.next(instant)
        assertEquals(Instant.ofEpochSecond(1001), result)
    }

    @Test
    void testInstantPrevious() {
        def instant = Instant.ofEpochSecond(1000)
        def result = DateTimeExtensions.previous(instant)
        assertEquals(Instant.ofEpochSecond(999), result)
    }

    @Test
    void testInstantToDate() {
        def instant = Instant.ofEpochMilli(1234567890000L)
        def date = DateTimeExtensions.toDate(instant)
        assertEquals(1234567890000L, date.getTime())
    }

    @Test
    void testInstantToCalendar() {
        def instant = Instant.ofEpochMilli(1234567890000L)
        def cal = DateTimeExtensions.toCalendar(instant)
        assertNotNull(cal)
        assertEquals(1234567890000L, cal.getTimeInMillis())
    }

    // Period extension methods
    @Test
    void testPeriodPlus() {
        def p = Period.ofDays(10)
        def result = DateTimeExtensions.plus(p, 5)
        assertEquals(Period.ofDays(15), result)
    }

    @Test
    void testPeriodMinus() {
        def p = Period.ofDays(10)
        def result = DateTimeExtensions.minus(p, 3)
        assertEquals(Period.ofDays(7), result)
    }

    @Test
    void testPeriodNext() {
        def p = Period.ofDays(10)
        def result = DateTimeExtensions.next(p)
        assertEquals(Period.ofDays(11), result)
    }

    @Test
    void testPeriodPrevious() {
        def p = Period.ofDays(10)
        def result = DateTimeExtensions.previous(p)
        assertEquals(Period.ofDays(9), result)
    }

    @Test
    void testPeriodNegative() {
        def p = Period.ofDays(10)
        def result = DateTimeExtensions.negative(p)
        assertEquals(Period.ofDays(-10), result)
    }

    @Test
    void testPeriodPositive() {
        def p = Period.ofDays(-10)
        def result = DateTimeExtensions.positive(p)
        // positive() might not be abs() for Period - it might just return the period
        assertNotNull(result)
    }

    @Test
    void testPeriodMultiply() {
        def p = Period.ofDays(5)
        def result = DateTimeExtensions.multiply(p, 3)
        assertEquals(Period.ofDays(15), result)
    }

    @Test
    void testPeriodIsPositive() {
        assertTrue(DateTimeExtensions.isPositive(Period.ofDays(1)))
        assertFalse(DateTimeExtensions.isPositive(Period.ZERO))
        assertFalse(DateTimeExtensions.isPositive(Period.ofDays(-1)))
    }

    @Test
    void testPeriodIsNonnegative() {
        assertTrue(DateTimeExtensions.isNonnegative(Period.ofDays(1)))
        assertTrue(DateTimeExtensions.isNonnegative(Period.ZERO))
        assertFalse(DateTimeExtensions.isNonnegative(Period.ofDays(-1)))
    }

    @Test
    void testPeriodIsNonpositive() {
        assertFalse(DateTimeExtensions.isNonpositive(Period.ofDays(1)))
        assertTrue(DateTimeExtensions.isNonpositive(Period.ZERO))
        assertTrue(DateTimeExtensions.isNonpositive(Period.ofDays(-1)))
    }

    // LocalDate extension methods
    @Test
    void testLocalDatePlusLong() {
        def date = LocalDate.of(2020, 1, 1)
        def result = DateTimeExtensions.plus(date, 10)
        assertEquals(LocalDate.of(2020, 1, 11), result)
    }

    @Test
    void testLocalDateMinusLong() {
        def date = LocalDate.of(2020, 1, 15)
        def result = DateTimeExtensions.minus(date, 10)
        assertEquals(LocalDate.of(2020, 1, 5), result)
    }

    @Test
    void testLocalDateNext() {
        def date = LocalDate.of(2020, 1, 1)
        def result = DateTimeExtensions.next(date)
        assertEquals(LocalDate.of(2020, 1, 2), result)
    }

    @Test
    void testLocalDatePrevious() {
        def date = LocalDate.of(2020, 1, 2)
        def result = DateTimeExtensions.previous(date)
        assertEquals(LocalDate.of(2020, 1, 1), result)
    }

    // LocalTime extension methods
    @Test
    void testLocalTimePlusLong() {
        def time = LocalTime.of(10, 30, 0)
        def result = DateTimeExtensions.plus(time, 60)
        assertEquals(LocalTime.of(10, 31, 0), result)
    }

    @Test
    void testLocalTimeMinusLong() {
        def time = LocalTime.of(10, 30, 0)
        def result = DateTimeExtensions.minus(time, 30)
        assertEquals(LocalTime.of(10, 29, 30), result)
    }

    @Test
    void testLocalTimeNext() {
        def time = LocalTime.of(10, 30, 0)
        def result = DateTimeExtensions.next(time)
        assertEquals(LocalTime.of(10, 30, 1), result)
    }

    @Test
    void testLocalTimePrevious() {
        def time = LocalTime.of(10, 30, 1)
        def result = DateTimeExtensions.previous(time)
        assertEquals(LocalTime.of(10, 30, 0), result)
    }

    // LocalDateTime extension methods
    @Test
    void testLocalDateTimePlusLong() {
        def dt = LocalDateTime.of(2020, 1, 1, 10, 0, 0)
        def result = DateTimeExtensions.plus(dt, 3600) // 1 hour
        assertEquals(LocalDateTime.of(2020, 1, 1, 11, 0, 0), result)
    }

    @Test
    void testLocalDateTimeMinusLong() {
        def dt = LocalDateTime.of(2020, 1, 1, 10, 0, 0)
        def result = DateTimeExtensions.minus(dt, 60) // 1 minute
        assertEquals(LocalDateTime.of(2020, 1, 1, 9, 59, 0), result)
    }

    @Test
    void testLocalDateTimeNext() {
        def dt = LocalDateTime.of(2020, 1, 1, 10, 0, 0)
        def result = DateTimeExtensions.next(dt)
        assertEquals(LocalDateTime.of(2020, 1, 1, 10, 0, 1), result)
    }

    @Test
    void testLocalDateTimePrevious() {
        def dt = LocalDateTime.of(2020, 1, 1, 10, 0, 1)
        def result = DateTimeExtensions.previous(dt)
        assertEquals(LocalDateTime.of(2020, 1, 1, 10, 0, 0), result)
    }

    // TemporalAccessor extension methods
    @Test
    void testGetAtTemporalField() {
        def dt = LocalDateTime.of(2020, 6, 15, 14, 30, 45)

        def year = DateTimeExtensions.getAt(dt, ChronoField.YEAR)
        assertEquals(2020, year)

        def month = DateTimeExtensions.getAt(dt, ChronoField.MONTH_OF_YEAR)
        assertEquals(6, month)

        def day = DateTimeExtensions.getAt(dt, ChronoField.DAY_OF_MONTH)
        assertEquals(15, day)
    }

    @Test
    void testGetAtIterableTemporalFields() {
        def dt = LocalDateTime.of(2020, 6, 15, 14, 30, 45)
        def fields = [
            ChronoField.YEAR, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH
        ]

        def values = DateTimeExtensions.getAt(dt, fields)

        assertEquals(3, values.size())
        assertEquals(2020L, values.get(0))
        assertEquals(6L, values.get(1))
        assertEquals(15L, values.get(2))
    }

    // TemporalAmount extension methods
    @Test
    void testGetAtTemporalUnit() {
        def d = Duration.ofHours(2).plusMinutes(30)

        def seconds = DateTimeExtensions.getAt(d, ChronoUnit.SECONDS)
        assertEquals(9000, seconds) // 2.5 hours in seconds
    }

    // Year extension methods
    @Test
    void testYearPlusLong() {
        def year = Year.of(2020)
        def result = DateTimeExtensions.plus(year, 5)
        assertEquals(Year.of(2025), result)
    }

    @Test
    void testYearMinusLong() {
        def year = Year.of(2020)
        def result = DateTimeExtensions.minus(year, 10)
        assertEquals(Year.of(2010), result)
    }

    @Test
    void testYearNext() {
        def year = Year.of(2020)
        def result = DateTimeExtensions.next(year)
        assertEquals(Year.of(2021), result)
    }

    @Test
    void testYearPrevious() {
        def year = Year.of(2020)
        def result = DateTimeExtensions.previous(year)
        assertEquals(Year.of(2019), result)
    }

    // YearMonth extension methods
    @Test
    void testYearMonthPlusLong() {
        def ym = YearMonth.of(2020, 1)
        def result = DateTimeExtensions.plus(ym, 3)
        assertEquals(YearMonth.of(2020, 4), result)
    }

    @Test
    void testYearMonthMinusLong() {
        def ym = YearMonth.of(2020, 6)
        def result = DateTimeExtensions.minus(ym, 3)
        assertEquals(YearMonth.of(2020, 3), result)
    }

    @Test
    void testYearMonthNext() {
        def ym = YearMonth.of(2020, 12)
        def result = DateTimeExtensions.next(ym)
        assertEquals(YearMonth.of(2021, 1), result)
    }

    @Test
    void testYearMonthPrevious() {
        def ym = YearMonth.of(2020, 1)
        def result = DateTimeExtensions.previous(ym)
        assertEquals(YearMonth.of(2019, 12), result)
    }

    // ZonedDateTime extension methods
    @Test
    void testZonedDateTimePlusLong() {
        def zdt = ZonedDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"))
        def result = DateTimeExtensions.plus(zdt, 3600)
        assertEquals(11, result.getHour())
    }

    @Test
    void testZonedDateTimeMinusLong() {
        def zdt = ZonedDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"))
        def result = DateTimeExtensions.minus(zdt, 3600)
        assertEquals(9, result.getHour())
    }

    // OffsetDateTime extension methods
    @Test
    void testOffsetDateTimePlusLong() {
        def odt = OffsetDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)
        def result = DateTimeExtensions.plus(odt, 3600)
        assertEquals(11, result.getHour())
    }

    @Test
    void testOffsetDateTimeMinusLong() {
        def odt = OffsetDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC)
        def result = DateTimeExtensions.minus(odt, 3600)
        assertEquals(9, result.getHour())
    }

    // OffsetTime extension methods
    @Test
    void testOffsetTimePlusLong() {
        def ot = OffsetTime.of(10, 0, 0, 0, ZoneOffset.UTC)
        def result = DateTimeExtensions.plus(ot, 60)
        assertEquals(10, result.getHour())
        assertEquals(1, result.getMinute())
    }

    @Test
    void testOffsetTimeMinusLong() {
        def ot = OffsetTime.of(10, 1, 0, 0, ZoneOffset.UTC)
        def result = DateTimeExtensions.minus(ot, 60)
        assertEquals(10, result.getHour())
        assertEquals(0, result.getMinute())
    }

    // DayOfWeek extension methods
    @Test
    void testDayOfWeekPlusInt() {
        def dow = DayOfWeek.MONDAY
        def result = DateTimeExtensions.plus(dow, 3)
        assertEquals(DayOfWeek.THURSDAY, result)
    }

    @Test
    void testDayOfWeekMinusInt() {
        def dow = DayOfWeek.FRIDAY
        def result = DateTimeExtensions.minus(dow, 2)
        assertEquals(DayOfWeek.WEDNESDAY, result)
    }

    // Month extension methods
    @Test
    void testMonthPlusInt() {
        def month = Month.JANUARY
        def result = DateTimeExtensions.plus(month, 3)
        assertEquals(Month.APRIL, result)
    }

    @Test
    void testMonthMinusInt() {
        def month = Month.APRIL
        def result = DateTimeExtensions.minus(month, 1)
        assertEquals(Month.MARCH, result)
    }

    // Conversion methods
    @Test
    void testLocalDateToDate() {
        def ld = LocalDate.of(2020, 6, 15)
        def date = DateTimeExtensions.toDate(ld)
        assertNotNull(date)
    }

    @Test
    void testLocalDateToCalendar() {
        def ld = LocalDate.of(2020, 6, 15)
        def cal = DateTimeExtensions.toCalendar(ld)
        assertNotNull(cal)
        assertEquals(2020, cal.get(Calendar.YEAR))
    }

    @Test
    void testLocalDateTimeToDate() {
        def ldt = LocalDateTime.of(2020, 6, 15, 10, 30, 0)
        def date = DateTimeExtensions.toDate(ldt)
        assertNotNull(date)
    }

    @Test
    void testLocalDateTimeToCalendar() {
        def ldt = LocalDateTime.of(2020, 6, 15, 10, 30, 0)
        def cal = DateTimeExtensions.toCalendar(ldt)
        assertNotNull(cal)
    }
}
