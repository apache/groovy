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

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Base class for date and time durations (dequirked, {@code java.util.Date}-flavored).
 * <p>
 * This is the B1 prototype: the same value-class model as the legacy
 * {@code groovy.time.BaseDuration}, but with the historical quirks removed:
 * <ul>
 *   <li><b>ago / from.now no longer zero the time-of-day</b> for date-based durations —
 *       the behavior is now uniform across all durations (time is preserved), so a single
 *       implementation lives here rather than divergent overrides per subclass.</li>
 *   <li><b>{@link #toMilliseconds()} is deterministic</b> — datum-dependent amounts (years,
 *       months) use {@link ChronoUnit}'s estimated durations rather than resolving against
 *       {@code new Date()} ("now"), so the result no longer depends on when it is called.</li>
 * </ul>
 *
 * @see Duration
 */
public abstract class BaseDuration implements Comparable<BaseDuration> {
    /** Estimated milliseconds in a year (ChronoUnit.YEARS = 365.2425 days). */
    static final long MILLIS_PER_YEAR = ChronoUnit.YEARS.getDuration().toMillis();
    /** Estimated milliseconds in a month (ChronoUnit.MONTHS = 30.436875 days). */
    static final long MILLIS_PER_MONTH = ChronoUnit.MONTHS.getDuration().toMillis();

    /** Whole years stored in this duration. */
    protected final int years;
    /** Whole months stored in this duration. */
    protected final int months;
    /** Whole days stored in this duration. */
    protected final int days;
    /** Hours stored in this duration. */
    protected final int hours;
    /** Minutes stored in this duration. */
    protected final int minutes;
    /** Seconds stored in this duration. */
    protected final int seconds;
    /** Milliseconds stored in this duration. */
    protected final int millis;

