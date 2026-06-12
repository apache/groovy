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

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Base class for date and time durations.
 *
 * @see Duration
 */
public abstract class BaseDuration implements Comparable<BaseDuration> {
    /**
     * Year component.
     */
    protected final int years;
    /**
     * Month component.
     */
    protected final int months;
    /**
     * Day component.
     */
    protected final int days;
    /**
     * Hour component.
     */
    protected final int hours;
    /**
     * Minute component.
     */
    protected final int minutes;
    /**
     * Second component.
     */
    protected final int seconds;
    /**
     * Millisecond component.
     */
    protected final int millis;

    /**
     * Creates a duration with date and time components.
     *
     * @param years the year component
     * @param months the month component
     * @param days the day component
     * @param hours the hour component
     * @param minutes the minute component
     * @param seconds the second component
     * @param millis the millisecond component
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
     * Creates a duration without year or month components.
     *
     * @param days the day component
     * @param hours the hour component
     * @param minutes the minute component
     * @param seconds the second component
     * @param millis the millisecond component
     */
    protected BaseDuration(final int days, final int hours, final int minutes, final int seconds, final int millis) {
        this(0, 0, days, hours, minutes, seconds, millis);
    }

    /**
     * Returns the year component.
     *
     * @return the year component
     */
    public int getYears() {
        return this.years;
    }

    /**
     * Returns the month component.
     *
     * @return the month component
     */
    public int getMonths() {
        return this.months;
    }

    /**
     * Returns the day component.
     *
     * @return the day component
     */
    public int getDays() {
        return this.days;
    }

    /**
     * Returns the hour component.
     *
     * @return the hour component
     */
    public int getHours() {
        return this.hours;
    }

    /**
     * Returns the minute component.
     *
     * @return the minute component
     */
    public int getMinutes() {
        return this.minutes;
    }

    /**
     * Returns the second component.
     *
     * @return the second component
     */
    public int getSeconds() {
        return this.seconds;
    }

    /**
     * Returns the millisecond component.
     *
     * @return the millisecond component
     */
    public int getMillis() {
        return this.millis;
    }

    /**
     * Adds this duration to the supplied date.
     *
     * @param date the date to adjust
     * @return the adjusted date
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
     * Returns a human-readable representation of this duration.
     *
     * @return the duration text
     */
    @Override
    public String toString() {
        List buffer = new ArrayList();

        if (this.years != 0) buffer.add(this.years + " years");
        if (this.months != 0) buffer.add(this.months + " months");
        if (this.days != 0) buffer.add(this.days + " days");
        if (this.hours != 0) buffer.add(this.hours + " hours");
        if (this.minutes != 0) buffer.add(this.minutes + " minutes");

        if (this.seconds != 0 || this.millis != 0) {
            int norm_millis = this.millis % 1000;
            int norm_seconds = this.seconds + DefaultGroovyMethods.intdiv(this.millis - norm_millis, 1000).intValue();
            CharSequence millisToPad = "" + Math.abs(norm_millis);
            buffer.add((norm_seconds == 0 ? (norm_millis < 0 ? "-0" : "0") : norm_seconds) + "." + StringGroovyMethods.padLeft(millisToPad, 3, "0") + " seconds");
        }

        if (!buffer.isEmpty()) {
            return DefaultGroovyMethods.join(buffer.iterator(), ", ");
        } else {
            return "0";
        }
    }

    /**
     * Converts this duration to milliseconds.
     *
     * @return the duration in milliseconds
     */
    public abstract long toMilliseconds();

    /**
     * Returns the date represented by this duration ago.
     *
     * @return the computed date
     */
    public abstract Date getAgo();

    /**
     * Returns a helper for computing dates relative to now.
     *
     * @return the relative-date helper
     */
    public abstract From getFrom();

    /**
     * Compares this duration with another by their millisecond values.
     *
     * @param otherDuration the duration to compare against
     * @return a negative, zero, or positive value as this duration is less than, equal to, or greater than the other
     */
    @Override
    public int compareTo(BaseDuration otherDuration) {
        return Long.signum(toMilliseconds() - otherDuration.toMilliseconds());
    }

    /**
     * Helper for computing dates relative to the current time.
     */
    public abstract static class From {
        /**
         * Returns the computed date relative to now.
         *
         * @return the computed date
         */
        public abstract Date getNow();

        /**
         * Returns the computed date relative to today.
         *
         * @return the computed date
         */
        public Date getToday() {
            return getNow();
        }
    }
}
