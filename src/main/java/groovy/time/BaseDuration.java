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
    protected final int years;
    protected final int months;
    protected final int days;
    protected final int hours;
    protected final int minutes;
    protected final int seconds;
    protected final int millis;

    protected BaseDuration(final int years, final int months, final int days, final int hours, final int minutes, final int seconds, final int millis) {
        this.years = years;
        this.months = months;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.millis = millis;
    }

    protected BaseDuration(final int days, final int hours, final int minutes, final int seconds, final int millis) {
        this(0, 0, days, hours, minutes, seconds, millis);
    }

    public int getYears() {
        return this.years;
    }

    public int getMonths() {
        return this.months;
    }

    public int getDays() {
        return this.days;
    }

    public int getHours() {
        return this.hours;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public int getSeconds() {
        return this.seconds;
    }

    public int getMillis() {
        return this.millis;
    }

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

    public abstract long toMilliseconds();

    public abstract Date getAgo();

    public abstract From getFrom();

    @Override
    public int compareTo(BaseDuration otherDuration) {
        return Long.signum(toMilliseconds() - otherDuration.toMilliseconds());
    }

    public abstract static class From {
        public abstract Date getNow();

        public Date getToday() {
            return getNow();
        }
    }
}
