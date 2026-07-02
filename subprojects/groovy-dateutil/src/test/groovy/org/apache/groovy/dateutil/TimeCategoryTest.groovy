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

import static java.util.Calendar.DAY_OF_YEAR
import static java.util.Calendar.MONTH
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests the {@link org.apache.groovy.dateutil.TimeCategory} class, the dequirked
 * {@code java.util.Date}-flavored parallel to the legacy {@code groovy.time.TimeCategory}.
 * The arithmetic assertions are ported verbatim from the legacy suite; the final section
 * exercises the two behaviors that were intentionally changed ("dequirked").
 */
class TimeCategoryTest {

    @Test
    void testDurationArithmeticOnMilliseconds() {
        use(TimeCategory) {
            def midnight = new Date(100, 0, 1, 0, 0, 0)
            def oneSecondPastMidnight = new Date(100, 0, 1, 0, 0, 1)
            def twoSecondsPastMidnight = new Date(100, 0, 1, 0, 0, 2)

            assert (midnight + 1000.millisecond) == oneSecondPastMidnight
            assert (midnight + 2000.milliseconds) == twoSecondsPastMidnight
            assert (twoSecondsPastMidnight - 1000.millisecond) == oneSecondPastMidnight
            assert (twoSecondsPastMidnight - 2000.milliseconds) == midnight
        }
    }

    @Test
    void testDurationArithmeticOnSeconds() {
        use(TimeCategory) {
            def midnight = new Date(100, 0, 1, 0, 0, 0)
            def oneSecondPastMidnight = new Date(100, 0, 1, 0, 0, 1)
            def twoSecondsPastMidnight = new Date(100, 0, 1, 0, 0, 2)

            assert (midnight + 1.second) == oneSecondPastMidnight
            assert (midnight + 2.seconds) == twoSecondsPastMidnight
            assert (twoSecondsPastMidnight - 1.second) == oneSecondPastMidnight
            assert (twoSecondsPastMidnight - 2.seconds) == midnight
        }
    }

    @Test
    void testDurationArithmeticOnMinutes() {
        use(TimeCategory) {
            def midnight = new Date(100, 0, 1, 0, 0, 0)
            def oneMinutePastMidnight = new Date(100, 0, 1, 0, 1, 0)
            def twoMinutesPastMidnight = new Date(100, 0, 1, 0, 2, 0)

            assert (midnight + 1.minute) == oneMinutePastMidnight
            assert (midnight + 2.minutes) == twoMinutesPastMidnight
            assert (twoMinutesPastMidnight - 60.seconds) == oneMinutePastMidnight
            assert (twoMinutesPastMidnight - 1.minute) == oneMinutePastMidnight
            assert (twoMinutesPastMidnight - 120.seconds) == midnight
            assert (twoMinutesPastMidnight - 2.minutes) == midnight
        }
    }

    @Test
    void testDurationArithmeticOnHours() {
        use(TimeCategory) {
            def midnight = new Date(100, 0, 1, 0, 0, 0)
            def oneAM = new Date(100, 0, 1, 1, 0, 0)
            def twoAM = new Date(100, 0, 1, 2, 0, 0)

            assert (midnight + 1.hour) == oneAM
            assert (midnight + 2.hours) == twoAM
            assert (twoAM - 3600.seconds) == oneAM
            assert (twoAM - 1.hour) == oneAM
            assert (twoAM - 7200.seconds) == midnight
            assert (twoAM - 2.hours) == midnight
        }
    }

    @Test
    void testDurationArithmeticOnDays() {
        use(TimeCategory) {
            def januaryFirst = new Date(100, 0, 1, 0, 0, 0)
            def januarySecond = new Date(100, 0, 2, 0, 0, 0)
            def januaryThird = new Date(100, 0, 3, 0, 0, 0)

            assert (januaryFirst + 1.day) == januarySecond
            assert (januaryFirst + 2.days) == januaryThird
            assert (januaryThird - 1.day) == januarySecond
            assert (januaryThird - 2.days) == januaryFirst
        }
    }

    @Test
    void testDurationArithmeticOnWeeks() {
        use(TimeCategory) {
            def firstWeek = new Date(100, 0, 1, 0, 0, 0)
            def secondWeek = new Date(100, 0, 8, 0, 0, 0)
            def thirdWeek = new Date(100, 0, 15, 0, 0, 0)

            assert (firstWeek + 1.week) == secondWeek
            assert (firstWeek + 2.weeks) == thirdWeek
            assert (thirdWeek - 1.week) == secondWeek
            assert (thirdWeek - 2.weeks) == firstWeek
        }
    }

    @Test
    void testDurationArithmeticOnMonths() {
        use(TimeCategory) {
            def january = new Date(100, 0, 1, 0, 0, 0)
            def february = new Date(100, 1, 1, 0, 0, 0)
            def march = new Date(100, 2, 1, 0, 0, 0)

            assert (january + 1.month) == february
            assert (january + 2.months) == march
            assert (march - 1.month) == february
            assert (march - 2.months) == january
        }
    }

