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
package org.apache.groovy.datetime

import org.junit.jupiter.api.Test

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

/**
 * Tests the {@code java.time}-based {@link org.apache.groovy.datetime.TimeCategory} DSL.
 */
class TimeCategoryTest {

    @Test
    void testTimeBasedProducersYieldDuration() {
        use(TimeCategory) {
            assert 5.nanoseconds == Duration.ofNanos(5)
            assert 5.milliseconds == Duration.ofMillis(5)
            assert 1.second == Duration.ofSeconds(1)
            assert 2.seconds == Duration.ofSeconds(2)
            assert 3.minutes == Duration.ofMinutes(3)
            assert 4.hours == Duration.ofHours(4)
        }
    }

    @Test
    void testDateBasedProducersYieldPeriod() {
        use(TimeCategory) {
            assert 1.day == Period.ofDays(1)
            assert 2.days == Period.ofDays(2)
            assert 1.week == Period.ofWeeks(1)
            assert 3.months == Period.ofMonths(3)
            assert 2.years == Period.ofYears(2)
        }
    }

    @Test
    void testDateArithmeticViaNativePlus() {
        use(TimeCategory) {
            assert LocalDate.of(2000, 1, 1) + 2.months == LocalDate.of(2000, 3, 1)
            assert LocalDate.of(2000, 1, 1) + 1.week == LocalDate.of(2000, 1, 8)
            assert LocalDate.of(2000, 1, 1) - 1.year == LocalDate.of(1999, 1, 1)

            def base = LocalDateTime.of(2000, 1, 1, 0, 0, 0)
            assert base + 2.hours == LocalDateTime.of(2000, 1, 1, 2, 0, 0)
            // date-based and time-based amounts compose by chaining
            assert base + 1.month + 3.hours == LocalDateTime.of(2000, 2, 1, 3, 0, 0)
        }
    }

    @Test
    void testAgoReturnTypes() {
        use(TimeCategory) {
            assert 2.days.ago instanceof LocalDate
            assert 3.months.ago instanceof LocalDate
            assert 1.hour.ago instanceof LocalDateTime
            assert 30.minutes.ago instanceof LocalDateTime
        }
    }

    @Test
    void testFromNowReturnTypes() {
        use(TimeCategory) {
            assert 2.days.from.now instanceof LocalDate
            assert 1.hour.from.now instanceof LocalDateTime
            assert 1.hour.from.today instanceof LocalDateTime
        }
    }

    @Test
    void testAgoAndFromNowValues() {
        use(TimeCategory) {
            assert 1.day.ago == LocalDate.now().minusDays(1)
            assert 2.weeks.from.now == LocalDate.now().plusWeeks(2)
            // time-based resolves against LocalDateTime (time-of-day preserved, never floored)
            def approx = LocalDateTime.now().plusHours(1)
            def actual = 1.hour.from.now
            assert Duration.between(actual, approx).abs() < Duration.ofSeconds(2)
        }
    }
}
