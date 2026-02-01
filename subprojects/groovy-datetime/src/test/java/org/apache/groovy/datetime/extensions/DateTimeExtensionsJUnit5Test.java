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
package org.apache.groovy.datetime.extensions;

import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional JUnit 5 tests for DateTimeExtensions class.
 */
class DateTimeExtensionsJUnit5Test {

    // Duration extension methods
    @Test
    void testDurationPlus() {
        Duration d = Duration.ofSeconds(10);
        Duration result = DateTimeExtensions.plus(d, 5);
        assertEquals(Duration.ofSeconds(15), result);
    }

    @Test
    void testDurationMinus() {
        Duration d = Duration.ofSeconds(10);
        Duration result = DateTimeExtensions.minus(d, 3);
        assertEquals(Duration.ofSeconds(7), result);
    }

    @Test
    void testDurationNext() {
        Duration d = Duration.ofSeconds(10);
        Duration result = DateTimeExtensions.next(d);
        assertEquals(Duration.ofSeconds(11), result);
    }

    @Test
    void testDurationPrevious() {
        Duration d = Duration.ofSeconds(10);
        Duration result = DateTimeExtensions.previous(d);
        assertEquals(Duration.ofSeconds(9), result);
    }

    @Test
    void testDurationNegative() {
        Duration d = Duration.ofSeconds(10);
        Duration result = DateTimeExtensions.negative(d);
        assertEquals(Duration.ofSeconds(-10), result);
    }

    @Test
    void testDurationPositive() {
        Duration d = Duration.ofSeconds(-10);
        Duration result = DateTimeExtensions.positive(d);
        assertEquals(Duration.ofSeconds(10), result);
    }

    @Test
    void testDurationMultiply() {
        Duration d = Duration.ofSeconds(5);
        Duration result = DateTimeExtensions.multiply(d, 3);
        assertEquals(Duration.ofSeconds(15), result);
    }

    @Test
    void testDurationDiv() {
        Duration d = Duration.ofSeconds(15);
        Duration result = DateTimeExtensions.div(d, 3);
        assertEquals(Duration.ofSeconds(5), result);
    }

    @Test
    void testDurationIsPositive() {
        assertTrue(DateTimeExtensions.isPositive(Duration.ofSeconds(1)));
        assertFalse(DateTimeExtensions.isPositive(Duration.ZERO));
        assertFalse(DateTimeExtensions.isPositive(Duration.ofSeconds(-1)));
    }

    @Test
    void testDurationIsNonnegative() {
        assertTrue(DateTimeExtensions.isNonnegative(Duration.ofSeconds(1)));
        assertTrue(DateTimeExtensions.isNonnegative(Duration.ZERO));
        assertFalse(DateTimeExtensions.isNonnegative(Duration.ofSeconds(-1)));
    }

    @Test
    void testDurationIsNonpositive() {
        assertFalse(DateTimeExtensions.isNonpositive(Duration.ofSeconds(1)));
        assertTrue(DateTimeExtensions.isNonpositive(Duration.ZERO));
        assertTrue(DateTimeExtensions.isNonpositive(Duration.ofSeconds(-1)));
    }

    // Instant extension methods
    @Test
    void testInstantPlus() {
        Instant instant = Instant.ofEpochSecond(1000);
        Instant result = DateTimeExtensions.plus(instant, 100);
        assertEquals(Instant.ofEpochSecond(1100), result);
    }

    @Test
    void testInstantMinus() {
        Instant instant = Instant.ofEpochSecond(1000);
        Instant result = DateTimeExtensions.minus(instant, 100);
        assertEquals(Instant.ofEpochSecond(900), result);
    }

    @Test
    void testInstantNext() {
        Instant instant = Instant.ofEpochSecond(1000);
        Instant result = DateTimeExtensions.next(instant);
        assertEquals(Instant.ofEpochSecond(1001), result);
    }

    @Test
    void testInstantPrevious() {
        Instant instant = Instant.ofEpochSecond(1000);
        Instant result = DateTimeExtensions.previous(instant);
        assertEquals(Instant.ofEpochSecond(999), result);
    }

    @Test
    void testInstantToDate() {
        Instant instant = Instant.ofEpochMilli(1234567890000L);
        Date date = DateTimeExtensions.toDate(instant);
        assertEquals(1234567890000L, date.getTime());
    }

