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
package gdk

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

class WorkingWithDateTimeTypesTest extends GroovyTestCase {

    void testParsing() {
        // tag::static_parsing[]
        def date = LocalDate.parse('Jun 3, 04', 'MMM d, yy')
        assert date == LocalDate.of(2004, Month.JUNE, 3)

        def time = LocalTime.parse('4:45', 'H:mm')
        assert time == LocalTime.of(4, 45, 0)

        def offsetTime = OffsetTime.parse('09:47:51-1234', 'HH:mm:ssZ')
        assert offsetTime == OffsetTime.of(9, 47, 51, 0, ZoneOffset.ofHoursMinutes(-12, -34))

        def dateTime = ZonedDateTime.parse('2017/07/11 9:47PM Pacific Standard Time', 'yyyy/MM/dd h:mma zzzz')
        assert dateTime == ZonedDateTime.of(
                LocalDate.of(2017, 7, 11),
                LocalTime.of(21, 47, 0),
                ZoneId.of('America/Los_Angeles')
        )
        // end::static_parsing[]
    }

    void testRange() {
        // tag::date_ranges[]
        def start = LocalDate.now()
        def end = start + 6 // 6 days later
        (start..end).each { date ->
            println date.dayOfWeek
        }
        // end::date_ranges[]
    }

    void testUptoDownto() {
        // tag::date_upto_date[]
        def start = LocalDate.now()
        def end = start + 6 // 6 days later
        start.upto(end) { next ->
            println next.dayOfWeek
        }
        // end::date_upto_date[]
    }

    void testUptoCustomUnit() {
        // tag::date_upto_date_by_months[]
        def start = LocalDate.of(2018, Month.MARCH, 1)
        def end = start + 1 // 1 day later

        int iterationCount = 0
        start.upto(end, ChronoUnit.MONTHS) { next ->
            println next
            ++iterationCount
        }

        assert iterationCount == 1
        // end::date_upto_date_by_months[]
    }

    void testPlusMinusWithTemporalAmounts() {
        // tag::plus_minus_period[]
        def aprilFools = LocalDate.of(2018, Month.APRIL, 1)

        def nextAprilFools = aprilFools + Period.ofDays(365) // add 365 days
        assert nextAprilFools.year == 2019

        def idesOfMarch = aprilFools - Period.ofDays(17) // subtract 17 days
        assert idesOfMarch.dayOfMonth == 15
        assert idesOfMarch.month == Month.MARCH
        // end::plus_minus_period[]
    }

    void testLocalDatePlusMinusInteger() {
        def aprilFools = LocalDate.of(2018, Month.APRIL, 1)

        // tag::localdate_plus_minus_integer[]
        def nextAprilFools = aprilFools + 365 // add 365 days
        def idesOfMarch = aprilFools - 17 // subtract 17 days
        // end::localdate_plus_minus_integer[]

        assert nextAprilFools.year == 2019
        assert idesOfMarch.dayOfMonth == 15
        assert idesOfMarch.month == Month.MARCH
    }

    void testLocalTimePlusMinusInteger() {
        // tag::localtime_plus_minus_integer[]
        def mars = LocalTime.of(12, 34, 56) // 12:34:56 pm

        def thirtySecondsToMars = mars - 30 // go back 30 seconds
        assert thirtySecondsToMars.second == 26
        // end::localtime_plus_minus_integer[]
    }

    void testNextPrevious() {
        // tag::next_previous[]
        def year = Year.of(2000)
        --year // decrement by one year
        assert year.value == 1999

        def offsetTime = OffsetTime.of(0, 0, 0, 0, ZoneOffset.UTC) // 00:00:00.000 UTC
        offsetTime++ // increment by one second
        assert offsetTime.second == 1
        // end::next_previous[]
    }

    void testMultiplyDivide() {
        // tag::multiply_divide[]
        def period = Period.ofMonths(1) * 2 // a 1-month period times 2
        assert period.months == 2

        def duration = Duration.ofSeconds(10) / 5// a 10-second duration divided by 5
        assert duration.seconds == 2
        // end::multiply_divide[]
    }

    void testNegation() {
        // tag::duration_negation[]
        def duration = Duration.ofSeconds(-15)
        def negated = -duration
        assert negated.seconds == 15
        // end::duration_negation[]
    }

