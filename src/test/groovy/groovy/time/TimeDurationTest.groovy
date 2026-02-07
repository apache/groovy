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
package groovy.time

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

/**
 * Unit tests for {@link TimeDuration} class.
 */
class TimeDurationTest {

    @Test
    void testConstructorWithoutDays() {
        def duration = new TimeDuration(2, 30, 45, 100)
        assertEquals(0, duration.getDays())
        assertEquals(2, duration.getHours())
        assertEquals(30, duration.getMinutes())
        assertEquals(45, duration.getSeconds())
        assertEquals(100, duration.getMillis())
    }

    @Test
    void testConstructorWithDays() {
        def duration = new TimeDuration(1, 2, 30, 45, 100)
        assertEquals(1, duration.getDays())
        assertEquals(2, duration.getHours())
        assertEquals(30, duration.getMinutes())
        assertEquals(45, duration.getSeconds())
        assertEquals(100, duration.getMillis())
    }

    @Test
    void testToMilliseconds() {
        def duration = new TimeDuration(1, 2, 3, 4)
        def expected = (((1L * 60) + 2) * 60 + 3) * 1000 + 4
        assertEquals(expected, duration.toMilliseconds())
    }

    @Test
    void testPlusDuration() {
        // TimeDuration constructor: hours, minutes, seconds, millis
        def td1 = new TimeDuration(1, 2, 30, 100)
        // Duration constructor: days, hours, minutes, seconds, millis
        def d2 = new Duration(1, 1, 15, 20, 50)
        def result = td1.plus(d2)
        assertTrue(result instanceof TimeDuration)
        // td1: days=0, hours=1, minutes=2, seconds=30, millis=100
        // d2: days=1, hours=1, minutes=15, seconds=20, millis=50
        // result: days=1, hours=2, minutes=17, seconds=50, millis=150 (no normalization)
        assertEquals(1, result.getDays())
        assertEquals(2, result.getHours())
        assertEquals(17, result.getMinutes())
        assertEquals(50, result.getSeconds())
        assertEquals(150, result.getMillis())
    }

    @Test
    void testPlusDatumDependentDuration() {
        def td = new TimeDuration(1, 2, 30, 45, 100)
        def ddd = new DatumDependentDuration(2, 3, 4, 5, 15, 10, 50)
        def result = td.plus(ddd)
        assertTrue(result instanceof TimeDatumDependentDuration)
        assertEquals(2, result.getYears())
        assertEquals(3, result.getMonths())
        assertEquals(5, result.getDays())
        assertEquals(7, result.getHours())
        assertEquals(45, result.getMinutes())
        assertEquals(55, result.getSeconds())
        assertEquals(150, result.getMillis())
    }

    @Test
    void testMinusDuration() {
        def td1 = new TimeDuration(2, 5, 30, 45, 100)
        def d2 = new Duration(1, 2, 15, 20, 50)
        def result = td1.minus(d2)
        assertTrue(result instanceof TimeDuration)
        assertEquals(1, result.getDays())
        assertEquals(3, result.getHours())
        assertEquals(15, result.getMinutes())
        assertEquals(25, result.getSeconds())
        assertEquals(50, result.getMillis())
    }

    @Test
    void testMinusDatumDependentDuration() {
        def td = new TimeDuration(2, 5, 30, 45, 100)
        def ddd = new DatumDependentDuration(1, 2, 1, 2, 15, 20, 50)
        def result = td.minus(ddd)
        assertTrue(result instanceof TimeDatumDependentDuration)
        assertEquals(-1, result.getYears())
        assertEquals(-2, result.getMonths())
        assertEquals(1, result.getDays())
        assertEquals(3, result.getHours())
        assertEquals(15, result.getMinutes())
        assertEquals(25, result.getSeconds())
        assertEquals(50, result.getMillis())
    }

    @Test
    void testGetAgo() {
        def duration = new TimeDuration(0, 1, 0, 0, 0)
        def ago = duration.getAgo()
        assertNotNull(ago)

        // The time should be approximately 1 hour ago (with some tolerance for test execution)
        def expectedTime = System.currentTimeMillis() - (60 * 60 * 1000L)
        def actualTime = ago.getTime()
        assertTrue(Math.abs(expectedTime - actualTime) < 5000, "Time should be approximately 1 hour ago")
    }

    @Test
    void testGetAgoWithAllComponents() {
        def duration = new TimeDuration(1, 2, 30, 45, 500)
        def ago = duration.getAgo()
        assertNotNull(ago)

        def expected = Calendar.getInstance()
        expected.add(Calendar.DAY_OF_YEAR, -1)
        expected.add(Calendar.HOUR_OF_DAY, -2)
        expected.add(Calendar.MINUTE, -30)
        expected.add(Calendar.SECOND, -45)
        expected.add(Calendar.MILLISECOND, -500)

        // Allow some tolerance for test execution time
        def tolerance = 5000L
        assertTrue(Math.abs(expected.getTimeInMillis() - ago.getTime()) < tolerance)
    }

    @Test
    void testGetFrom() {
        def duration = new TimeDuration(0, 1, 0, 0, 0)
        def from = duration.getFrom()
        assertNotNull(from)
        def now = from.getNow()
        assertNotNull(now)

        // The time should be approximately 1 hour from now
        def expectedTime = System.currentTimeMillis() + (60 * 60 * 1000L)
        def actualTime = now.getTime()
        assertTrue(Math.abs(expectedTime - actualTime) < 5000, "Time should be approximately 1 hour from now")
    }

    @Test
    void testGetFromWithAllComponents() {
        def duration = new TimeDuration(1, 2, 30, 45, 500)
        def from = duration.getFrom()
        def now = from.getNow()
        assertNotNull(now)

        def expected = Calendar.getInstance()
        expected.add(Calendar.DAY_OF_YEAR, 1)
        expected.add(Calendar.HOUR_OF_DAY, 2)
        expected.add(Calendar.MINUTE, 30)
        expected.add(Calendar.SECOND, 45)
        expected.add(Calendar.MILLISECOND, 500)

        def tolerance = 5000L
        assertTrue(Math.abs(expected.getTimeInMillis() - now.getTime()) < tolerance)
    }

    @Test
    void testZeroTimeDuration() {
        def zero = new TimeDuration(0, 0, 0, 0)
        assertEquals(0, zero.toMilliseconds())
    }

    @Test
    void testNegativeTimeDuration() {
        def negative = new TimeDuration(-1, -30, -15, -500)
        assertTrue(negative.toMilliseconds() < 0)
    }

    @Test
    void testOnlyMillis() {
        def millis = new TimeDuration(0, 0, 0, 500)
        assertEquals(500, millis.toMilliseconds())
    }

    @Test
    void testOnlySeconds() {
        def seconds = new TimeDuration(0, 0, 30, 0)
        assertEquals(30 * 1000L, seconds.toMilliseconds())
    }

    @Test
    void testOnlyMinutes() {
        def minutes = new TimeDuration(0, 15, 0, 0)
        assertEquals(15 * 60 * 1000L, minutes.toMilliseconds())
    }

    @Test
    void testOnlyHours() {
        def hours = new TimeDuration(3, 0, 0, 0)
        assertEquals(3 * 60 * 60 * 1000L, hours.toMilliseconds())
    }
}
