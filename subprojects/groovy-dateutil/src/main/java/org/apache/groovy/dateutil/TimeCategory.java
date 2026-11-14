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
package org.apache.groovy.dateutil;

import java.util.Calendar;
import java.util.Date;

/**
 * A dequirked, {@code java.util.Date}-flavored parallel to the legacy
 * {@code groovy.time.TimeCategory}. Use as:
 * <pre class="groovyTestCase">
 * use (org.apache.groovy.dateutil.TimeCategory) {
 *     println 1.minute.from.now
 *     println 10.hours.ago
 *     def someDate = new Date()
 *     println someDate - 3.months
 * }
 * </pre>
 * Differences from the legacy class: the deprecated {@code getTimeZone} and the
 * {@code getDaylightSavingsOffset} helpers are dropped, and {@code .ago}/{@code .from.now}
 * preserve the time-of-day (see {@link BaseDuration}).
 *
 * @see BaseDuration
 */
public class TimeCategory {

    /**
     * Adds {@code duration} to {@code date} by calendar field.
     *
     * @param date the starting date
     * @param duration the amount to add
     * @return the shifted date
     */
    public static Date plus(final Date date, final BaseDuration duration) {
        return duration.plus(date);
    }

    /**
     * Subtracts each component of {@code duration} from {@code date}.
     * Hours, minutes, seconds, and milliseconds move the time of day.
     *
     * @param date the starting date
     * @param duration the amount to subtract
     * @return the shifted date
     */
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
     * Returns {@code lhs - rhs} as a {@link TimeDuration}. The millisecond
     * difference is split into days, hours, minutes, seconds, and milliseconds
     * by integer division.
     *
     * @param lhs the later date when the result is positive
     * @param rhs the date subtracted from {@code lhs}
     * @return the difference
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

    /* Integer producers: 1.month, 4.years etc. */

    /**
     * A {@link DatumDependentDuration} whose month component is {@code self}.
     *
     * @param self the number of months
     * @return that duration
     */
    public static DatumDependentDuration getMonths(final Integer self) {
        return new DatumDependentDuration(0, self, 0, 0, 0, 0, 0);
    }

    /**
     * Same month duration as {@link #getMonths(Integer)}.
     *
     * @param self the number of months
     * @return that duration
     */
    public static DatumDependentDuration getMonth(final Integer self) {
        return getMonths(self);
    }

    /**
     * A {@link DatumDependentDuration} whose year component is {@code self}.
     *
     * @param self the number of years
     * @return that duration
     */
    public static DatumDependentDuration getYears(final Integer self) {
        return new DatumDependentDuration(self, 0, 0, 0, 0, 0, 0);
    }

    /**
     * Same year duration as {@link #getYears(Integer)}.
     *
     * @param self the number of years
     * @return that duration
     */
    public static DatumDependentDuration getYear(final Integer self) {
        return getYears(self);
    }

    /**
     * A {@link Duration} of {@code self * 7} days.
     *
     * @param self the number of weeks
     * @return that duration
     */
    public static Duration getWeeks(final Integer self) {
        return new Duration(self * 7, 0, 0, 0, 0);
    }

    /**
     * Same week duration as {@link #getWeeks(Integer)}.
     *
     * @param self the number of weeks
     * @return that duration
     */
    public static Duration getWeek(final Integer self) {
        return getWeeks(self);
    }

    /**
     * A {@link Duration} of {@code self} days.
     *
     * @param self the number of days
     * @return that duration
     */
    public static Duration getDays(final Integer self) {
        return new Duration(self, 0, 0, 0, 0);
    }

    /**
     * Same day duration as {@link #getDays(Integer)}.
     *
     * @param self the number of days
     * @return that duration
     */
    public static Duration getDay(final Integer self) {
        return getDays(self);
    }

    /**
     * A {@link TimeDuration} of {@code self} hours.
     *
     * @param self the number of hours
     * @return that duration
     */
    public static TimeDuration getHours(final Integer self) {
        return new TimeDuration(0, self, 0, 0, 0);
    }

    /**
     * Same hour duration as {@link #getHours(Integer)}.
     *
     * @param self the number of hours
     * @return that duration
     */
    public static TimeDuration getHour(final Integer self) {
        return getHours(self);
    }

    /**
     * A {@link TimeDuration} of {@code self} minutes.
     *
     * @param self the number of minutes
     * @return that duration
     */
    public static TimeDuration getMinutes(final Integer self) {
        return new TimeDuration(0, 0, self, 0, 0);
    }

    /**
     * Same minute duration as {@link #getMinutes(Integer)}.
     *
     * @param self the number of minutes
     * @return that duration
     */
    public static TimeDuration getMinute(final Integer self) {
        return getMinutes(self);
    }

    /**
     * A {@link TimeDuration} of {@code self} seconds.
     *
     * @param self the number of seconds
     * @return that duration
     */
    public static TimeDuration getSeconds(final Integer self) {
        return new TimeDuration(0, 0, 0, self, 0);
    }

    /**
     * Same second duration as {@link #getSeconds(Integer)}.
     *
     * @param self the number of seconds
     * @return that duration
     */
    public static TimeDuration getSecond(final Integer self) {
        return getSeconds(self);
    }

    /**
     * A {@link TimeDuration} of {@code self} milliseconds.
     *
     * @param self the number of milliseconds
     * @return that duration
     */
    public static TimeDuration getMilliseconds(final Integer self) {
        return new TimeDuration(0, 0, 0, 0, self);
    }

    /**
     * Same millisecond duration as {@link #getMilliseconds(Integer)}.
     *
     * @param self the number of milliseconds
     * @return that duration
     */
    public static TimeDuration getMillisecond(final Integer self) {
        return getMilliseconds(self);
    }
}