    void testPropertyNotation() {
        // tag::property_notation[]
        def date = LocalDate.of(2018, Month.MARCH, 12)
        assert date[ChronoField.YEAR] == 2018
        assert date[ChronoField.MONTH_OF_YEAR] == Month.MARCH.value
        assert date[ChronoField.DAY_OF_MONTH] == 12
        assert date[ChronoField.DAY_OF_WEEK] == DayOfWeek.MONDAY.value

        def period = Period.ofYears(2).withMonths(4).withDays(6)
        assert period[ChronoUnit.YEARS] == 2
        assert period[ChronoUnit.MONTHS] == 4
        assert period[ChronoUnit.DAYS] == 6
        // end::property_notation[]
    }

    void testLeftShift() {
        // tag::leftshift_operator[]
        MonthDay monthDay = Month.JUNE << 3 // June 3rd
        LocalDate date = monthDay << Year.of(2015) // 3-Jun-2015
        LocalDateTime dateTime = date << LocalTime.NOON // 3-Jun-2015 @ 12pm
        OffsetDateTime offsetDateTime = dateTime << ZoneOffset.ofHours(-5) // 3-Jun-2015 @ 12pm UTC-5
        // end::leftshift_operator[]
        // tag::leftshift_operator_reflexive[]
        def year = Year.of(2000)
        def month = Month.DECEMBER

        YearMonth a = year << month
        YearMonth b = month << year
        assert a == b
        // end::leftshift_operator_reflexive[]
    }

    void testRightShift() {
        // tag::rightshift_operator_period[]
        def newYears = LocalDate.of(2018, Month.JANUARY, 1)
        def aprilFools = LocalDate.of(2018, Month.APRIL, 1)

        def period = newYears >> aprilFools
        assert period instanceof Period
        assert period.months == 3
        // end::rightshift_operator_period[]

        // tag::rightshift_operator_duration[]
        def duration = LocalTime.NOON >> (LocalTime.NOON + 30)
        assert duration instanceof Duration
        assert duration.seconds == 30
        // end::rightshift_operator_duration[]

        // tag::rightshift_operator_negative[]
        def decade = Year.of(2010) >> Year.of(2000)
        assert decade.years == -10
        // end::rightshift_operator_negative[]
    }

    void testToDateAndToCalendar() {
        // tag::todate_tocalendar[]
        // LocalDate to java.util.Date
        def valentines = LocalDate.of(2018, Month.FEBRUARY, 14)
        assert valentines.toDate().format('MMMM dd, yyyy') == 'February 14, 2018'

        // LocalTime to java.util.Date
        def noon = LocalTime.of(12, 0, 0)
        assert noon.toDate().format('HH:mm:ss') == '12:00:00'

        // ZoneId to java.util.TimeZone
        def newYork = ZoneId.of('America/New_York')
        assert newYork.toTimeZone() == TimeZone.getTimeZone('America/New_York')

        // ZonedDateTime to java.util.Calendar
        def valAtNoonInNY = ZonedDateTime.of(valentines, noon, newYork)
        assert valAtNoonInNY.toCalendar().getTimeZone().toZoneId() == newYork
        // end::todate_tocalendar[]
    }

    void testConvertToJSR310Types() {
        // tag::to_jsr310_types[]
        Date legacy = Date.parse('yyyy-MM-dd HH:mm:ss.SSS', '2010-04-03 10:30:58.999')

        assert legacy.toLocalDate() == LocalDate.of(2010, 4, 3)
        assert legacy.toLocalTime() == LocalTime.of(10, 30, 58, 999_000_000) // 999M ns = 999ms
        assert legacy.toOffsetTime().hour == 10
        assert legacy.toYear() == Year.of(2010)
        assert legacy.toMonth() == Month.APRIL
        assert legacy.toDayOfWeek() == DayOfWeek.SATURDAY
        assert legacy.toMonthDay() == MonthDay.of(Month.APRIL, 3)
        assert legacy.toYearMonth() == YearMonth.of(2010, Month.APRIL)
        assert legacy.toLocalDateTime().year == 2010
        assert legacy.toOffsetDateTime().dayOfMonth == 3
        assert legacy.toZonedDateTime().zone == ZoneId.systemDefault()
        // end::to_jsr310_types[]
    }

}
