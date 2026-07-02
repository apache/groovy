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
package org.apache.groovy.dateutil

import org.junit.jupiter.api.Test

import java.time.temporal.ChronoUnit

import static java.util.Calendar.DAY_OF_YEAR
import static java.util.Calendar.MONTH
import static java.util.Calendar.WEEK_OF_YEAR
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests for the dequirked {@code org.apache.groovy.dateutil} Duration hierarchy.
 */
class DurationTest {

    @Test
    void testFixedDurationArithmetic() {
        use(TimeCategory) {
            def oneDay = 2.days - 1.day
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000)

            oneDay = 2.days - 1.day + 24.hours - 1440.minutes
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000)
        }
    }

    @Test
    void testDurationToString() {
        use(TimeCategory) {
            assert (0.years).toString() == "0"
            assert (3.years + 8.months + 4.days + 2.hours + 5.minutes + 12.milliseconds).toString() ==
                    "3 years, 8 months, 4 days, 2 hours, 5 minutes, 0.012 seconds"
            assert (4.days + 2.hours + 5.minutes + 12.milliseconds).toString() == "4 days, 2 hours, 5 minutes, 0.012 seconds"
            assert (4.days + 2.hours + 5.minutes).toString() == "4 days, 2 hours, 5 minutes"
            assert (5.seconds).toString() == "5.000 seconds"
            assert ((-12).milliseconds).toString() == "-0.012 seconds"
            assert (5.seconds + 12.milliseconds).toString() == "5.012 seconds"
            assert (5.seconds + 1012.milliseconds).toString() == "6.012 seconds"
        }
    }

    @Test
    void testDurationComparison() {
        use(TimeCategory) {
            assert 1.week < 2.weeks
            assert 3.weeks <= 4.weeks
            assert 10.seconds == 10.seconds
            assert 4.months >= 1.month
            assert 10.days > 2.days
            assert 3.months > 2.months
            assert 1.second < 1.year
            assert 1.day > 1000.seconds
            assert 10.weeks < 10.years
        }
    }

    @Test
    void testConstructor() {
        def duration = new Duration(1, 2, 3, 4, 5)
        assertEquals(1, duration.days)
        assertEquals(2, duration.hours)
        assertEquals(3, duration.minutes)
        assertEquals(4, duration.seconds)
        assertEquals(5, duration.millis)
    }

    @Test
    void testToMilliseconds() {
        assertEquals(24 * 60 * 60 * 1000L, new Duration(1, 0, 0, 0, 0).toMilliseconds())
        def d = new Duration(1, 2, 3, 4, 5)
        assertEquals(((((1L * 24 + 2) * 60 + 3) * 60 + 4) * 1000) + 5, d.toMilliseconds())
    }

    @Test
    void testDatumDependentToMillisecondsIsDeterministic() { // DEQUIRK B
        // years/months use fixed ChronoUnit estimates; no dependence on the current date
        assertEquals(ChronoUnit.YEARS.duration.toMillis(), new DatumDependentDuration(1, 0, 0, 0, 0, 0, 0).toMilliseconds())
        assertEquals(ChronoUnit.MONTHS.duration.toMillis(), new DatumDependentDuration(0, 1, 0, 0, 0, 0, 0).toMilliseconds())
        // a year is exactly twelve estimated months
        assertEquals(new DatumDependentDuration(1, 0, 0, 0, 0, 0, 0).toMilliseconds(),
                new DatumDependentDuration(0, 12, 0, 0, 0, 0, 0).toMilliseconds())
    }

    @Test
    void testPlusDuration() {
        def result = new Duration(1, 2, 3, 4, 5).plus(new Duration(1, 1, 1, 1, 1))
        assertEquals(2, result.days); assertEquals(3, result.hours); assertEquals(4, result.minutes)
        assertEquals(5, result.seconds); assertEquals(6, result.millis)
    }

    @Test
    void testPlusDatumDependentDuration() {
        def result = new Duration(1, 0, 0, 0, 0).plus(new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7))
        assertNotNull(result)
        assertEquals(1, result.years); assertEquals(2, result.months)
    }

    @Test
    void testMinusDatumDependentDuration() {
        def result = new Duration(10, 5, 30, 20, 100).minus(new DatumDependentDuration(1, 2, 3, 1, 10, 5, 50))
        assertEquals(-1, result.years); assertEquals(-2, result.months); assertEquals(7, result.days)
    }

    @Test
    void testGetAgoPreservesTimeOfDay() { // DEQUIRK A
        def ago = new Duration(1, 0, 0, 0, 0).getAgo()
        def expected = Calendar.instance
        expected.add(DAY_OF_YEAR, -1)
        assertTrue Math.abs(expected.timeInMillis - ago.time) < 2000
    }

    @Test
    void testGetFromPreservesTimeOfDay() { // DEQUIRK A
        def now = new Duration(3, 0, 0, 0, 0).from.now
        def expected = Calendar.instance
        expected.add(DAY_OF_YEAR, 3)
        assertTrue Math.abs(expected.timeInMillis - now.time) < 2000
    }

    @Test
    void testMinutesAgo() { // See GROOVY-3687 - was already un-quirky for time durations
        use(TimeCategory) {
            def now = Calendar.instance
            def before = 10.minutes.ago
            now.add(Calendar.MINUTE, -11)
            assertTrue now.timeInMillis < before.time
        }
    }

    @Test
    void testDatumDependentArithmetic() {
        use(TimeCategory) {
            def start = Calendar.instance
            def date = new Date(start.time.time)

            date += 2.months
            start.add MONTH, 2
            assertEquals start.time, date

            date += 5.weeks
            start.add WEEK_OF_YEAR, 5
            assertEquals start.time, date

            date -= (52.days + 123.minutes)
            start.add DAY_OF_YEAR, -52
            start.add Calendar.MINUTE, -123
            assertEquals start.time, date
        }
    }
}
