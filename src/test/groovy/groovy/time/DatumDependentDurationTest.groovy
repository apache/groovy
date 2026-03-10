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
 * Tests for DatumDependentDuration class.
 * DatumDependentDuration represents durations whose length in milliseconds
 * cannot be determined without knowing the datum point (e.g., months, years).
 */
class DatumDependentDurationTest {

    // ===== Constructor and Getter Tests =====

    @Test
    void testConstructorAndGetters() {
        def duration = new DatumDependentDuration(2, 3, 4, 5, 6, 7, 8)

        assertEquals 2, duration.getYears()
        assertEquals 3, duration.getMonths()
        assertEquals 4, duration.getDays()
        assertEquals 5, duration.getHours()
        assertEquals 6, duration.getMinutes()
        assertEquals 7, duration.getSeconds()
        assertEquals 8, duration.getMillis()
    }

    @Test
    void testConstructorWithZeroValues() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 0, 0, 0)

        assertEquals 0, duration.getYears()
        assertEquals 0, duration.getMonths()
        assertEquals 0, duration.getDays()
        assertEquals 0, duration.getHours()
        assertEquals 0, duration.getMinutes()
        assertEquals 0, duration.getSeconds()
        assertEquals 0, duration.getMillis()
    }

    @Test
    void testConstructorWithNegativeValues() {
        def duration = new DatumDependentDuration(-1, -2, -3, -4, -5, -6, -7)

        assertEquals(-1, duration.getYears())
        assertEquals(-2, duration.getMonths())
        assertEquals(-3, duration.getDays())
        assertEquals(-4, duration.getHours())
        assertEquals(-5, duration.getMinutes())
        assertEquals(-6, duration.getSeconds())
        assertEquals(-7, duration.getMillis())
    }

    // ===== Plus DatumDependentDuration Tests =====

    @Test
    void testPlusDatumDependentDuration() {
        def duration1 = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def duration2 = new DatumDependentDuration(1, 1, 1, 1, 1, 1, 1)

        def result = duration1.plus(duration2)

        assertEquals 2, result.getYears()
        assertEquals 3, result.getMonths()
        assertEquals 4, result.getDays()
        assertEquals 5, result.getHours()
        assertEquals 6, result.getMinutes()
        assertEquals 7, result.getSeconds()
        assertEquals 8, result.getMillis()
    }

    @Test
    void testPlusDatumDependentDurationWithZero() {
        def duration1 = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def zeroDuration = new DatumDependentDuration(0, 0, 0, 0, 0, 0, 0)

        def result = duration1.plus(zeroDuration)

        assertEquals 1, result.getYears()
        assertEquals 2, result.getMonths()
        assertEquals 3, result.getDays()
        assertEquals 4, result.getHours()
        assertEquals 5, result.getMinutes()
        assertEquals 6, result.getSeconds()
        assertEquals 7, result.getMillis()
    }

    @Test
    void testPlusDatumDependentDurationWithNegative() {
        def duration1 = new DatumDependentDuration(5, 6, 7, 8, 9, 10, 11)
        def duration2 = new DatumDependentDuration(-2, -3, -4, -5, -6, -7, -8)

        def result = duration1.plus(duration2)

        assertEquals 3, result.getYears()
        assertEquals 3, result.getMonths()
        assertEquals 3, result.getDays()
        assertEquals 3, result.getHours()
        assertEquals 3, result.getMinutes()
        assertEquals 3, result.getSeconds()
        assertEquals 3, result.getMillis()
    }

    // ===== Plus TimeDatumDependentDuration Tests =====

    @Test
    void testPlusTimeDatumDependentDuration() {
        def datumDuration = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def timeDatumDuration = new TimeDatumDependentDuration(2, 3, 4, 5, 6, 7, 8)

        def result = datumDuration.plus(timeDatumDuration)

        assertEquals 3, result.getYears()
        assertEquals 5, result.getMonths()
        assertEquals 7, result.getDays()
        assertEquals 9, result.getHours()
        assertEquals 11, result.getMinutes()
        assertEquals 13, result.getSeconds()
        assertEquals 15, result.getMillis()
    }

    // ===== Plus Duration Tests =====

    @Test
    void testPlusDuration() {
        def datumDuration = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def duration = new Duration(10, 11, 12, 13, 14)

        def result = datumDuration.plus(duration)

        assertEquals 1, result.getYears()
        assertEquals 2, result.getMonths()
        assertEquals 13, result.getDays()
        assertEquals 15, result.getHours()
        assertEquals 17, result.getMinutes()
        assertEquals 19, result.getSeconds()
        assertEquals 21, result.getMillis()
    }

    @Test
    void testPlusDurationWithZeroYearsAndMonths() {
        def datumDuration = new DatumDependentDuration(0, 0, 5, 6, 7, 8, 9)
        def duration = new Duration(1, 2, 3, 4, 5)

        def result = datumDuration.plus(duration)

        assertEquals 0, result.getYears()
        assertEquals 0, result.getMonths()
        assertEquals 6, result.getDays()
        assertEquals 8, result.getHours()
        assertEquals 10, result.getMinutes()
        assertEquals 12, result.getSeconds()
        assertEquals 14, result.getMillis()
    }

    // ===== Plus TimeDuration Tests =====

    @Test
    void testPlusTimeDuration() {
        def datumDuration = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def timeDuration = new TimeDuration(10, 11, 12, 13, 14)

        def result = datumDuration.plus(timeDuration)

        // TimeDuration.plus(DatumDependentDuration) returns TimeDatumDependentDuration
        assertTrue result instanceof DatumDependentDuration
        assertEquals 1, result.getYears()
        assertEquals 2, result.getMonths()
        assertEquals 13, result.getDays()
        assertEquals 15, result.getHours()
        assertEquals 17, result.getMinutes()
        assertEquals 19, result.getSeconds()
        assertEquals 21, result.getMillis()
    }

    // ===== Minus DatumDependentDuration Tests =====

    @Test
    void testMinusDatumDependentDuration() {
        def duration1 = new DatumDependentDuration(5, 6, 7, 8, 9, 10, 11)
        def duration2 = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)

        def result = duration1.minus(duration2)

        assertEquals 4, result.getYears()
        assertEquals 4, result.getMonths()
        assertEquals 4, result.getDays()
        assertEquals 4, result.getHours()
        assertEquals 4, result.getMinutes()
        assertEquals 4, result.getSeconds()
        assertEquals 4, result.getMillis()
    }

    @Test
    void testMinusDatumDependentDurationResultingInNegative() {
        def duration1 = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def duration2 = new DatumDependentDuration(5, 6, 7, 8, 9, 10, 11)

        def result = duration1.minus(duration2)

        assertEquals(-4, result.getYears())
        assertEquals(-4, result.getMonths())
        assertEquals(-4, result.getDays())
        assertEquals(-4, result.getHours())
        assertEquals(-4, result.getMinutes())
        assertEquals(-4, result.getSeconds())
        assertEquals(-4, result.getMillis())
    }

    @Test
    void testMinusDatumDependentDurationSameValues() {
        def duration1 = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def duration2 = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)

        def result = duration1.minus(duration2)

        assertEquals 0, result.getYears()
        assertEquals 0, result.getMonths()
        assertEquals 0, result.getDays()
        assertEquals 0, result.getHours()
        assertEquals 0, result.getMinutes()
        assertEquals 0, result.getSeconds()
        assertEquals 0, result.getMillis()
    }

    // ===== Minus Duration Tests =====

    @Test
    void testMinusDuration() {
        def datumDuration = new DatumDependentDuration(1, 2, 10, 11, 12, 13, 14)
        def duration = new Duration(3, 4, 5, 6, 7)

        def result = datumDuration.minus(duration)

        assertEquals 1, result.getYears()
        assertEquals 2, result.getMonths()
        assertEquals 7, result.getDays()
        assertEquals 7, result.getHours()
        assertEquals 7, result.getMinutes()
        assertEquals 7, result.getSeconds()
        assertEquals 7, result.getMillis()
    }

    @Test
    void testMinusDurationPreservesYearsAndMonths() {
        def datumDuration = new DatumDependentDuration(5, 6, 10, 10, 10, 10, 10)
        def duration = new Duration(5, 5, 5, 5, 5)

        def result = datumDuration.minus(duration)

        assertEquals 5, result.getYears()
        assertEquals 6, result.getMonths()
        assertEquals 5, result.getDays()
        assertEquals 5, result.getHours()
        assertEquals 5, result.getMinutes()
        assertEquals 5, result.getSeconds()
        assertEquals 5, result.getMillis()
    }

    // ===== toMilliseconds Tests =====

    @Test
    void testToMillisecondsWithOnlyDays() {
        def duration = new DatumDependentDuration(0, 0, 1, 0, 0, 0, 0)
        def expectedMillis = 24L * 60 * 60 * 1000

        assertEquals expectedMillis, duration.toMilliseconds()
    }

    @Test
    void testToMillisecondsWithOnlyHours() {
        def duration = new DatumDependentDuration(0, 0, 0, 1, 0, 0, 0)
        def expectedMillis = 60L * 60 * 1000

        assertEquals expectedMillis, duration.toMilliseconds()
    }

    @Test
    void testToMillisecondsWithOnlyMinutes() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 1, 0, 0)
        def expectedMillis = 60L * 1000

        assertEquals expectedMillis, duration.toMilliseconds()
    }

    @Test
    void testToMillisecondsWithOnlySeconds() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 0, 1, 0)
        def expectedMillis = 1000L

        assertEquals expectedMillis, duration.toMilliseconds()
    }

    @Test
    void testToMillisecondsWithOnlyMillis() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 0, 0, 500)

        assertEquals 500L, duration.toMilliseconds()
    }

    @Test
    void testToMillisecondsZero() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 0, 0, 0)

        assertEquals 0L, duration.toMilliseconds()
    }

    // ===== getAgo Tests =====

    @Test
    void testGetAgo() {
        def duration = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def before = duration.getAgo()

        def expected = Calendar.getInstance()
        expected.add(Calendar.YEAR, -1)
        expected.add(Calendar.MONTH, -2)
        expected.add(Calendar.DAY_OF_YEAR, -3)
        expected.add(Calendar.HOUR_OF_DAY, -4)
        expected.add(Calendar.MINUTE, -5)
        expected.add(Calendar.SECOND, -6)
        expected.add(Calendar.MILLISECOND, -7)
        expected.set(Calendar.HOUR_OF_DAY, 0)
        expected.set(Calendar.MINUTE, 0)
        expected.set(Calendar.SECOND, 0)
        expected.set(Calendar.MILLISECOND, 0)

        assertEquals expected.getTimeInMillis(), before.getTime()
    }

    @Test
    void testGetAgoWithZeroDuration() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 0, 0, 0)
        def before = duration.getAgo()

        def expected = Calendar.getInstance()
        expected.set(Calendar.HOUR_OF_DAY, 0)
        expected.set(Calendar.MINUTE, 0)
        expected.set(Calendar.SECOND, 0)
        expected.set(Calendar.MILLISECOND, 0)

        assertEquals expected.getTimeInMillis(), before.getTime()
    }

    @Test
    void testGetAgoZerosOutTime() {
        def duration = new DatumDependentDuration(0, 0, 1, 0, 0, 0, 0)
        def before = duration.getAgo()

        def cal = Calendar.getInstance()
        cal.setTime(before)

        assertEquals 0, cal.get(Calendar.HOUR_OF_DAY)
        assertEquals 0, cal.get(Calendar.MINUTE)
        assertEquals 0, cal.get(Calendar.SECOND)
        assertEquals 0, cal.get(Calendar.MILLISECOND)
    }

    // ===== getFrom().getNow() Tests =====

    @Test
    void testGetFromNow() {
        def duration = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def future = duration.getFrom().getNow()

        def expected = Calendar.getInstance()
        expected.add(Calendar.YEAR, 1)
        expected.add(Calendar.MONTH, 2)
        expected.add(Calendar.DAY_OF_YEAR, 3)
        expected.add(Calendar.HOUR_OF_DAY, 4)
        expected.add(Calendar.MINUTE, 5)
        expected.add(Calendar.SECOND, 6)
        expected.add(Calendar.MILLISECOND, 7)
        expected.set(Calendar.HOUR_OF_DAY, 0)
        expected.set(Calendar.MINUTE, 0)
        expected.set(Calendar.SECOND, 0)
        expected.set(Calendar.MILLISECOND, 0)

        assertEquals expected.getTimeInMillis(), future.getTime()
    }

    @Test
    void testGetFromNowWithZeroDuration() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 0, 0, 0)
        def future = duration.getFrom().getNow()

        def expected = Calendar.getInstance()
        expected.set(Calendar.HOUR_OF_DAY, 0)
        expected.set(Calendar.MINUTE, 0)
        expected.set(Calendar.SECOND, 0)
        expected.set(Calendar.MILLISECOND, 0)

        assertEquals expected.getTimeInMillis(), future.getTime()
    }

    @Test
    void testGetFromNowZerosOutTime() {
        def duration = new DatumDependentDuration(0, 0, 1, 0, 0, 0, 0)
        def future = duration.getFrom().getNow()

        def cal = Calendar.getInstance()
        cal.setTime(future)

        assertEquals 0, cal.get(Calendar.HOUR_OF_DAY)
        assertEquals 0, cal.get(Calendar.MINUTE)
        assertEquals 0, cal.get(Calendar.SECOND)
        assertEquals 0, cal.get(Calendar.MILLISECOND)
    }

    @Test
    void testGetFromToday() {
        def duration = new DatumDependentDuration(0, 1, 0, 0, 0, 0, 0)
        def fromObj = duration.getFrom()

        // getToday() should return the same as getNow()
        assertEquals fromObj.getNow(), fromObj.getToday()
    }

    // ===== TimeCategory Integration Tests =====

    @Test
    void testTimeCategoryYearsCreation() {
        use(TimeCategory) {
            def oneYear = 1.year
            assertTrue oneYear instanceof DatumDependentDuration
            assertEquals 1, oneYear.getYears()
            assertEquals 0, oneYear.getMonths()

            def twoYears = 2.years
            assertEquals 2, twoYears.getYears()
        }
    }

    @Test
    void testTimeCategoryMonthsCreation() {
        use(TimeCategory) {
            def oneMonth = 1.month
            assertTrue oneMonth instanceof DatumDependentDuration
            assertEquals 0, oneMonth.getYears()
            assertEquals 1, oneMonth.getMonths()

            def threeMonths = 3.months
            assertEquals 3, threeMonths.getMonths()
        }
    }

    @Test
    void testTimeCategoryMixedDuration() {
        use(TimeCategory) {
            def mixed = 1.year + 2.months + 3.days
            assertTrue mixed instanceof DatumDependentDuration
            assertEquals 1, mixed.getYears()
            assertEquals 2, mixed.getMonths()
            assertEquals 3, mixed.getDays()
        }
    }

    @Test
    void testTimeCategoryAgo() {
        use(TimeCategory) {
            def now = Calendar.getInstance()
            def oneMonthAgo = 1.month.ago

            now.add(Calendar.MONTH, -1)
            now.set(Calendar.HOUR_OF_DAY, 0)
            now.set(Calendar.MINUTE, 0)
            now.set(Calendar.SECOND, 0)
            now.set(Calendar.MILLISECOND, 0)

            assertEquals now.getTimeInMillis(), oneMonthAgo.getTime()
        }
    }

    @Test
    void testTimeCategoryFromNow() {
        use(TimeCategory) {
            def expected = Calendar.getInstance()
            expected.add(Calendar.YEAR, 1)
            expected.set(Calendar.HOUR_OF_DAY, 0)
            expected.set(Calendar.MINUTE, 0)
            expected.set(Calendar.SECOND, 0)
            expected.set(Calendar.MILLISECOND, 0)

            def oneYearFromNow = 1.year.from.now

            assertEquals expected.getTimeInMillis(), oneYearFromNow.getTime()
        }
    }

    // ===== Date Arithmetic Tests =====

    @Test
    void testPlusDate() {
        def duration = new DatumDependentDuration(1, 0, 0, 0, 0, 0, 0)
        def date = new Date(100, 0, 1, 0, 0, 0) // Jan 1, 2000

        def result = duration.plus(date)

        def expected = Calendar.getInstance()
        expected.setTime(date)
        expected.add(Calendar.YEAR, 1)

        assertEquals expected.getTime(), result
    }

    @Test
    void testPlusDateWithMonths() {
        def duration = new DatumDependentDuration(0, 1, 0, 0, 0, 0, 0)
        def date = new Date(100, 0, 1, 0, 0, 0) // Jan 1, 2000

        def result = duration.plus(date)

        def expected = new Date(100, 1, 1, 0, 0, 0) // Feb 1, 2000

        assertEquals expected, result
    }

    @Test
    void testPlusDateWithAllFields() {
        def duration = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def date = new Date(100, 0, 1, 0, 0, 0)

        def result = duration.plus(date)

        def expected = Calendar.getInstance()
        expected.setTime(date)
        expected.add(Calendar.YEAR, 1)
        expected.add(Calendar.MONTH, 2)
        expected.add(Calendar.DAY_OF_YEAR, 3)
        expected.add(Calendar.HOUR_OF_DAY, 4)
        expected.add(Calendar.MINUTE, 5)
        expected.add(Calendar.SECOND, 6)
        expected.add(Calendar.MILLISECOND, 7)

        assertEquals expected.getTime(), result
    }

    // ===== toString Tests =====

    @Test
    void testToStringWithAllFields() {
        def duration = new DatumDependentDuration(1, 2, 3, 4, 5, 6, 7)
        def result = duration.toString()

        assertTrue result.contains("1 years")
        assertTrue result.contains("2 months")
        assertTrue result.contains("3 days")
        assertTrue result.contains("4 hours")
        assertTrue result.contains("5 minutes")
        assertTrue result.contains("seconds")
    }

    @Test
    void testToStringWithZeroDuration() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 0, 0, 0)

        assertEquals "0", duration.toString()
    }

    @Test
    void testToStringWithOnlyYears() {
        def duration = new DatumDependentDuration(2, 0, 0, 0, 0, 0, 0)

        assertEquals "2 years", duration.toString()
    }

    @Test
    void testToStringWithOnlyMonths() {
        def duration = new DatumDependentDuration(0, 3, 0, 0, 0, 0, 0)

        assertEquals "3 months", duration.toString()
    }

    @Test
    void testToStringWithSecondsAndMillis() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 0, 5, 12)

        assertEquals "5.012 seconds", duration.toString()
    }

    @Test
    void testToStringWithOnlyMillis() {
        def duration = new DatumDependentDuration(0, 0, 0, 0, 0, 0, 500)

        assertEquals "0.500 seconds", duration.toString()
    }

    // ===== Comparison Tests =====

    @Test
    void testCompareTo() {
        use(TimeCategory) {
            def oneMonth = 1.month
            def twoMonths = 2.months

            assertTrue oneMonth < twoMonths
            assertTrue twoMonths > oneMonth
        }
    }

    @Test
    void testCompareToEqual() {
        use(TimeCategory) {
            def oneYear = 1.year
            def twelveMonths = 12.months

            assertEquals 0, oneYear.compareTo(twelveMonths)
        }
    }

    @Test
    void testCompareToWithDifferentTypes() {
        use(TimeCategory) {
            def oneMonth = 1.month
            def oneDay = 1.day

            assertTrue oneMonth > oneDay
        }
    }

    // ===== Edge Cases =====

    @Test
    void testLeapYearHandling() {
        use(TimeCategory) {
            // Feb 29, 2000 (leap year)
            def leapYearDate = new Date(100, 1, 29, 0, 0, 0)
            def oneYear = 1.year

            def result = oneYear.plus(leapYearDate)

            // Calendar handles leap year to non-leap year transition
            def expected = Calendar.getInstance()
            expected.setTime(leapYearDate)
            expected.add(Calendar.YEAR, 1)

            assertEquals expected.getTime(), result
        }
    }

    @Test
    void testMonthEndHandling() {
        use(TimeCategory) {
            // Jan 31
            def janEnd = new Date(100, 0, 31, 0, 0, 0)
            def oneMonth = 1.month

            def result = oneMonth.plus(janEnd)

            // Calendar handles month-end transitions
            def expected = Calendar.getInstance()
            expected.setTime(janEnd)
            expected.add(Calendar.MONTH, 1)

            assertEquals expected.getTime(), result
        }
    }

    @Test
    void testLargeDurationValues() {
        def duration = new DatumDependentDuration(100, 120, 365, 24, 60, 60, 1000)

        assertEquals 100, duration.getYears()
        assertEquals 120, duration.getMonths()
        assertEquals 365, duration.getDays()
        assertEquals 24, duration.getHours()
        assertEquals 60, duration.getMinutes()
        assertEquals 60, duration.getSeconds()
        assertEquals 1000, duration.getMillis()
    }

    @Test
    void testChainedOperations() {
        use(TimeCategory) {
            def duration = 1.year + 2.months - 1.month + 3.days - 1.day

            assertEquals 1, duration.getYears()
            assertEquals 1, duration.getMonths()
            assertEquals 2, duration.getDays()
        }
    }

    @Test
    void testDurationWithTimeCategoryDateArithmetic() {
        use(TimeCategory) {
            def start = new Date(100, 0, 1, 0, 0, 0)
            def future = start + 1.year + 6.months

            def expected = Calendar.getInstance()
            expected.setTime(start)
            expected.add(Calendar.YEAR, 1)
            expected.add(Calendar.MONTH, 6)

            assertEquals expected.getTime(), future
        }
    }

    @Test
    void testNegativeDurationAgo() {
        def duration = new DatumDependentDuration(-1, 0, 0, 0, 0, 0, 0)
        def result = duration.getAgo()

        def expected = Calendar.getInstance()
        expected.add(Calendar.YEAR, 1) // -(-1) = +1
        expected.set(Calendar.HOUR_OF_DAY, 0)
        expected.set(Calendar.MINUTE, 0)
        expected.set(Calendar.SECOND, 0)
        expected.set(Calendar.MILLISECOND, 0)

        assertEquals expected.getTimeInMillis(), result.getTime()
    }

    @Test
    void testNegativeDurationFromNow() {
        def duration = new DatumDependentDuration(-1, 0, 0, 0, 0, 0, 0)
        def result = duration.getFrom().getNow()

        def expected = Calendar.getInstance()
        expected.add(Calendar.YEAR, -1)
        expected.set(Calendar.HOUR_OF_DAY, 0)
        expected.set(Calendar.MINUTE, 0)
        expected.set(Calendar.SECOND, 0)
        expected.set(Calendar.MILLISECOND, 0)

        assertEquals expected.getTimeInMillis(), result.getTime()
    }
}
