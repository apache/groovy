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

/**
 * Tests the groovy.time.TimeCategory class. 
 * Most of these tests use January 1 as a start time to avoid 
 * leap years and daylight savings time issues. 
 */
class TimeCategoryTest extends GroovyTestCase {

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


    void testDateSubtractionOnSeconds() {
        use(TimeCategory) {
            def current = new Date(100, 0, 1, 0, 0, 0)
            def oneSecondLater = new Date(100, 0, 1, 0, 0, 1)
            def twoSecondsLater = new Date(100, 0, 1, 0, 0, 2)

            def result = oneSecondLater - current
            assert result.seconds == 1
            result = twoSecondsLater - current
            assert result.seconds == 2
        }
    }

    void testDateSubtractionOnMinutes() {
        use(TimeCategory) {
            def current = new Date(100, 0, 1, 0, 0, 0)
            def oneMinuteLater = new Date(100, 0, 1, 0, 1, 0)
            def twoMinutesLater = new Date(100, 0, 1, 0, 2, 0)

            def result = oneMinuteLater - current
            assert result.minutes == 1
            result = twoMinutesLater - current
            assert result.minutes == 2
        }
    }

    void testDateSubtractionOnHours() {
        use(TimeCategory) {
            def current = new Date(100, 0, 1, 0, 0, 0)
            def oneHourLater = new Date(100, 0, 1, 1, 0, 0)
            def twoHoursLater = new Date(100, 0, 1, 2, 0, 0)

            def result = oneHourLater - current
            assert result.hours == 1
            result = twoHoursLater - current
            assert result.hours == 2
        }
    }

    void testDateSubtractionOnDays() {
        use(TimeCategory) {
            def current = new Date(100, 0, 1, 0, 0, 0)
            def oneDayLater = new Date(100, 0, 2, 0, 0, 0)
            def twoDaysLater = new Date(100, 0, 3, 0, 0, 0)

            def result = oneDayLater - current
            assert result.days == 1
            result = twoDaysLater - current
            assert result.days == 2
        }
    }

    void testDateSubtraction_NoYearsOrMonths() {
        use(TimeCategory) {
            def yearOne = new Date(100, 0, 1, 0, 0, 0)
            def yearThree = new Date(102, 0, 1, 0, 0, 0)

            def result = yearThree - yearOne

            //do NOT expect months and years to be
            //set on the result of date subtraction
            assert result.years == 0
            assert result.months == 0

        }
    }

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

    void testToStringForOverflow() {
        use(TimeCategory) {
            def t = 800.milliseconds + 300.milliseconds
            assert t.toString() == '1.100 seconds'
        }
    }

    void testDateEquality() {
        use(TimeCategory) {
            Date dt1 = 0.days.from.now
            Date dt2 = new Date(0.days.from.now.time)

            assert dt1 == dt2
            assert dt1.toString() == dt2.toString()
        }
    }
}