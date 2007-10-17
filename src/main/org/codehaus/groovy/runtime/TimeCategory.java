/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.runtime;

import groovy.time.BaseDuration;
import groovy.time.DatumDependentDuration;
import groovy.time.Duration;
import groovy.time.TimeDuration;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    public static TimeZone getTimeZone(Date self) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        return calendar.getTimeZone();
    }

    public static Duration getDaylightSavingsOffset(Date self) {
        TimeZone timeZone = getTimeZone(self);
        int millis = (timeZone.useDaylightTime() && timeZone.inDaylightTime(self))
                ? timeZone.getDSTSavings() : 0;
        return new TimeDuration(0, 0, 0, millis);
    }

    public static Duration getDaylightSavingsOffset(BaseDuration self) {
        return getDaylightSavingsOffset(new Date(self.toMilliseconds() + 1));
    }

    public static Duration getRelativeDaylightSavingsOffset(Date self, Date other) {
        Duration d1 = getDaylightSavingsOffset(self);
        Duration d2 = getDaylightSavingsOffset(other);
        return new TimeDuration(0, 0, 0, (int) (d2.toMilliseconds() - d1.toMilliseconds()));
    }

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
        return new DatumDependentDuration(0, self.intValue(), 0, 0, 0, 0, 0);
    }

    public static DatumDependentDuration getMonth(final Integer self) {
        return getMonths(self);
    }

    public static DatumDependentDuration getYears(final Integer self) {
        return new DatumDependentDuration(self.intValue(), 0, 0, 0, 0, 0, 0);
    }

    public static DatumDependentDuration getYear(final Integer self) {
        return getYears(self);
    }

    /*
    * Methods on Integer to implement 1.week, 4.days etc.
    */

    public static Duration getWeeks(final Integer self) {
        return new Duration(self.intValue() * 7, 0, 0, 0, 0);
    }

    public static Duration getWeek(final Integer self) {
        return getWeeks(self);
    }

    public static Duration getDays(final Integer self) {
        return new Duration(self.intValue(), 0, 0, 0, 0);
    }

    public static Duration getDay(final Integer self) {
        return getDays(self);
    }

    public static TimeDuration getHours(final Integer self) {
        return new TimeDuration(0, self.intValue(), 0, 0, 0);
    }

    public static TimeDuration getHour(final Integer self) {
        return getHours(self);
    }

    public static TimeDuration getMinutes(final Integer self) {
        return new TimeDuration(0, 0, self.intValue(), 0, 0);
    }

    public static TimeDuration getMinute(final Integer self) {
        return getMinutes(self);
    }

    public static TimeDuration getSeconds(final Integer self) {
        return new TimeDuration(0, 0, 0, self.intValue(), 0);
    }

    public static TimeDuration getSecond(final Integer self) {
        return getSeconds(self);
    }

    public static TimeDuration getMilliseconds(final Integer self) {
        return new TimeDuration(0, 0, 0, 0, self.intValue());
    }

    public static TimeDuration getMillisecond(final Integer self) {
        return getMilliseconds(self);
    }
}
