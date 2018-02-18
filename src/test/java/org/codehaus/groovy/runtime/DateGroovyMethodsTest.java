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

package org.codehaus.groovy.runtime;

import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

public class DateGroovyMethodsTest {
    @Test
    public void minus() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        assertEquals("20171231", sdf.format(DateGroovyMethods.minus(sdf.parse("20180101"), 1)));
    }

    @Test
    public void plus() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        assertEquals("20180101", sdf.format(DateGroovyMethods.plus(new Timestamp(sdf.parse("20171231").getTime()), 1)));
    }

    @Test
    public void next() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse("20171231"));
        assertEquals("20180101", sdf.format(DateGroovyMethods.next(calendar).getTime()));
    }

    @Test
    public void previous() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse("20180101"));
        assertEquals("20171231", sdf.format(DateGroovyMethods.previous(calendar).getTime()));
    }

    @Test
    public void calendarConversionsDefaultTimeZone() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss SSS");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse("20180115 153256 001"));

        LocalDate expectedLocalDate = LocalDate.of(2018, Month.JANUARY, 15);
        LocalTime expectedLocalTime = LocalTime.of(15, 32, 56, 1_000_000);
        LocalDateTime expectedLocalDateTime = LocalDateTime.of(expectedLocalDate, expectedLocalTime);

        assertEquals("DayOfWeek", DayOfWeek.MONDAY, DateGroovyMethods.toDayOfWeek(calendar));
        assertEquals("Month", Month.JANUARY, DateGroovyMethods.toMonth(calendar));
        assertEquals("MonthDay", MonthDay.of(Month.JANUARY, 15), DateGroovyMethods.toMonthDay(calendar));
        assertEquals("YearMonth", YearMonth.of(2018, Month.JANUARY), DateGroovyMethods.toYearMonth(calendar));
        assertEquals("Year", Year.of(2018), DateGroovyMethods.toYear(calendar));
        assertEquals("LocalDate", expectedLocalDate, DateGroovyMethods.toLocalDate(calendar));
        assertEquals("LocalTime", expectedLocalTime, DateGroovyMethods.toLocalTime(calendar));
        assertEquals("LocalDateTime", expectedLocalDateTime, DateGroovyMethods.toLocalDateTime(calendar));
        assertEquals("OffsetTime", expectedLocalTime, DateGroovyMethods.toOffsetTime(calendar).toLocalTime());
        assertEquals("OffsetDateTime", expectedLocalDateTime,
                DateGroovyMethods.toOffsetDateTime(calendar).toLocalDateTime());
        assertEquals("ZonedDateTime", expectedLocalDateTime,
                DateGroovyMethods.toZonedDateTime(calendar).toLocalDateTime());
    }

    @Test
    public void calendarConversionsDifferingTimeZones() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss SSS");
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC+0"));
        calendar.setTime(sdf.parse("20180115 153256 001"));
    }

    @Test
    public void sameCalendarAndDateConvertIdentically() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HHmmss SSS");
        Date date = sdf.parse("20180115 153256 001");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        assertEquals("DayOfWeek", DateGroovyMethods.toDayOfWeek(calendar), DateGroovyMethods.toDayOfWeek(date));
        assertEquals("Month", DateGroovyMethods.toMonth(calendar), DateGroovyMethods.toMonth(date));
        assertEquals("MonthDay", DateGroovyMethods.toMonthDay(calendar), DateGroovyMethods.toMonthDay(date));
        assertEquals("YearMonth", DateGroovyMethods.toYearMonth(calendar), DateGroovyMethods.toYearMonth(date));
        assertEquals("Year", DateGroovyMethods.toYear(calendar), DateGroovyMethods.toYear(date));
        assertEquals("LocalDate", DateGroovyMethods.toLocalDate(calendar), DateGroovyMethods.toLocalDate(date));
        assertEquals("LocalTime", DateGroovyMethods.toLocalTime(calendar), DateGroovyMethods.toLocalTime(date));
        assertEquals("LocalDateTime", DateGroovyMethods.toLocalDate(calendar), DateGroovyMethods.toLocalDate(date));
        assertEquals("OffsetTime", DateGroovyMethods.toOffsetTime(calendar), DateGroovyMethods.toOffsetTime(date));
        assertEquals("OffsetDateTime",
                DateGroovyMethods.toOffsetDateTime(calendar), DateGroovyMethods.toOffsetDateTime(date));
        assertEquals("ZonedDateTime",
                DateGroovyMethods.toZonedDateTime(calendar), DateGroovyMethods.toZonedDateTime(date));
    }
}