    @Test
    void testDurationArithmeticOnYears() {
        use(TimeCategory) {
            def firstYear = new Date(100, 0, 1, 0, 0, 0)
            def secondYear = new Date(101, 0, 1, 0, 0, 0)
            def thirdYear = new Date(102, 0, 1, 0, 0, 0)

            assert (firstYear + 1.year) == secondYear
            assert (firstYear + 2.years) == thirdYear
            assert (thirdYear - 1.year) == secondYear
            assert (thirdYear - 2.years) == firstYear
        }
    }

    @Test
    void testDateSubtractionOnSeconds() {
        use(TimeCategory) {
            def current = new Date(100, 0, 1, 0, 0, 0)
            def oneSecondLater = new Date(100, 0, 1, 0, 0, 1)
            def twoSecondsLater = new Date(100, 0, 1, 0, 0, 2)

            assert (oneSecondLater - current).seconds == 1
            assert (twoSecondsLater - current).seconds == 2
        }
    }

    @Test
    void testDateSubtractionOnMinutes() {
        use(TimeCategory) {
            def current = new Date(100, 0, 1, 0, 0, 0)
            assert (new Date(100, 0, 1, 0, 1, 0) - current).minutes == 1
            assert (new Date(100, 0, 1, 0, 2, 0) - current).minutes == 2
        }
    }

    @Test
    void testDateSubtractionOnHours() {
        use(TimeCategory) {
            def current = new Date(100, 0, 1, 0, 0, 0)
            assert (new Date(100, 0, 1, 1, 0, 0) - current).hours == 1
            assert (new Date(100, 0, 1, 2, 0, 0) - current).hours == 2
        }
    }

    @Test
    void testDateSubtractionOnDays() {
        use(TimeCategory) {
            def current = new Date(100, 0, 1, 0, 0, 0)
            assert (new Date(100, 0, 2, 0, 0, 0) - current).days == 1
            assert (new Date(100, 0, 3, 0, 0, 0) - current).days == 2
        }
    }

    @Test
    void testDateSubtraction_NoYearsOrMonths() {
        use(TimeCategory) {
            def result = new Date(102, 0, 1, 0, 0, 0) - new Date(100, 0, 1, 0, 0, 0)
            // date subtraction does not populate months and years
            assert result.years == 0
            assert result.months == 0
        }
    }

    @Test
    void testToStringForNegativeValues() {
        use(TimeCategory) {
            def t1 = Calendar.instance.time
            def t2 = t1 - 4.seconds + 2.milliseconds
            def t3 = t1 + 4.seconds + 2.milliseconds
            def t4 = t1 - 4.seconds - 2.milliseconds
            def t5 = t1 + 4.seconds - 2.milliseconds
            def t6 = t1 - 2.milliseconds
            def t7 = t1 + 2.milliseconds
            assert (t1 - t2).toString() == '3.998 seconds'
            assert (t1 - t3).toString() == '-4.002 seconds'
            assert (t1 - t4).toString() == '4.002 seconds'
            assert (t1 - t5).toString() == '-3.998 seconds'
            assert (t1 - t6).toString() == '0.002 seconds'
            assert (t1 - t7).toString() == '-0.002 seconds'
        }
    }

    @Test
    void testToStringForOverflow() {
        use(TimeCategory) {
            def t = 800.milliseconds + 300.milliseconds
            assert t.toString() == '1.100 seconds'
        }
    }

    @Test
    void testZeroDurationFromNowIsNow() {
        use(TimeCategory) {
            // Replaces the legacy testDateEquality, whose reproducibility relied on from.now
            // flooring to midnight. Dequirked, a zero-length duration from now is now, with the
            // time-of-day preserved; bracket the call so it is robust under scheduling delays.
            long before = System.currentTimeMillis()
            Date result = 0.days.from.now
            long after = System.currentTimeMillis()
            assert result.time >= before && result.time <= after
        }
    }

    // ===== Dequirked behavior (would fail against the legacy groovy.time.TimeCategory) =====

    @Test
    void testAgoPreservesTimeOfDay() { // DEQUIRK A
        use(TimeCategory) {
            def now = Calendar.instance
            def before = 3.days.ago
            now.add(DAY_OF_YEAR, -3)
            // same wall-clock instant; legacy would have floored to midnight
            assertTrue Math.abs(now.timeInMillis - before.time) < 2000
        }
    }

    @Test
    void testMonthAgoPreservesTimeOfDay() { // DEQUIRK A
        use(TimeCategory) {
            def now = Calendar.instance
            def before = 1.month.ago
            now.add(MONTH, -1)
            assertTrue Math.abs(now.timeInMillis - before.time) < 2000
        }
    }

    @Test
    void testWeeksFromNowPreservesTimeOfDay() { // DEQUIRK A
        use(TimeCategory) {
            def now = Calendar.instance
            now.add(DAY_OF_YEAR, 21)
            def later = 3.weeks.from.now
            assertTrue Math.abs(now.timeInMillis - later.time) < 2000
        }
    }

    @Test
    void testToMillisecondsIsDeterministic() { // DEQUIRK B
        use(TimeCategory) {
            // no dependence on "now": years and months use fixed ChronoUnit estimates
            assert 1.year.toMilliseconds() == 12.months.toMilliseconds()
            assert 2.weeks.toMilliseconds() == 14.days.toMilliseconds()
            assert 5.months.toMilliseconds() ==
                    5 * java.time.temporal.ChronoUnit.MONTHS.duration.toMillis()
        }
    }
}
