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
package org.apache.groovy.datetime;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.Temporal;

/**
 * A {@code java.time}-based DSL for convenient date/time manipulation, the modern
 * counterpart to the legacy {@code groovy.time.TimeCategory}. Numbers gain properties
 * that produce {@link Duration} (time-based amounts) or {@link Period} (date-based
 * amounts), which compose with the {@code java.time} types via their native arithmetic:
 * <pre class="groovyTestCase">
 * import java.time.LocalDate
 * import java.time.LocalDateTime
 * use (org.apache.groovy.datetime.TimeCategory) {
 *     assert LocalDate.of(2000, 1, 1) + 2.months == LocalDate.of(2000, 3, 1)
 *     assert 2.days.ago instanceof LocalDate
 *     assert 1.hour.from.now instanceof LocalDateTime
 * }
 * </pre>
 * <p>
 * Time-based units ({@code nanoseconds}, {@code milliseconds}, {@code seconds},
 * {@code minutes}, {@code hours}) yield a {@link Duration}; date-based units
 * ({@code days}, {@code weeks}, {@code months}, {@code years}) yield a {@link Period}.
 * The two are not combined into a single amount (as {@code java.time} intends); instead
 * they compose by chaining, e.g. {@code someDateTime + 2.months + 3.hours}.
 * <p>
 * Unlike the legacy DSL, {@code .ago}/{@code .from.now} never floor the time-of-day:
 * a {@link Duration} resolves against {@link LocalDateTime#now()} (time preserved) and a
 * {@link Period} against {@link LocalDate#now()} (date only, so no time component exists).
 */
public class TimeCategory {

    /* ******** time-based amounts -> java.time.Duration ******** */

    /**
     * A {@link Duration} of {@code self} nanoseconds.
     *
     * @param self the number of nanoseconds
     * @return that duration
     */
    public static Duration getNanoseconds(final Integer self) {
        return Duration.ofNanos(self);
    }

    /**
     * Same nanosecond duration as {@link #getNanoseconds(Integer)}.
     *
     * @param self the number of nanoseconds
     * @return that duration
     */
    public static Duration getNanosecond(final Integer self) {
        return getNanoseconds(self);
    }

    /**
     * A {@link Duration} of {@code self} milliseconds.
     *
     * @param self the number of milliseconds
     * @return that duration
     */
    public static Duration getMilliseconds(final Integer self) {
        return Duration.ofMillis(self);
    }

    /**
     * Same millisecond duration as {@link #getMilliseconds(Integer)}.
     *
     * @param self the number of milliseconds
     * @return that duration
     */
    public static Duration getMillisecond(final Integer self) {
        return getMilliseconds(self);
    }

    /**
     * A {@link Duration} of {@code self} seconds.
     *
     * @param self the number of seconds
     * @return that duration
     */
    public static Duration getSeconds(final Integer self) {
        return Duration.ofSeconds(self);
    }

    /**
     * Same second duration as {@link #getSeconds(Integer)}.
     *
     * @param self the number of seconds
     * @return that duration
     */
    public static Duration getSecond(final Integer self) {
        return getSeconds(self);
    }

    /**
     * A {@link Duration} of {@code self} minutes.
     *
     * @param self the number of minutes
     * @return that duration
     */
    public static Duration getMinutes(final Integer self) {
        return Duration.ofMinutes(self);
    }

    /**
     * Same minute duration as {@link #getMinutes(Integer)}.
     *
     * @param self the number of minutes
     * @return that duration
     */
    public static Duration getMinute(final Integer self) {
        return getMinutes(self);
    }

    /**
     * A {@link Duration} of {@code self} hours.
     *
     * @param self the number of hours
     * @return that duration
     */
    public static Duration getHours(final Integer self) {
        return Duration.ofHours(self);
    }

    /**
     * Same hour duration as {@link #getHours(Integer)}.
     *
     * @param self the number of hours
     * @return that duration
     */
    public static Duration getHour(final Integer self) {
        return getHours(self);
    }

    /* ******** date-based amounts -> java.time.Period ******** */

    /**
     * A {@link Period} of {@code self} days.
     *
     * @param self the number of days
     * @return that period
     */
    public static Period getDays(final Integer self) {
        return Period.ofDays(self);
    }

    /**
     * Same day period as {@link #getDays(Integer)}.
     *
     * @param self the number of days
     * @return that period
     */
    public static Period getDay(final Integer self) {
        return getDays(self);
    }

    /**
     * A {@link Period} of {@code self} weeks.
     *
     * @param self the number of weeks
     * @return that period
     */
    public static Period getWeeks(final Integer self) {
        return Period.ofWeeks(self);
    }

    /**
     * Same week period as {@link #getWeeks(Integer)}.
     *
     * @param self the number of weeks
     * @return that period
     */
    public static Period getWeek(final Integer self) {
        return getWeeks(self);
    }

    /**
     * A {@link Period} of {@code self} months.
     *
     * @param self the number of months
     * @return that period
     */
    public static Period getMonths(final Integer self) {
        return Period.ofMonths(self);
    }

    /**
     * Same month period as {@link #getMonths(Integer)}.
     *
     * @param self the number of months
     * @return that period
     */
    public static Period getMonth(final Integer self) {
        return getMonths(self);
    }

    /**
     * A {@link Period} of {@code self} years.
     *
     * @param self the number of years
     * @return that period
     */
    public static Period getYears(final Integer self) {
        return Period.ofYears(self);
    }

    /**
     * Same year period as {@link #getYears(Integer)}.
     *
     * @param self the number of years
     * @return that period
     */
    public static Period getYear(final Integer self) {
        return getYears(self);
    }

    /* ******** relative-time terminals ******** */

    /**
     * The {@link LocalDateTime} this time-based duration before now (time-of-day preserved).
     */
    public static LocalDateTime getAgo(final Duration self) {
        return LocalDateTime.now().minus(self);
    }

    /**
     * The {@link LocalDate} this date-based period before today.
     */
    public static LocalDate getAgo(final Period self) {
        return LocalDate.now().minus(self);
    }

    /**
     * Helper for {@code duration.from.now}; the resulting temporal is a {@link LocalDateTime}.
     */
    public static From getFrom(final Duration self) {
        return new From(LocalDateTime.now().plus(self));
    }

    /**
     * Helper for {@code period.from.now}; the resulting temporal is a {@link LocalDate}.
     */
    public static From getFrom(final Period self) {
        return new From(LocalDate.now().plus(self));
    }

    /**
     * Helper returned by {@link #getFrom}, supporting the {@code .now} / {@code .today} suffix.
     */
    public static final class From {
        private final Temporal point;

        private From(final Temporal point) {
            this.point = point;
        }

        /**
         * Returns the temporal captured when {@code from} was evaluated.
         * A {@link Duration} was added to {@link LocalDateTime#now()}; a {@link Period}
         * was added to {@link LocalDate#now()}.
         *
         * @return that stored temporal
         */
        public Temporal getNow() {
            return point;
        }

        /**
         * Returns the same temporal as {@link #getNow()}.
         *
         * @return that stored temporal
         */
        public Temporal getToday() {
            return point;
        }
    }
}
