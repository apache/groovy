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
 * Unit tests for {@link TimeDatumDependentDuration} class.
 */
class TimeDatumDependentDurationTest {

    @Test
    void testConstructor() {
        def duration = new TimeDatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        assertEquals(1, duration.getYears())
        assertEquals(2, duration.getMonths())
        assertEquals(3, duration.getDays())
        assertEquals(4, duration.getHours())
        assertEquals(5, duration.getMinutes())
        assertEquals(6, duration.getSeconds())
        assertEquals(7, duration.getMillis())
    }

    @Test
    void testPlusDuration() {
        def tddd = new TimeDatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def d = new Duration(1, 2, 3, 4, 5)
        def result = tddd.plus(d)
        assertTrue(result instanceof TimeDatumDependentDuration)
        assertEquals(1, result.getYears())
        assertEquals(2, result.getMonths())
        assertEquals(4, result.getDays())
        assertEquals(6, result.getHours())
        assertEquals(8, result.getMinutes())
        assertEquals(10, result.getSeconds())
        assertEquals(12, result.getMillis())
    }

    @Test
    void testPlusDatumDependentDuration() {
        def tddd1 = new TimeDatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def ddd2 = new DatumDependentDuration(2, 3, 4, 5, 6, 7, 8)
        def result = tddd1.plus(ddd2)
        assertTrue(result instanceof TimeDatumDependentDuration)
        assertEquals(3, result.getYears())
        assertEquals(5, result.getMonths())
        assertEquals(7, result.getDays())
        assertEquals(9, result.getHours())
        assertEquals(11, result.getMinutes())
        assertEquals(13, result.getSeconds())
        assertEquals(15, result.getMillis())
    }

    @Test
    void testMinusDuration() {
        def tddd = new TimeDatumDependentDuration(1, 2, 10, 10, 30, 45, 500)
        def d = new Duration(3, 2, 10, 15, 100)
        def result = tddd.minus(d)
        assertTrue(result instanceof TimeDatumDependentDuration)
        assertEquals(1, result.getYears())
        assertEquals(2, result.getMonths())
        assertEquals(7, result.getDays())
        assertEquals(8, result.getHours())
        assertEquals(20, result.getMinutes())
        assertEquals(30, result.getSeconds())
        assertEquals(400, result.getMillis())
    }

    @Test
    void testMinusDatumDependentDuration() {
        def tddd1 = new TimeDatumDependentDuration(5, 6, 10, 10, 30, 45, 500)
        def ddd2 = new DatumDependentDuration(2, 3, 4, 5, 15, 20, 100)
        def result = tddd1.minus(ddd2)
        assertTrue(result instanceof TimeDatumDependentDuration)
        assertEquals(3, result.getYears())
        assertEquals(3, result.getMonths())
        assertEquals(6, result.getDays())
        assertEquals(5, result.getHours())
        assertEquals(15, result.getMinutes())
        assertEquals(25, result.getSeconds())
        assertEquals(400, result.getMillis())
    }

    @Test
    void testGetFrom() {
        def duration = new TimeDatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def from = duration.getFrom()
        assertNotNull(from)
        def now = from.getNow()
        assertNotNull(now)

        def expected = Calendar.getInstance()
        expected.add(Calendar.YEAR, 1)
        expected.add(Calendar.MONTH, 2)
        expected.add(Calendar.DAY_OF_YEAR, 3)
        expected.add(Calendar.HOUR_OF_DAY, 4)
        expected.add(Calendar.MINUTE, 5)
        expected.add(Calendar.SECOND, 6)
        expected.add(Calendar.MILLISECOND, 7)

        def tolerance = 5000L
        assertTrue(Math.abs(expected.getTimeInMillis() - now.getTime()) < tolerance)
    }

    @Test
    void testZeroDuration() {
        def zero = new TimeDatumDependentDuration(0, 0, 0, 0, 0, 0, 0)
        assertEquals(0, zero.getYears())
        assertEquals(0, zero.getMonths())
        assertEquals(0, zero.getDays())
        assertEquals(0, zero.getHours())
        assertEquals(0, zero.getMinutes())
        assertEquals(0, zero.getSeconds())
        assertEquals(0, zero.getMillis())
    }

    @Test
    void testNegativeDuration() {
        def negative = new TimeDatumDependentDuration(-1, -2, -3, -4, -5, -6, -7)
        assertEquals(-1, negative.getYears())
        assertEquals(-2, negative.getMonths())
        assertEquals(-3, negative.getDays())
        assertEquals(-4, negative.getHours())
        assertEquals(-5, negative.getMinutes())
        assertEquals(-6, negative.getSeconds())
        assertEquals(-7, negative.getMillis())
    }

    @Test
    void testPlusTimeDatumDependentDuration() {
        def tddd1 = new TimeDatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def tddd2 = new TimeDatumDependentDuration(1, 1, 1, 1, 1, 1, 1)
        def result = tddd1.plus(tddd2)
        assertEquals(2, result.getYears())
        assertEquals(3, result.getMonths())
        assertEquals(4, result.getDays())
        assertEquals(5, result.getHours())
        assertEquals(6, result.getMinutes())
        assertEquals(7, result.getSeconds())
        assertEquals(8, result.getMillis())
    }

    @Test
    void testMinusTimeDatumDependentDuration() {
        def tddd1 = new TimeDatumDependentDuration(5, 6, 7, 8, 9, 10, 11)
        def tddd2 = new TimeDatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def result = tddd1.minus(tddd2)
        assertEquals(4, result.getYears())
        assertEquals(4, result.getMonths())
        assertEquals(4, result.getDays())
        assertEquals(4, result.getHours())
        assertEquals(4, result.getMinutes())
        assertEquals(4, result.getSeconds())
        assertEquals(4, result.getMillis())
    }
}
