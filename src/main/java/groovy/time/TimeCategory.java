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
package groovy.time;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Apply a number of methods to allow convenient Date/Time manipulation,such as:
 * <pre class="groovyTestCase">
 * use (groovy.time.TimeCategory) {
 *     // application on numbers:
 *     println 1.minute.from.now
 *     println 10.hours.ago
 *
 *     // application on dates
 *     def someDate = new Date()
 *     println someDate - 3.months
 * }
 * </pre>
 *
 * @see BaseDuration
 */
public class TimeCategory {
    /*
     * Methods to allow Date Duration arithmetic
     */

    public static Date plus(final Date date, final BaseDuration duration) {
        return duration.plus(date);
    }

    public static Date minus(final Date date, final BaseDuration duration) {
        final Calendar cal = Calendar.getInstance();

        cal.setTime(date);
        cal.add(Calendar.YEAR, -duration.getYears());
        cal.add(Calendar.MONTH, -duration.getMonths());
        cal.add(Calendar.DAY_OF_YEAR, -duration.getDays());
        cal.add(Calendar.HOUR_OF_DAY, -duration.getHours());
        cal.add(Calendar.MINUTE, -duration.getMinutes());
        cal.add(Calendar.SECOND, -duration.getSeconds());
        cal.add(Calendar.MILLISECOND, -duration.getMillis());

        return cal.getTime();
    }

    /**
     * Retrieves the default TimeZone for a date by using the default Locale
     * settings. Recommended that you use {@code TimeZone.getDefault()} instead.
     *
     * @param self a Date
     * @return the TimeZone
     */
    @Deprecated
    public static TimeZone getTimeZone(Date self) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        return calendar.getTimeZone();
    }

    /**
     * Get the DST offset (if any) for the default locale and the given date.
     *
     * @param self a Date
     * @return the DST offset as a Duration.
     */
    public static Duration getDaylightSavingsOffset(Date self) {
        TimeZone timeZone = getTimeZone(self);
        int millis = (timeZone.useDaylightTime() && timeZone.inDaylightTime(self))
                ? timeZone.getDSTSavings() : 0;
        return new TimeDuration(0, 0, 0, millis);
    }

    public static Duration getDaylightSavingsOffset(BaseDuration self) {
        return getDaylightSavingsOffset(new Date(self.toMilliseconds() + 1));
    }

    /**
     * Return a Duration representing the DST difference (if any) between two
     * dates.  i.e. if one date is before the DST changeover, and the other
     * date is after, the resulting duration will represent the DST offset.
     *
     * @param self  a Date
     * @param other another Date
     * @return a Duration
     */
    public static Duration getRelativeDaylightSavingsOffset(Date self, Date other) {
        Duration d1 = getDaylightSavingsOffset(self);
        Duration d2 = getDaylightSavingsOffset(other);
        return new TimeDuration(0, 0, 0, (int) (d2.toMilliseconds() - d1.toMilliseconds()));
    }

    /**
     * Subtract one date from the other.
     *
     * @param lhs a Date
     * @param rhs another Date
     * @return a Duration
     */
    public static TimeDuration minus(final Date lhs, final Date rhs) {
        long milliseconds = lhs.getTime() - rhs.getTime();
        long days = milliseconds / (24 * 60 * 60 * 1000);
        milliseconds -= days * 24 * 60 * 60 * 1000;
        int hours = (int) (milliseconds / (60 * 60 * 1000));
        milliseconds -= hours * 60 * 60 * 1000;
        int minutes = (int) (milliseconds / (60 * 1000));
        milliseconds -= minutes * 60 * 1000;
        int seconds = (int) (milliseconds / 1000);
        milliseconds -= seconds * 1000;

        return new TimeDuration((int) days, hours, minutes, seconds, (int) milliseconds);
    }

    /*
    * Methods on Integer to implement 1.month, 4.years etc.
    */

    public static DatumDependentDuration getMonths(final Integer self) {
        return new DatumDependentDuration(0, self, 0, 0, 0, 0, 0);
    }

    public static DatumDependentDuration getMonth(final Integer self) {
        return getMonths(self);
    }

    public static DatumDependentDuration getYears(final Integer self) {
        return new DatumDependentDuration(self, 0, 0, 0, 0, 0, 0);
    }

    public static DatumDependentDuration getYear(final Integer self) {
        return getYears(self);
    }

    /*
    * Methods on Integer to implement 1.week, 4.days etc.
    */

    public static Duration getWeeks(final Integer self) {
        return new Duration(self * 7, 0, 0, 0, 0);
    }

    public static Duration getWeek(final Integer self) {
        return getWeeks(self);
    }

    public static Duration getDays(final Integer self) {
        return new Duration(self, 0, 0, 0, 0);
    }

    public static Duration getDay(final Integer self) {
        return getDays(self);
    }

    public static TimeDuration getHours(final Integer self) {
        return new TimeDuration(0, self, 0, 0, 0);
    }

    public static TimeDuration getHour(final Integer self) {
        return getHours(self);
    }

    public static TimeDuration getMinutes(final Integer self) {
        return new TimeDuration(0, 0, self, 0, 0);
    }

    public static TimeDuration getMinute(final Integer self) {
        return getMinutes(self);
    }

    public static TimeDuration getSeconds(final Integer self) {
        return new TimeDuration(0, 0, 0, self, 0);
    }

    public static TimeDuration getSecond(final Integer self) {
        return getSeconds(self);
    }

    public static TimeDuration getMilliseconds(final Integer self) {
        return new TimeDuration(0, 0, 0, 0, self);
    }

    public static TimeDuration getMillisecond(final Integer self) {
        return getMilliseconds(self);
    }
}
