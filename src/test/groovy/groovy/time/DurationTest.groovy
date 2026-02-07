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

import static java.util.Calendar.DAY_OF_YEAR
import static java.util.Calendar.HOUR
import static java.util.Calendar.HOUR_OF_DAY
import static java.util.Calendar.MILLISECOND
import static java.util.Calendar.MINUTE
import static java.util.Calendar.MONTH
import static java.util.Calendar.SECOND
import static java.util.Calendar.WEEK_OF_YEAR
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class DurationTest {
    @Test
    void testFixedDurationArithmetic() {
        use(TimeCategory) {
            def oneDay = 2.days - 1.day
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000):  \
                 "Expected ${24 * 60 * 60 * 1000} but was ${oneDay.toMilliseconds()}"

            oneDay = 2.days - 1.day + 24.hours - 1440.minutes
            assert oneDay.toMilliseconds() == (24 * 60 * 60 * 1000):  \
                 "Expected ${24 * 60 * 60 * 1000} but was ${oneDay.toMilliseconds()}"
        }
    }

    @Test
    void testDurationToString() {
        use(TimeCategory) {
            assert (0.years).toString() == "0"
            assert (3.years + 8.months + 4.days + 2.hours + 5.minutes + 12.milliseconds).toString() == "3 years, 8 months, 4 days, 2 hours, 5 minutes, 0.012 seconds"
            assert (4.days + 2.hours + 5.minutes + 12.milliseconds).toString() == "4 days, 2 hours, 5 minutes, 0.012 seconds"
            assert (4.days + 2.hours + 5.minutes).toString() == "4 days, 2 hours, 5 minutes"
            assert (5.seconds).toString() == "5.000 seconds"
            assert ((-12).milliseconds).toString() == "-0.012 seconds"
            assert (5.seconds + 12.milliseconds).toString() == "5.012 seconds"
            assert (5.seconds + 1012.milliseconds).toString() == "6.012 seconds"
        }
    }

    @Test
    void testDurationArithmetic() {
        use(TimeCategory) {
            def date = new Date(0)
            def cal = Calendar.getInstance()
            cal.timeInMillis = 0

            // add two durations
            def twoMonths = 1.month + 1.month
            cal.add MONTH, 2

            assertEquals cal.timeInMillis, (date + twoMonths).time,
                    "Two months absolute duration"

            // add two durations
            def monthAndWeek = 1.month + 1.week
            cal.timeInMillis = 0
            cal.add MONTH, 1
            cal.add DAY_OF_YEAR, 7
            assertEquals cal.timeInMillis, (date + monthAndWeek).time,
                    "A week and a month absolute duration"

            def twoAndaHalfWeeks = 3.weeks - 4.days + 12.hours
            cal.timeInMillis = 0
            cal.add DAY_OF_YEAR, 17
            cal.add HOUR, 12
            assertEquals cal.timeInMillis, (date + twoAndaHalfWeeks).time,
                    "two and a half weeks\n"

            assertEquals 2.weeks.toMilliseconds(),
                    14.days.toMilliseconds(), "two weeks"
            assertEquals 1.year.toMilliseconds(), 12.months.toMilliseconds(),
                    "One year and 365 days"
        }
    }

    @Test
    void testMinutesAgo() { // See GROOVY-3687
        use(TimeCategory) {
            def now = Calendar.getInstance()
            def before = 10.minutes.ago
            now.add(Calendar.MINUTE, -11)
            assertTrue now.timeInMillis < before.time,
                    "10.minutes.ago should not zero out the date"

            now = Calendar.getInstance()
            now.add(Calendar.MINUTE, -10)
            assertTrue now.timeInMillis >= before.time,
                    "10.minutes.ago should be older than 'now - 10 minutes'"
        }
    }

    @Test
    void testFromNow() {
        use(TimeCategory) {
            def now = Calendar.getInstance()
            now.add(MINUTE, 10)
            def later = 10.minutes.from.now
            assertTrue now.timeInMillis <= later.time,
                    "10.minutes.from.now should be later!"

            now = Calendar.getInstance()
            now.add(MINUTE, 11)
            assertTrue now.timeInMillis > later.time,
                    "10.minutes.from.now should be less calendar + 11 minutes"

            now = Calendar.getInstance()
            now.add(WEEK_OF_YEAR, 3)
            now.set(HOUR_OF_DAY, 0)
            now.set(MINUTE, 0)
            now.set(SECOND, 0)
            now.set(MILLISECOND, 0)
            later = 3.weeks.from.now
            assertEquals now.timeInMillis, later.time, "weeks from now!"
        }
    }

    @Test
    void testDatumDependantArithmetic() {
        use(TimeCategory) {
            def start = new Date(961552080000)
            def then = (start + 1.month) + 1.week
            def week = then - (start + 1.month)
            assert week.toMilliseconds() == (7 * 24 * 60 * 60 * 1000):  \
                 "Expected ${7 * 24 * 60 * 60 * 1000} but was ${week.toMilliseconds()}"

            start = Calendar.getInstance() // our reference
            def date = new Date(start.time.time) // our test date

            date += 2.months
            start.add MONTH, 2
            assertEquals start.time, date, "after adding two months"

            date += 5.weeks
            start.add WEEK_OF_YEAR, 5
            assertEquals start.time, date, "after adding 5 weeks"

            date -= (52.days + 123.minutes)
            start.add DAY_OF_YEAR, -52
            start.add MINUTE, -123
            assertEquals start.time, date, "after subtracting 52 days and 123 minutes"

            date -= 12345678.seconds
            start.add SECOND, -12345678
            assertEquals start.time, date, "after subtracting 12345678 seconds"
        }
    }

    @Test
    void testDurationComparison() {
        use (TimeCategory) {
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
        assertEquals(1, duration.getDays())
        assertEquals(2, duration.getHours())
        assertEquals(3, duration.getMinutes())
        assertEquals(4, duration.getSeconds())
        assertEquals(5, duration.getMillis())
    }

    @Test
    void testToMilliseconds() {
        def duration = new Duration(1, 0, 0, 0, 0)
        assertEquals(24 * 60 * 60 * 1000L, duration.toMilliseconds())
    }

    @Test
    void testToMillisecondsComplex() {
        def duration = new Duration(1, 2, 3, 4, 5)
        def expected = ((((1L * 24 + 2) * 60 + 3) * 60 + 4) * 1000) + 5
        assertEquals(expected, duration.toMilliseconds())
    }

    @Test
    void testPlusDuration() {
        def d1 = new Duration(1, 2, 3, 4, 5)
        def d2 = new Duration(1, 1, 1, 1, 1)
        def result = d1.plus(d2)
        assertEquals(2, result.getDays())
        assertEquals(3, result.getHours())
        assertEquals(4, result.getMinutes())
        assertEquals(5, result.getSeconds())
        assertEquals(6, result.getMillis())
    }

    @Test
    void testPlusTimeDuration() {
        def d = new Duration(1, 0, 0, 0, 0)
        def td = new TimeDuration(0, 2, 30, 0, 0)
        def result = d.plus(td)
        assertNotNull(result)
    }

    @Test
    void testPlusDatumDependentDuration() {
        def d = new Duration(1, 0, 0, 0, 0)
        def ddd = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def result = d.plus(ddd)
        assertNotNull(result)
        assertEquals(1, result.getYears())
        assertEquals(2, result.getMonths())
    }

    @Test
    void testMinusDuration() {
        def d1 = new Duration(5, 10, 30, 45, 500)
        def d2 = new Duration(2, 5, 15, 20, 200)
        def result = d1.minus(d2)
        assertEquals(3, result.getDays())
        assertEquals(5, result.getHours())
        assertEquals(15, result.getMinutes())
        assertEquals(25, result.getSeconds())
        assertEquals(300, result.getMillis())
    }

    @Test
    void testMinusTimeDuration() {
        def d = new Duration(2, 5, 30, 0, 0)
        def td = new TimeDuration(1, 2, 15, 0, 0)
        def result = d.minus(td)
        assertEquals(1, result.getDays())
        assertEquals(3, result.getHours())
        assertEquals(15, result.getMinutes())
    }

    @Test
    void testMinusDatumDependentDuration() {
        def d = new Duration(10, 5, 30, 20, 100)
        def ddd = new DatumDependentDuration(1, 2, 3, 1, 10, 5, 50)
        def result = d.minus(ddd)
        assertEquals(-1, result.getYears())
        assertEquals(-2, result.getMonths())
        assertEquals(7, result.getDays())
    }

    @Test
    void testMinusTimeDatumDependentDuration() {
        def d = new Duration(10, 5, 30, 20, 100)
        def tddd = new TimeDatumDependentDuration(1, 2, 3, 1, 10, 5, 50)
        def result = d.minus(tddd)
        assertEquals(-1, result.getYears())
        assertEquals(-2, result.getMonths())
    }

    @Test
    void testGetAgo() {
        def duration = new Duration(1, 0, 0, 0, 0)
        def ago = duration.getAgo()
        assertNotNull(ago)

        def expected = Calendar.getInstance()
        expected.add(Calendar.DAY_OF_YEAR, -1)
        expected.set(Calendar.HOUR_OF_DAY, 0)
        expected.set(Calendar.MINUTE, 0)
        expected.set(Calendar.SECOND, 0)
        expected.set(Calendar.MILLISECOND, 0)

        assertEquals(expected.getTimeInMillis(), ago.getTime())
    }

    @Test
    void testGetFrom() {
        def duration = new Duration(3, 0, 0, 0, 0)
        def from = duration.getFrom()
        assertNotNull(from)
        def now = from.getNow()
        assertNotNull(now)

        def expected = Calendar.getInstance()
        expected.add(Calendar.DAY_OF_YEAR, 3)
        expected.set(Calendar.HOUR_OF_DAY, 0)
        expected.set(Calendar.MINUTE, 0)
        expected.set(Calendar.SECOND, 0)
        expected.set(Calendar.MILLISECOND, 0)

        assertEquals(expected.getTimeInMillis(), now.getTime())
    }

    @Test
    void testZeroDuration() {
        def zero = new Duration(0, 0, 0, 0, 0)
        assertEquals(0, zero.toMilliseconds())
    }

    @Test
    void testNegativeDuration() {
        def negative = new Duration(-1, -2, -3, -4, -5)
        assertTrue(negative.toMilliseconds() < 0)
    }

    @Test
    void testMillisecondsOnly() {
        def millis = new Duration(0, 0, 0, 0, 500)
        assertEquals(500, millis.toMilliseconds())
    }

    @Test
    void testSecondsOnly() {
        def seconds = new Duration(0, 0, 0, 45, 0)
        assertEquals(45 * 1000L, seconds.toMilliseconds())
    }

    @Test
    void testMinutesOnly() {
        def minutes = new Duration(0, 0, 30, 0, 0)
        assertEquals(30 * 60 * 1000L, minutes.toMilliseconds())
    }

    @Test
    void testHoursOnly() {
        def hours = new Duration(0, 12, 0, 0, 0)
        assertEquals(12 * 60 * 60 * 1000L, hours.toMilliseconds())
    }

    @Test
    void testDaysOnly() {
        def days = new Duration(7, 0, 0, 0, 0)
        assertEquals(7 * 24 * 60 * 60 * 1000L, days.toMilliseconds())
    }

    @Test
    void testPlusWithZero() {
        def d = new Duration(1, 2, 3, 4, 5)
        def zero = new Duration(0, 0, 0, 0, 0)
        def result = d.plus(zero)
        assertEquals(1, result.getDays())
        assertEquals(2, result.getHours())
        assertEquals(3, result.getMinutes())
        assertEquals(4, result.getSeconds())
        assertEquals(5, result.getMillis())
    }

    @Test
    void testMinusWithZero() {
        def d = new Duration(1, 2, 3, 4, 5)
        def zero = new Duration(0, 0, 0, 0, 0)
        def result = d.minus(zero)
        assertEquals(1, result.getDays())
        assertEquals(2, result.getHours())
        assertEquals(3, result.getMinutes())
        assertEquals(4, result.getSeconds())
        assertEquals(5, result.getMillis())
    }

    @Test
    void testLargeDuration() {
        def large = new Duration(365, 23, 59, 59, 999)
        def expected = ((((365L * 24 + 23) * 60 + 59) * 60 + 59) * 1000) + 999
        assertEquals(expected, large.toMilliseconds())
    }
}
