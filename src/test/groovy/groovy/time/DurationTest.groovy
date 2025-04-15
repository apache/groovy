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

import groovy.test.GroovyTestCase

import static java.util.Calendar.*

class DurationTest extends GroovyTestCase {
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

    void testDurationArithmetic() {
        use(TimeCategory) {
            def date = new Date(0)
            def cal = Calendar.getInstance()
            cal.timeInMillis = 0

            // add two durations
            def twoMonths = 1.month + 1.month
            cal.add MONTH, 2

            assertEquals "Two months absolute duration",
                    cal.timeInMillis, (date + twoMonths).time

            // add two durations
            def monthAndWeek = 1.month + 1.week
            cal.timeInMillis = 0
            cal.add MONTH, 1
            cal.add DAY_OF_YEAR, 7
            assertEquals "A week and a month absolute duration",
                    cal.timeInMillis, (date + monthAndWeek).time

            def twoAndaHalfWeeks = 3.weeks - 4.days + 12.hours
            cal.timeInMillis = 0
            cal.add DAY_OF_YEAR, 17
            cal.add HOUR, 12
            assertEquals "two and a half weeks\n",
                    cal.timeInMillis, (date + twoAndaHalfWeeks).time

            assertEquals "two weeks", 2.weeks.toMilliseconds(),
                    14.days.toMilliseconds()
            assertEquals "One year and 365 days",
                    1.year.toMilliseconds(), 12.months.toMilliseconds()
        }
    }

    void testMinutesAgo() { // See GROOVY-3687
        use(TimeCategory) {
            def now = Calendar.getInstance()
            def before = 10.minutes.ago
            now.add(Calendar.MINUTE, -11)
            assertTrue "10.minutes.ago should not zero out the date",
                    now.timeInMillis < before.time

            now = Calendar.getInstance()
            now.add(Calendar.MINUTE, -10)
            assertTrue "10.minutes.ago should be older than 'now - 10 minutes'",
                    now.timeInMillis >= before.time
        }
    }

    void testFromNow() {
        use(TimeCategory) {
            def now = Calendar.getInstance()
            now.add(MINUTE, 10)
            def later = 10.minutes.from.now
            assertTrue "10.minutes.from.now should be later!",
                    now.timeInMillis <= later.time

            now = Calendar.getInstance()
            now.add(MINUTE, 11)
            assertTrue "10.minutes.from.now should be less calendar + 11 minutes",
                    now.timeInMillis > later.time

            now = Calendar.getInstance()
            now.add(WEEK_OF_YEAR, 3)
            now.set(HOUR_OF_DAY, 0)
            now.set(MINUTE, 0)
            now.set(SECOND, 0)
            now.set(MILLISECOND, 0)
            later = 3.weeks.from.now
            assertEquals "weeks from now!", now.timeInMillis, later.time
        }
    }

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
            assertEquals "after adding two months", start.time, date

            date += 5.weeks
            start.add WEEK_OF_YEAR, 5
            assertEquals "after adding 5 weeks", start.time, date

            date -= (52.days + 123.minutes)
            start.add DAY_OF_YEAR, -52
            start.add MINUTE, -123
            assertEquals "after subtracting 52 days and 123 minutes", start.time, date

            date -= 12345678.seconds
            start.add SECOND, -12345678
            assertEquals "after subtracting 12345678 seconds", start.time, date
        }
    }

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
}