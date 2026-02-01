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
package org.apache.groovy.json.internal;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 tests for Dates utility class.
 */
class DatesJUnit5Test {

    // utc tests
    @Test
    void testUtc() {
        long time = System.currentTimeMillis();
        long utcTime = Dates.utc(time);
        assertNotNull(utcTime);
    }

    // toDate tests
    @Test
    void testToDateWithoutMilliseconds() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Date date = Dates.toDate(tz, 2020, 6, 15, 10, 30, 45);
        
        assertNotNull(date);
        java.util.Calendar cal = java.util.Calendar.getInstance(tz);
        cal.setTime(date);
        assertEquals(2020, cal.get(java.util.Calendar.YEAR));
        assertEquals(5, cal.get(java.util.Calendar.MONTH)); // June is 5 (0-indexed), but Dates uses 1-indexed
        assertEquals(15, cal.get(java.util.Calendar.DAY_OF_MONTH));
    }

    @Test
    void testToDateWithMilliseconds() {
        TimeZone tz = TimeZone.getTimeZone("GMT");
        Date date = Dates.toDate(tz, 2020, 6, 15, 10, 30, 45, 123);
        
        assertNotNull(date);
    }

    @Test
    void testToDateWithDifferentTimezones() {
        TimeZone gmt = TimeZone.getTimeZone("GMT");
        TimeZone est = TimeZone.getTimeZone("EST");
        
        Date gmtDate = Dates.toDate(gmt, 2020, 6, 15, 12, 0, 0);
        Date estDate = Dates.toDate(est, 2020, 6, 15, 12, 0, 0);
        
        // Different timezones should produce different times
        assertNotEquals(gmtDate.getTime(), estDate.getTime());
    }

    // isISO8601 tests
    @Test
    void testIsISO8601ShortFormat() {
        // Format: 1994-11-05T08:15:30Z
        char[] chars = "1994-11-05T08:15:30Z".toCharArray();
        assertTrue(Dates.isISO8601(chars, 0, chars.length));
    }

    @Test
    void testIsISO8601LongFormat() {
        // Format: 1994-11-05T08:15:30-05:00
        char[] chars = "1994-11-05T08:15:30-05:00".toCharArray();
        assertTrue(Dates.isISO8601(chars, 0, chars.length));
    }

    @Test
    void testIsISO8601LongFormatPlus() {
        // Format: 1994-11-05T08:15:30+05:00
        char[] chars = "1994-11-05T08:15:30+05:00".toCharArray();
        assertTrue(Dates.isISO8601(chars, 0, chars.length));
    }

    @Test
    void testIsISO8601InvalidLength() {
        char[] chars = "1994-11-05".toCharArray();
        assertFalse(Dates.isISO8601(chars, 0, chars.length));
    }

    @Test
    void testIsISO8601InvalidFormat() {
        char[] chars = "1994/11/05T08:15:30Z".toCharArray();
        assertFalse(Dates.isISO8601(chars, 0, chars.length));
    }

    // isISO8601QuickCheck tests
    @Test
    void testIsISO8601QuickCheckJsonTimeLength() {
        char[] chars = "2013-12-14T01:55:33.412Z".toCharArray();
        assertTrue(Dates.isISO8601QuickCheck(chars, 0, chars.length));
    }

    @Test
    void testIsISO8601QuickCheckShortLength() {
        char[] chars = "1994-11-05T08:15:30Z".toCharArray();
        assertTrue(Dates.isISO8601QuickCheck(chars, 0, chars.length));
    }

    @Test
    void testIsISO8601QuickCheckLongLength() {
        char[] chars = "1994-11-05T08:15:30-05:00".toCharArray();
        assertTrue(Dates.isISO8601QuickCheck(chars, 0, chars.length));
    }

    @Test
    void testIsISO8601QuickCheckWithColonAt16() {
        // Length >= 17 and has colon at position 16
        char[] chars = "1994-11-05T08:15:30".toCharArray();
        assertTrue(Dates.isISO8601QuickCheck(chars, 0, chars.length));
    }

    @Test
    void testIsISO8601QuickCheckTooShort() {
        char[] chars = "1994-11-05".toCharArray();
        assertFalse(Dates.isISO8601QuickCheck(chars, 0, chars.length));
    }

    // isJsonDate tests
    @Test
    void testIsJsonDateValid() {
        // Format: 2013-12-14T01:55:33.412Z
        char[] chars = "2013-12-14T01:55:33.412Z".toCharArray();
        assertTrue(Dates.isJsonDate(chars, 0, chars.length));
    }

    @Test
    void testIsJsonDateWithPlus() {
        // Format with + for milliseconds separator
        char[] chars = "2013-12-14T01:55:33+412Z".toCharArray();
        assertTrue(Dates.isJsonDate(chars, 0, chars.length));
    }

    @Test
    void testIsJsonDateInvalidLength() {
        char[] chars = "2013-12-14T01:55:33Z".toCharArray();
        assertFalse(Dates.isJsonDate(chars, 0, chars.length));
    }

    @Test
    void testIsJsonDateInvalidFormat() {
        char[] chars = "2013/12/14T01:55:33.412Z".toCharArray();
        assertFalse(Dates.isJsonDate(chars, 0, chars.length));
    }

    @Test
    void testIsJsonDateInvalidSeparator() {
        // Neither . nor + at position 19
        char[] chars = "2013-12-14T01:55:33X412Z".toCharArray();
        assertFalse(Dates.isJsonDate(chars, 0, chars.length));
    }

    // fromISO8601 tests
    @Test
    void testFromISO8601ShortFormat() {
        char[] chars = "1994-11-05T08:15:30Z".toCharArray();
        Date date = Dates.fromISO8601(chars, 0, chars.length);
        
        assertNotNull(date);
    }

    @Test
    void testFromISO8601LongFormat() {
        char[] chars = "1994-11-05T08:15:30-05:00".toCharArray();
        Date date = Dates.fromISO8601(chars, 0, chars.length);
        
        assertNotNull(date);
    }

    @Test
    void testFromISO8601InvalidFormat() {
        char[] chars = "invalid-date-format".toCharArray();
        Date date = Dates.fromISO8601(chars, 0, chars.length);
        
        assertNull(date);
    }

    @Test
    void testFromISO8601WithOffset() {
        char[] chars = "2020-06-15T10:30:45".toCharArray();
        // Too short, should return null
        Date date = Dates.fromISO8601(chars, 0, chars.length);
        assertNull(date);
    }

    // fromJsonDate tests
    @Test
    void testFromJsonDateValid() {
        char[] chars = "2013-12-14T01:55:33.412Z".toCharArray();
        Date date = Dates.fromJsonDate(chars, 0, chars.length);
        
        assertNotNull(date);
    }

    @Test
    void testFromJsonDateInvalidFormat() {
        char[] chars = "invalid-json-date!!!".toCharArray();
        Date date = Dates.fromJsonDate(chars, 0, chars.length);
        
        assertNull(date);
    }

    @Test
    void testFromJsonDateWrongLength() {
        char[] chars = "2013-12-14T01:55:33Z".toCharArray();
        Date date = Dates.fromJsonDate(chars, 0, chars.length);
        
        assertNull(date);
    }

    // Edge case tests
    @Test
    void testFromISO8601WithSubstring() {
        String fullString = "prefix1994-11-05T08:15:30Zsuffix";
        char[] chars = fullString.toCharArray();
        // Parse from position 6 to 26 (the ISO8601 part)
        Date date = Dates.fromISO8601(chars, 6, 26);
        
        assertNotNull(date);
    }

    @Test
    void testFromJsonDateWithSubstring() {
        String fullString = "prefix2013-12-14T01:55:33.412Zsuffix";
        char[] chars = fullString.toCharArray();
        // Parse from position 6 to 30 (the JSON date part)
        Date date = Dates.fromJsonDate(chars, 6, 30);
        
        assertNotNull(date);
    }

    @Test
    void testIsISO8601WithSubstring() {
        String fullString = "prefix1994-11-05T08:15:30Zsuffix";
        char[] chars = fullString.toCharArray();
        assertTrue(Dates.isISO8601(chars, 6, 26));
    }

    @Test
    void testIsJsonDateWithSubstring() {
        String fullString = "prefix2013-12-14T01:55:33.412Zsuffix";
        char[] chars = fullString.toCharArray();
        assertTrue(Dates.isJsonDate(chars, 6, 30));
    }

    // Specific date parsing verification
    @Test
    void testFromISO8601ParsesCorrectly() {
        char[] chars = "2020-06-15T10:30:45Z".toCharArray();
        Date date = Dates.fromISO8601(chars, 0, chars.length);
        
        assertNotNull(date);
        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(2020, cal.get(java.util.Calendar.YEAR));
        assertEquals(5, cal.get(java.util.Calendar.MONTH)); // June is 5 (0-indexed)
        assertEquals(15, cal.get(java.util.Calendar.DAY_OF_MONTH));
        assertEquals(10, cal.get(java.util.Calendar.HOUR_OF_DAY));
        assertEquals(30, cal.get(java.util.Calendar.MINUTE));
        assertEquals(45, cal.get(java.util.Calendar.SECOND));
    }

    @Test
    void testFromJsonDateParsesCorrectly() {
        char[] chars = "2020-06-15T10:30:45.123Z".toCharArray();
        Date date = Dates.fromJsonDate(chars, 0, chars.length);
        
        assertNotNull(date);
        java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        assertEquals(2020, cal.get(java.util.Calendar.YEAR));
        assertEquals(15, cal.get(java.util.Calendar.DAY_OF_MONTH));
    }

    // Constants verification
    @Test
    void testConstantsHaveCorrectValues() {
        assertEquals(20, Dates.SHORT_ISO_8601_TIME_LENGTH);
        assertEquals(25, Dates.LONG_ISO_8601_TIME_LENGTH);
        assertEquals(24, Dates.JSON_TIME_LENGTH);
    }
}
