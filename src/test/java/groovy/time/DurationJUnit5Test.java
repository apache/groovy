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
package groovy.time;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Duration} class.
 */
class DurationJUnit5Test {

    @Test
    void testConstructor() {
        Duration duration = new Duration(1, 2, 3, 4, 5);
        assertEquals(1, duration.getDays());
        assertEquals(2, duration.getHours());
        assertEquals(3, duration.getMinutes());
        assertEquals(4, duration.getSeconds());
        assertEquals(5, duration.getMillis());
    }

    @Test
    void testToMilliseconds() {
        Duration duration = new Duration(1, 0, 0, 0, 0);
        assertEquals(24 * 60 * 60 * 1000L, duration.toMilliseconds());
    }

    @Test
    void testToMillisecondsComplex() {
        Duration duration = new Duration(1, 2, 3, 4, 5);
        long expected = ((((1L * 24 + 2) * 60 + 3) * 60 + 4) * 1000) + 5;
        assertEquals(expected, duration.toMilliseconds());
    }

    @Test
    void testPlusDuration() {
        Duration d1 = new Duration(1, 2, 3, 4, 5);
        Duration d2 = new Duration(1, 1, 1, 1, 1);
        Duration result = d1.plus(d2);
        assertEquals(2, result.getDays());
        assertEquals(3, result.getHours());
        assertEquals(4, result.getMinutes());
        assertEquals(5, result.getSeconds());
        assertEquals(6, result.getMillis());
    }

    @Test
    void testPlusTimeDuration() {
        Duration d = new Duration(1, 0, 0, 0, 0);
        TimeDuration td = new TimeDuration(0, 2, 30, 0, 0);
        Duration result = d.plus(td);
        assertNotNull(result);
    }

    @Test
    void testPlusDatumDependentDuration() {
        Duration d = new Duration(1, 0, 0, 0, 0);
        DatumDependentDuration ddd = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7);
        DatumDependentDuration result = d.plus(ddd);
        assertNotNull(result);
        assertEquals(1, result.getYears());
        assertEquals(2, result.getMonths());
    }

    @Test
    void testMinusDuration() {
        Duration d1 = new Duration(5, 10, 30, 45, 500);
        Duration d2 = new Duration(2, 5, 15, 20, 200);
        Duration result = d1.minus(d2);
        assertEquals(3, result.getDays());
        assertEquals(5, result.getHours());
        assertEquals(15, result.getMinutes());
        assertEquals(25, result.getSeconds());
        assertEquals(300, result.getMillis());
    }

    @Test
    void testMinusTimeDuration() {
        Duration d = new Duration(2, 5, 30, 0, 0);
        TimeDuration td = new TimeDuration(1, 2, 15, 0, 0);
        TimeDuration result = d.minus(td);
        assertEquals(1, result.getDays());
        assertEquals(3, result.getHours());
        assertEquals(15, result.getMinutes());
    }

    @Test
    void testMinusDatumDependentDuration() {
        Duration d = new Duration(10, 5, 30, 20, 100);
        DatumDependentDuration ddd = new DatumDependentDuration(1, 2, 3, 1, 10, 5, 50);
        DatumDependentDuration result = d.minus(ddd);
        assertEquals(-1, result.getYears());
        assertEquals(-2, result.getMonths());
        assertEquals(7, result.getDays());
    }

    @Test
    void testMinusTimeDatumDependentDuration() {
        Duration d = new Duration(10, 5, 30, 20, 100);
        TimeDatumDependentDuration tddd = new TimeDatumDependentDuration(1, 2, 3, 1, 10, 5, 50);
        TimeDatumDependentDuration result = d.minus(tddd);
        assertEquals(-1, result.getYears());
        assertEquals(-2, result.getMonths());
    }

    @Test
    void testGetAgo() {
        Duration duration = new Duration(1, 0, 0, 0, 0);
        Date ago = duration.getAgo();
        assertNotNull(ago);
        
        Calendar expected = Calendar.getInstance();
        expected.add(Calendar.DAY_OF_YEAR, -1);
        expected.set(Calendar.HOUR_OF_DAY, 0);
        expected.set(Calendar.MINUTE, 0);
        expected.set(Calendar.SECOND, 0);
        expected.set(Calendar.MILLISECOND, 0);
        
        assertEquals(expected.getTimeInMillis(), ago.getTime());
    }

    @Test
    void testGetFrom() {
        Duration duration = new Duration(3, 0, 0, 0, 0);
        BaseDuration.From from = duration.getFrom();
        assertNotNull(from);
        Date now = from.getNow();
        assertNotNull(now);
        
        Calendar expected = Calendar.getInstance();
        expected.add(Calendar.DAY_OF_YEAR, 3);
        expected.set(Calendar.HOUR_OF_DAY, 0);
        expected.set(Calendar.MINUTE, 0);
        expected.set(Calendar.SECOND, 0);
        expected.set(Calendar.MILLISECOND, 0);
        
        assertEquals(expected.getTimeInMillis(), now.getTime());
    }

    @Test
    void testZeroDuration() {
        Duration zero = new Duration(0, 0, 0, 0, 0);
        assertEquals(0, zero.toMilliseconds());
    }

    @Test
    void testNegativeDuration() {
        Duration negative = new Duration(-1, -2, -3, -4, -5);
        assertTrue(negative.toMilliseconds() < 0);
    }

    @Test
    void testMillisecondsOnly() {
        Duration millis = new Duration(0, 0, 0, 0, 500);
        assertEquals(500, millis.toMilliseconds());
    }

    @Test
    void testSecondsOnly() {
        Duration seconds = new Duration(0, 0, 0, 45, 0);
        assertEquals(45 * 1000L, seconds.toMilliseconds());
    }

    @Test
    void testMinutesOnly() {
        Duration minutes = new Duration(0, 0, 30, 0, 0);
        assertEquals(30 * 60 * 1000L, minutes.toMilliseconds());
    }

    @Test
    void testHoursOnly() {
        Duration hours = new Duration(0, 12, 0, 0, 0);
        assertEquals(12 * 60 * 60 * 1000L, hours.toMilliseconds());
    }

    @Test
    void testDaysOnly() {
        Duration days = new Duration(7, 0, 0, 0, 0);
        assertEquals(7 * 24 * 60 * 60 * 1000L, days.toMilliseconds());
    }

    @Test
    void testPlusWithZero() {
        Duration d = new Duration(1, 2, 3, 4, 5);
        Duration zero = new Duration(0, 0, 0, 0, 0);
        Duration result = d.plus(zero);
        assertEquals(1, result.getDays());
        assertEquals(2, result.getHours());
        assertEquals(3, result.getMinutes());
        assertEquals(4, result.getSeconds());
        assertEquals(5, result.getMillis());
    }

    @Test
    void testMinusWithZero() {
        Duration d = new Duration(1, 2, 3, 4, 5);
        Duration zero = new Duration(0, 0, 0, 0, 0);
        Duration result = d.minus(zero);
        assertEquals(1, result.getDays());
        assertEquals(2, result.getHours());
        assertEquals(3, result.getMinutes());
        assertEquals(4, result.getSeconds());
        assertEquals(5, result.getMillis());
    }

    @Test
    void testLargeDuration() {
        Duration large = new Duration(365, 23, 59, 59, 999);
        long expected = ((((365L * 24 + 23) * 60 + 59) * 60 + 59) * 1000) + 999;
        assertEquals(expected, large.toMilliseconds());
    }
}
