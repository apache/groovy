/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

import java.text.SimpleDateFormat

import static java.util.Calendar.*

class DateTest extends GroovyTestCase {
    void testCalendarNextPrevious() {
        TimeZone tz = TimeZone.getTimeZone('GMT+00')
        Calendar c = getInstance(tz)
        c[HOUR_OF_DAY] = 6
        c[YEAR] = 2002
        c[MONTH] = FEBRUARY
        c[DATE] = 2
        c.clearTime()
        def formatter = new SimpleDateFormat('dd-MMM-yyyy', Locale.US)
        formatter.calendar.timeZone = tz

        assert formatter.format(c.previous().time) == '01-Feb-2002'
        assert formatter.format(c.time) == '02-Feb-2002'
        assert formatter.format(c.next().time) == '03-Feb-2002'
        def dates = (c.previous()..c.next()).collect{ formatter.format(it.time) }
        assert dates == ['01-Feb-2002', '02-Feb-2002', '03-Feb-2002']
    }

    void testDateNextPrevious() {
        def x = new Date()
        def y = x + 2
        assert x < y
        ++x
        --y
        assert x == y
        x += 2
        assert x > y
    }

    void testDateRange() {
        def today = new Date()
        def later = today + 3
        def expected = [today, today + 1, today + 2, today + 3]
        def list = []
        for (d in today..later) {
            list << d
        }
        assert list == expected
    }

    void testCalendarIndex() {
        Calendar c = new GregorianCalendar(2002, FEBRUARY, 2)
        assert c[MONTH] == FEBRUARY
        assert c[DAY_OF_WEEK] == SATURDAY
    }

    void testDateIndex() {
        Date d = new GregorianCalendar(2002, FEBRUARY, 2).time
        assert d[MONTH] == FEBRUARY
        assert d[DAY_OF_WEEK] == SATURDAY
    }
}