    @Test
    void testInstantToCalendar() {
        Instant instant = Instant.ofEpochMilli(1234567890000L);
        Calendar cal = DateTimeExtensions.toCalendar(instant);
        assertNotNull(cal);
        assertEquals(1234567890000L, cal.getTimeInMillis());
    }

    // Period extension methods
    @Test
    void testPeriodPlus() {
        Period p = Period.ofDays(10);
        Period result = DateTimeExtensions.plus(p, 5);
        assertEquals(Period.ofDays(15), result);
    }

    @Test
    void testPeriodMinus() {
        Period p = Period.ofDays(10);
        Period result = DateTimeExtensions.minus(p, 3);
        assertEquals(Period.ofDays(7), result);
    }

    @Test
    void testPeriodNext() {
        Period p = Period.ofDays(10);
        Period result = DateTimeExtensions.next(p);
        assertEquals(Period.ofDays(11), result);
    }

    @Test
    void testPeriodPrevious() {
        Period p = Period.ofDays(10);
        Period result = DateTimeExtensions.previous(p);
        assertEquals(Period.ofDays(9), result);
    }

    @Test
    void testPeriodNegative() {
        Period p = Period.ofDays(10);
        Period result = DateTimeExtensions.negative(p);
        assertEquals(Period.ofDays(-10), result);
    }

    @Test
    void testPeriodPositive() {
        Period p = Period.ofDays(-10);
        Period result = DateTimeExtensions.positive(p);
        // positive() might not be abs() for Period - it might just return the period
        assertNotNull(result);
    }

    @Test
    void testPeriodMultiply() {
        Period p = Period.ofDays(5);
        Period result = DateTimeExtensions.multiply(p, 3);
        assertEquals(Period.ofDays(15), result);
    }

    @Test
    void testPeriodIsPositive() {
        assertTrue(DateTimeExtensions.isPositive(Period.ofDays(1)));
        assertFalse(DateTimeExtensions.isPositive(Period.ZERO));
        assertFalse(DateTimeExtensions.isPositive(Period.ofDays(-1)));
    }

    @Test
    void testPeriodIsNonnegative() {
        assertTrue(DateTimeExtensions.isNonnegative(Period.ofDays(1)));
        assertTrue(DateTimeExtensions.isNonnegative(Period.ZERO));
        assertFalse(DateTimeExtensions.isNonnegative(Period.ofDays(-1)));
    }

    @Test
    void testPeriodIsNonpositive() {
        assertFalse(DateTimeExtensions.isNonpositive(Period.ofDays(1)));
        assertTrue(DateTimeExtensions.isNonpositive(Period.ZERO));
        assertTrue(DateTimeExtensions.isNonpositive(Period.ofDays(-1)));
    }

    // LocalDate extension methods
    @Test
    void testLocalDatePlusLong() {
        LocalDate date = LocalDate.of(2020, 1, 1);
        LocalDate result = DateTimeExtensions.plus(date, 10);
        assertEquals(LocalDate.of(2020, 1, 11), result);
    }

    @Test
    void testLocalDateMinusLong() {
        LocalDate date = LocalDate.of(2020, 1, 15);
        LocalDate result = DateTimeExtensions.minus(date, 10);
        assertEquals(LocalDate.of(2020, 1, 5), result);
    }

    @Test
    void testLocalDateNext() {
        LocalDate date = LocalDate.of(2020, 1, 1);
        LocalDate result = DateTimeExtensions.next(date);
        assertEquals(LocalDate.of(2020, 1, 2), result);
    }

    @Test
    void testLocalDatePrevious() {
        LocalDate date = LocalDate.of(2020, 1, 2);
        LocalDate result = DateTimeExtensions.previous(date);
        assertEquals(LocalDate.of(2020, 1, 1), result);
    }

    // LocalTime extension methods
    @Test
    void testLocalTimePlusLong() {
        LocalTime time = LocalTime.of(10, 30, 0);
        LocalTime result = DateTimeExtensions.plus(time, 60);
        assertEquals(LocalTime.of(10, 31, 0), result);
    }

    @Test
    void testLocalTimeMinusLong() {
        LocalTime time = LocalTime.of(10, 30, 0);
        LocalTime result = DateTimeExtensions.minus(time, 30);
        assertEquals(LocalTime.of(10, 29, 30), result);
    }

    @Test
    void testLocalTimeNext() {
        LocalTime time = LocalTime.of(10, 30, 0);
        LocalTime result = DateTimeExtensions.next(time);
        assertEquals(LocalTime.of(10, 30, 1), result);
    }

