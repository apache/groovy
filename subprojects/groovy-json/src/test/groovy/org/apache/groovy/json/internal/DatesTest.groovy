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
package org.apache.groovy.json.internal

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue


class DatesTest{

    // GROOVY-7462
    @Test
    void testDatesFactory() {
        Date d1 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59)

        Thread.sleep(1) // lets get some time between calling constructors

        Date d2 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59)

        assert d1 == d2
    }

    @Test
    void testDatesFactoryWithDefaultMs() {
        Date d1 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59,0)
        Date d2 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59)

        assert d1 == d2
    }

    @Test
    void testDatesFactoryEnforceDefaultMs() {
        Date d1 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59,1)
        Date d2 = Dates.toDate(TimeZone.getTimeZone("GMT"),2015,06,07,23,55,59)

        assert d1 != d2
    }

    @Test
    void testUtc() {
        def time = System.currentTimeMillis()
        def utcTime = Dates.utc(time)
        assertNotNull(utcTime)
    }

    @Test
    void testToDateWithoutMilliseconds() {
        def tz = TimeZone.getTimeZone("GMT")
        def date = Dates.toDate(tz, 2020, 6, 15, 10, 30, 45)

        assertNotNull(date)
        def cal = Calendar.getInstance(tz)
        cal.setTime(date)
        assertEquals(2020, cal.get(Calendar.YEAR))
        assertEquals(5, cal.get(Calendar.MONTH)) // June is 5 (0-indexed), but Dates uses 1-indexed
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    void testToDateWithMilliseconds() {
        def tz = TimeZone.getTimeZone("GMT")
        def date = Dates.toDate(tz, 2020, 6, 15, 10, 30, 45, 123)

        assertNotNull(date)
    }

    @Test
    void testToDateWithDifferentTimezones() {
        def gmt = TimeZone.getTimeZone("GMT")
        def est = TimeZone.getTimeZone("EST")

        def gmtDate = Dates.toDate(gmt, 2020, 6, 15, 12, 0, 0)
        def estDate = Dates.toDate(est, 2020, 6, 15, 12, 0, 0)

        // Different timezones should produce different times
        assert gmtDate.getTime() != estDate.getTime()
    }

    @Test
    void testIsISO8601ShortFormat() {
        // Format: 1994-11-05T08:15:30Z
        char[] chars = "1994-11-05T08:15:30Z".toCharArray()
        assertTrue(Dates.isISO8601(chars, 0, chars.length))
    }

    @Test
    void testIsISO8601LongFormat() {
        // Format: 1994-11-05T08:15:30-05:00
        char[] chars = "1994-11-05T08:15:30-05:00".toCharArray()
        assertTrue(Dates.isISO8601(chars, 0, chars.length))
    }

    @Test
    void testIsISO8601LongFormatPlus() {
        // Format: 1994-11-05T08:15:30+05:00
        char[] chars = "1994-11-05T08:15:30+05:00".toCharArray()
        assertTrue(Dates.isISO8601(chars, 0, chars.length))
    }

    @Test
    void testIsISO8601InvalidLength() {
        char[] chars = "1994-11-05".toCharArray()
        assertFalse(Dates.isISO8601(chars, 0, chars.length))
    }

    @Test
    void testIsISO8601InvalidFormat() {
        char[] chars = "1994/11/05T08:15:30Z".toCharArray()
        assertFalse(Dates.isISO8601(chars, 0, chars.length))
    }

    @Test
    void testIsISO8601QuickCheckJsonTimeLength() {
        char[] chars = "2013-12-14T01:55:33.412Z".toCharArray()
        assertTrue(Dates.isISO8601QuickCheck(chars, 0, chars.length))
    }

    @Test
    void testIsISO8601QuickCheckShortLength() {
        char[] chars = "1994-11-05T08:15:30Z".toCharArray()
        assertTrue(Dates.isISO8601QuickCheck(chars, 0, chars.length))
    }

    @Test
    void testIsISO8601QuickCheckLongLength() {
        char[] chars = "1994-11-05T08:15:30-05:00".toCharArray()
        assertTrue(Dates.isISO8601QuickCheck(chars, 0, chars.length))
    }

    @Test
    void testIsISO8601QuickCheckWithColonAt16() {
        // Length >= 17 and has colon at position 16
        char[] chars = "1994-11-05T08:15:30".toCharArray()
        assertTrue(Dates.isISO8601QuickCheck(chars, 0, chars.length))
    }

    @Test
    void testIsISO8601QuickCheckTooShort() {
        char[] chars = "1994-11-05".toCharArray()
        assertFalse(Dates.isISO8601QuickCheck(chars, 0, chars.length))
    }

    @Test
    void testIsJsonDateValid() {
        // Format: 2013-12-14T01:55:33.412Z
        char[] chars = "2013-12-14T01:55:33.412Z".toCharArray()
        assertTrue(Dates.isJsonDate(chars, 0, chars.length))
    }

    @Test
    void testIsJsonDateWithPlus() {
        // Format with + for milliseconds separator
        char[] chars = "2013-12-14T01:55:33+412Z".toCharArray()
        assertTrue(Dates.isJsonDate(chars, 0, chars.length))
    }

    @Test
    void testIsJsonDateInvalidLength() {
        char[] chars = "2013-12-14T01:55:33Z".toCharArray()
        assertFalse(Dates.isJsonDate(chars, 0, chars.length))
    }

    @Test
    void testIsJsonDateInvalidFormat() {
        char[] chars = "2013/12/14T01:55:33.412Z".toCharArray()
        assertFalse(Dates.isJsonDate(chars, 0, chars.length))
    }

    @Test
    void testIsJsonDateInvalidSeparator() {
        // Neither . nor + at position 19
        char[] chars = "2013-12-14T01:55:33X412Z".toCharArray()
        assertFalse(Dates.isJsonDate(chars, 0, chars.length))
    }

    @Test
    void testFromISO8601ShortFormat() {
        char[] chars = "1994-11-05T08:15:30Z".toCharArray()
        def date = Dates.fromISO8601(chars, 0, chars.length)

        assertNotNull(date)
    }

    @Test
    void testFromISO8601LongFormat() {
        char[] chars = "1994-11-05T08:15:30-05:00".toCharArray()
        def date = Dates.fromISO8601(chars, 0, chars.length)

        assertNotNull(date)
    }

    @Test
    void testFromISO8601InvalidFormat() {
        char[] chars = "invalid-date-format".toCharArray()
        def date = Dates.fromISO8601(chars, 0, chars.length)

        assertNull(date)
    }

    @Test
    void testFromISO8601WithOffset() {
        char[] chars = "2020-06-15T10:30:45".toCharArray()
        // Too short, should return null
        def date = Dates.fromISO8601(chars, 0, chars.length)
        assertNull(date)
    }

    @Test
    void testFromJsonDateValid() {
        char[] chars = "2013-12-14T01:55:33.412Z".toCharArray()
        def date = Dates.fromJsonDate(chars, 0, chars.length)

        assertNotNull(date)
    }

    @Test
    void testFromJsonDateInvalidFormat() {
        char[] chars = "invalid-json-date!!!".toCharArray()
        def date = Dates.fromJsonDate(chars, 0, chars.length)

        assertNull(date)
    }

    @Test
    void testFromJsonDateWrongLength() {
        char[] chars = "2013-12-14T01:55:33Z".toCharArray()
        def date = Dates.fromJsonDate(chars, 0, chars.length)

        assertNull(date)
    }

    @Test
    void testFromISO8601WithSubstring() {
        def fullString = "prefix1994-11-05T08:15:30Zsuffix"
        char[] chars = fullString.toCharArray()
        // Parse from position 6 to 26 (the ISO8601 part)
        def date = Dates.fromISO8601(chars, 6, 26)

        assertNotNull(date)
    }

    @Test
    void testFromJsonDateWithSubstring() {
        def fullString = "prefix2013-12-14T01:55:33.412Zsuffix"
        char[] chars = fullString.toCharArray()
        // Parse from position 6 to 30 (the JSON date part)
        def date = Dates.fromJsonDate(chars, 6, 30)

        assertNotNull(date)
    }

    @Test
    void testIsISO8601WithSubstring() {
        def fullString = "prefix1994-11-05T08:15:30Zsuffix"
        char[] chars = fullString.toCharArray()
        assertTrue(Dates.isISO8601(chars, 6, 26))
    }

    @Test
    void testIsJsonDateWithSubstring() {
        def fullString = "prefix2013-12-14T01:55:33.412Zsuffix"
        char[] chars = fullString.toCharArray()
        assertTrue(Dates.isJsonDate(chars, 6, 30))
    }

    @Test
    void testFromISO8601ParsesCorrectly() {
        char[] chars = "2020-06-15T10:30:45Z".toCharArray()
        def date = Dates.fromISO8601(chars, 0, chars.length)

        assertNotNull(date)
        def cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        cal.setTime(date)
        assertEquals(2020, cal.get(Calendar.YEAR))
        assertEquals(5, cal.get(Calendar.MONTH)) // June is 5 (0-indexed)
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(10, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, cal.get(Calendar.MINUTE))
        assertEquals(45, cal.get(Calendar.SECOND))
    }

    @Test
    void testFromJsonDateParsesCorrectly() {
        char[] chars = "2020-06-15T10:30:45.123Z".toCharArray()
        def date = Dates.fromJsonDate(chars, 0, chars.length)

        assertNotNull(date)
        def cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        cal.setTime(date)
        assertEquals(2020, cal.get(Calendar.YEAR))
        assertEquals(15, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    void testConstantsHaveCorrectValues() {
        assertEquals(20, Dates.SHORT_ISO_8601_TIME_LENGTH)
        assertEquals(25, Dates.LONG_ISO_8601_TIME_LENGTH)
        assertEquals(24, Dates.JSON_TIME_LENGTH)
    }
}