    /**
     * Stores every calendar and time component of the duration.
     *
     * @param years whole years
     * @param months whole months
     * @param days whole days
     * @param hours hours
     * @param minutes minutes
     * @param seconds seconds
     * @param millis milliseconds
     */
    protected BaseDuration(final int years, final int months, final int days, final int hours, final int minutes, final int seconds, final int millis) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.millis = millis;
    }

    /**
     * Stores the day and time components. Years and months are zero.
     *
     * @param days whole days
     * @param hours hours
     * @param minutes minutes
     * @param seconds seconds
     * @param millis milliseconds
     */
    protected BaseDuration(final int days, final int hours, final int minutes, final int seconds, final int millis) {
        this(0, 0, days, hours, minutes, seconds, millis);
    }

    /**
     * Returns the stored year count.
     *
     * @return the stored year count
     */
    public int getYears()   { return this.years; }

    /**
     * Returns the stored month count.
     *
     * @return the stored month count
     */
    public int getMonths()  { return this.months; }

    /**
     * Returns the stored day count.
     *
     * @return the stored day count
     */
    public int getDays()    { return this.days; }

    /**
     * Returns the stored hour count.
     *
     * @return the stored hour count
     */
    public int getHours()   { return this.hours; }

    /**
     * Returns the stored minute count.
     *
     * @return the stored minute count
     */
    public int getMinutes() { return this.minutes; }

    /**
     * Returns the stored second count.
     *
     * @return the stored second count
     */
    public int getSeconds() { return this.seconds; }

    /**
     * Returns the stored millisecond count.
     *
     * @return the stored millisecond count
     */
    public int getMillis()  { return this.millis; }

    /**
     * Adds this duration to the supplied date.
     */
    public Date plus(final Date date) {
        final Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.YEAR, this.years);
        cal.add(Calendar.MONTH, this.months);
        cal.add(Calendar.DAY_OF_YEAR, this.days);
        cal.add(Calendar.HOUR_OF_DAY, this.hours);
        cal.add(Calendar.MINUTE, this.minutes);
        cal.add(Calendar.SECOND, this.seconds);
        cal.add(Calendar.MILLISECOND, this.millis);
        return cal.getTime();
    }

    /**
     * Converts this duration to milliseconds.
     * <p>
     * DEQUIRKED (B): deterministic. Years and months use {@link ChronoUnit} estimates
     * (a year is exactly 12 estimated months, so {@code 1.year == 12.months}); days and
     * below are exact assuming 24-hour days. The result never depends on the current date.
     */
    public long toMilliseconds() {
        return years * MILLIS_PER_YEAR
                + months * MILLIS_PER_MONTH
                + days * 86_400_000L
                + hours * 3_600_000L
                + minutes * 60_000L
                + seconds * 1_000L
                + millis;
    }

    /**
     * The date this duration ago.
     * <p>
     * DEQUIRKED (A): the time-of-day is preserved (no flooring to midnight),
     * uniformly for all duration kinds.
     */
    public Date getAgo() {
        final Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -this.years);
        cal.add(Calendar.MONTH, -this.months);
        cal.add(Calendar.DAY_OF_YEAR, -this.days);
        cal.add(Calendar.HOUR_OF_DAY, -this.hours);
        cal.add(Calendar.MINUTE, -this.minutes);
        cal.add(Calendar.SECOND, -this.seconds);
        cal.add(Calendar.MILLISECOND, -this.millis);
        return cal.getTime();
    }

    /**
     * Helper for computing dates relative to the current time.
     * DEQUIRKED (A): time-of-day preserved.
     */
    public From getFrom() {
        return new From() {
            /**
             * Adds this duration's years, months, days, and time of day to the current time.
             *
             * @return that calendar date
             */
            @Override
            public Date getNow() {
                final Calendar cal = Calendar.getInstance();
                cal.add(Calendar.YEAR, BaseDuration.this.years);
                cal.add(Calendar.MONTH, BaseDuration.this.months);
                cal.add(Calendar.DAY_OF_YEAR, BaseDuration.this.days);
                cal.add(Calendar.HOUR_OF_DAY, BaseDuration.this.hours);
                cal.add(Calendar.MINUTE, BaseDuration.this.minutes);
                cal.add(Calendar.SECOND, BaseDuration.this.seconds);
                cal.add(Calendar.MILLISECOND, BaseDuration.this.millis);
                return cal.getTime();
            }
        };
    }

    /**
     * Orders durations by {@link #toMilliseconds()}, so years and months use the
     * {@link ChronoUnit} estimates and the shorter units are exact.
     *
     * @param other the duration to compare with
     * @return a negative, zero, or positive value when this duration is shorter, equal, or longer
     */
    @Override
    public int compareTo(BaseDuration other) {
        return Long.compare(toMilliseconds(), other.toMilliseconds());
    }

    /**
     * Lists each non-zero component. Seconds and milliseconds are combined into one
     * fractional-seconds term, with the millisecond part padded to three digits.
     * A duration whose components are all zero is {@code "0"}.
     *
     * @return the component list
     */
    @Override
    public String toString() {
        List<String> buffer = new ArrayList<>();
        if (years != 0)   buffer.add(years + " years");
        if (months != 0)  buffer.add(months + " months");
        if (days != 0)    buffer.add(days + " days");
        if (hours != 0)   buffer.add(hours + " hours");
        if (minutes != 0) buffer.add(minutes + " minutes");
        if (seconds != 0 || millis != 0) {
            int normMillis = millis % 1000;
            int normSeconds = seconds + (millis - normMillis) / 1000;
            String secPart = (normSeconds == 0) ? (normMillis < 0 ? "-0" : "0") : String.valueOf(normSeconds);
            buffer.add(secPart + "." + padLeft(String.valueOf(Math.abs(normMillis))) + " seconds");
        }
        return buffer.isEmpty() ? "0" : String.join(", ", buffer);
    }

    private static String padLeft(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < 3) sb.insert(0, '0');
        return sb.toString();
    }

    /** Helper for computing dates relative to the current time. */
    public abstract static class From {
        /**
         * Returns the date produced by applying the duration to the current time.
         *
         * @return that date, with the time of day preserved
         */
        public abstract Date getNow();

        /**
         * Returns {@link #getNow()} unchanged, so the time of day is kept.
         *
         * @return the same date {@link #getNow()} produces
         */
        public Date getToday() {
            return getNow();
        }
    }
}