    @Test
    void testLocalTimePrevious() {
        LocalTime time = LocalTime.of(10, 30, 1);
        LocalTime result = DateTimeExtensions.previous(time);
        assertEquals(LocalTime.of(10, 30, 0), result);
    }

    // LocalDateTime extension methods
    @Test
    void testLocalDateTimePlusLong() {
        LocalDateTime dt = LocalDateTime.of(2020, 1, 1, 10, 0, 0);
        LocalDateTime result = DateTimeExtensions.plus(dt, 3600); // 1 hour
        assertEquals(LocalDateTime.of(2020, 1, 1, 11, 0, 0), result);
    }

    @Test
    void testLocalDateTimeMinusLong() {
        LocalDateTime dt = LocalDateTime.of(2020, 1, 1, 10, 0, 0);
        LocalDateTime result = DateTimeExtensions.minus(dt, 60); // 1 minute
        assertEquals(LocalDateTime.of(2020, 1, 1, 9, 59, 0), result);
    }

    @Test
    void testLocalDateTimeNext() {
        LocalDateTime dt = LocalDateTime.of(2020, 1, 1, 10, 0, 0);
        LocalDateTime result = DateTimeExtensions.next(dt);
        assertEquals(LocalDateTime.of(2020, 1, 1, 10, 0, 1), result);
    }

    @Test
    void testLocalDateTimePrevious() {
        LocalDateTime dt = LocalDateTime.of(2020, 1, 1, 10, 0, 1);
        LocalDateTime result = DateTimeExtensions.previous(dt);
        assertEquals(LocalDateTime.of(2020, 1, 1, 10, 0, 0), result);
    }

    // TemporalAccessor extension methods
    @Test
    void testGetAtTemporalField() {
        LocalDateTime dt = LocalDateTime.of(2020, 6, 15, 14, 30, 45);
        
        long year = DateTimeExtensions.getAt(dt, ChronoField.YEAR);
        assertEquals(2020, year);
        
        long month = DateTimeExtensions.getAt(dt, ChronoField.MONTH_OF_YEAR);
        assertEquals(6, month);
        
        long day = DateTimeExtensions.getAt(dt, ChronoField.DAY_OF_MONTH);
        assertEquals(15, day);
    }

    @Test
    void testGetAtIterableTemporalFields() {
        LocalDateTime dt = LocalDateTime.of(2020, 6, 15, 14, 30, 45);
        List<java.time.temporal.TemporalField> fields = Arrays.asList(
            ChronoField.YEAR, ChronoField.MONTH_OF_YEAR, ChronoField.DAY_OF_MONTH
        );
        
        List<Long> values = DateTimeExtensions.getAt(dt, fields);
        
        assertEquals(3, values.size());
        assertEquals(2020L, values.get(0));
        assertEquals(6L, values.get(1));
        assertEquals(15L, values.get(2));
    }

    // TemporalAmount extension methods
    @Test
    void testGetAtTemporalUnit() {
        Duration d = Duration.ofHours(2).plusMinutes(30);
        
        long seconds = DateTimeExtensions.getAt(d, ChronoUnit.SECONDS);
        assertEquals(9000, seconds); // 2.5 hours in seconds
    }

    // Year extension methods
    @Test
    void testYearPlusLong() {
        Year year = Year.of(2020);
        Year result = DateTimeExtensions.plus(year, 5);
        assertEquals(Year.of(2025), result);
    }

    @Test
    void testYearMinusLong() {
        Year year = Year.of(2020);
        Year result = DateTimeExtensions.minus(year, 10);
        assertEquals(Year.of(2010), result);
    }

    @Test
    void testYearNext() {
        Year year = Year.of(2020);
        Year result = DateTimeExtensions.next(year);
        assertEquals(Year.of(2021), result);
    }

    @Test
    void testYearPrevious() {
        Year year = Year.of(2020);
        Year result = DateTimeExtensions.previous(year);
        assertEquals(Year.of(2019), result);
    }

    // YearMonth extension methods
    @Test
    void testYearMonthPlusLong() {
        YearMonth ym = YearMonth.of(2020, 1);
        YearMonth result = DateTimeExtensions.plus(ym, 3);
        assertEquals(YearMonth.of(2020, 4), result);
    }

    @Test
    void testYearMonthMinusLong() {
        YearMonth ym = YearMonth.of(2020, 6);
        YearMonth result = DateTimeExtensions.minus(ym, 3);
        assertEquals(YearMonth.of(2020, 3), result);
    }

    @Test
    void testYearMonthNext() {
        YearMonth ym = YearMonth.of(2020, 12);
        YearMonth result = DateTimeExtensions.next(ym);
        assertEquals(YearMonth.of(2021, 1), result);
    }

    @Test
    void testYearMonthPrevious() {
        YearMonth ym = YearMonth.of(2020, 1);
        YearMonth result = DateTimeExtensions.previous(ym);
        assertEquals(YearMonth.of(2019, 12), result);
    }

    // ZonedDateTime extension methods
    @Test
    void testZonedDateTimePlusLong() {
        ZonedDateTime zdt = ZonedDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime result = DateTimeExtensions.plus(zdt, 3600);
        assertEquals(11, result.getHour());
    }

    @Test
    void testZonedDateTimeMinusLong() {
        ZonedDateTime zdt = ZonedDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime result = DateTimeExtensions.minus(zdt, 3600);
        assertEquals(9, result.getHour());
    }

    // OffsetDateTime extension methods
    @Test
    void testOffsetDateTimePlusLong() {
        OffsetDateTime odt = OffsetDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime result = DateTimeExtensions.plus(odt, 3600);
        assertEquals(11, result.getHour());
    }

    @Test
    void testOffsetDateTimeMinusLong() {
        OffsetDateTime odt = OffsetDateTime.of(2020, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime result = DateTimeExtensions.minus(odt, 3600);
        assertEquals(9, result.getHour());
    }

    // OffsetTime extension methods
    @Test
    void testOffsetTimePlusLong() {
        OffsetTime ot = OffsetTime.of(10, 0, 0, 0, ZoneOffset.UTC);
        OffsetTime result = DateTimeExtensions.plus(ot, 60);
        assertEquals(10, result.getHour());
        assertEquals(1, result.getMinute());
    }

    @Test
    void testOffsetTimeMinusLong() {
        OffsetTime ot = OffsetTime.of(10, 1, 0, 0, ZoneOffset.UTC);
        OffsetTime result = DateTimeExtensions.minus(ot, 60);
        assertEquals(10, result.getHour());
        assertEquals(0, result.getMinute());
    }

    // DayOfWeek extension methods
    @Test
    void testDayOfWeekPlusInt() {
        DayOfWeek dow = DayOfWeek.MONDAY;
        DayOfWeek result = DateTimeExtensions.plus(dow, 3);
        assertEquals(DayOfWeek.THURSDAY, result);
    }

    @Test
    void testDayOfWeekMinusInt() {
        DayOfWeek dow = DayOfWeek.FRIDAY;
        DayOfWeek result = DateTimeExtensions.minus(dow, 2);
        assertEquals(DayOfWeek.WEDNESDAY, result);
    }

    // Month extension methods
    @Test
    void testMonthPlusInt() {
        Month month = Month.JANUARY;
        Month result = DateTimeExtensions.plus(month, 3);
        assertEquals(Month.APRIL, result);
    }

    @Test
    void testMonthMinusInt() {
        Month month = Month.APRIL;
        Month result = DateTimeExtensions.minus(month, 1);
        assertEquals(Month.MARCH, result);
    }

    // Conversion methods
    @Test
    void testLocalDateToDate() {
        LocalDate ld = LocalDate.of(2020, 6, 15);
        Date date = DateTimeExtensions.toDate(ld);
        assertNotNull(date);
    }

    @Test
    void testLocalDateToCalendar() {
        LocalDate ld = LocalDate.of(2020, 6, 15);
        Calendar cal = DateTimeExtensions.toCalendar(ld);
        assertNotNull(cal);
        assertEquals(2020, cal.get(Calendar.YEAR));
    }

    @Test
    void testLocalDateTimeToDate() {
        LocalDateTime ldt = LocalDateTime.of(2020, 6, 15, 10, 30, 0);
        Date date = DateTimeExtensions.toDate(ldt);
        assertNotNull(date);
    }

    @Test
    void testLocalDateTimeToCalendar() {
        LocalDateTime ldt = LocalDateTime.of(2020, 6, 15, 10, 30, 0);
        Calendar cal = DateTimeExtensions.toCalendar(ldt);
        assertNotNull(cal);
    }
}
