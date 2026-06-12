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

/**
 * Duration represents time periods which have values independent of the context.
 * So, whilst we can't say how long a month is without knowing the year and the name of the month,
 * we know how long a day is independent of the date.
 * <p>
 * This is not 100% true for days.
 * Days can actually be 23, 24 or 25 hours long (due to daylight saving adjustments.)
 * <p>
 * If you ask Duration to convert itself to milliseconds then it will work on the basis of 24 hours
 * in a day. If you add or subtract it from a date it will take daylight saving into account.
 */
public class Duration extends BaseDuration {
    /**
     * Creates a fixed duration.
     *
     * @param days the day component
     * @param hours the hour component
     * @param minutes the minute component
     * @param seconds the second component
     * @param millis the millisecond component
     */
    public Duration(final int days, final int hours, final int minutes, final int seconds, final int millis) {
        super(days, hours, minutes, seconds, millis);
    }

    /**
     * Adds another fixed duration.
     *
     * @param rhs the duration to add
     * @return the combined duration
     */
    public Duration plus(final Duration rhs) {
        return new Duration(this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                            this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                            this.getMillis() + rhs.getMillis());
    }

    /**
     * Adds a time duration.
     *
     * @param rhs the duration to add
     * @return the combined duration
     */
    public Duration plus(final TimeDuration rhs) {
        return rhs.plus(this);
    }

    /**
     * Adds a datum-dependent duration.
     *
     * @param rhs the duration to add
     * @return the combined duration
     */
    public DatumDependentDuration plus(final DatumDependentDuration rhs) {
        return rhs.plus(this);
    }

    /**
     * Subtracts another fixed duration.
     *
     * @param rhs the duration to subtract
     * @return the resulting duration
     */
    public Duration minus(final Duration rhs) {
        return new Duration(this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                            this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                            this.getMillis() - rhs.getMillis());
    }

    /**
     * Subtracts a time duration.
     *
     * @param rhs the duration to subtract
     * @return the resulting duration
     */
    public TimeDuration minus(final TimeDuration rhs) {
        return new TimeDuration(this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                this.getMillis() - rhs.getMillis());
    }

    /**
     * Subtracts a datum-dependent duration.
     *
     * @param rhs the duration to subtract
     * @return the resulting duration
     */
    public DatumDependentDuration minus(final DatumDependentDuration rhs) {
        return new DatumDependentDuration(-rhs.getYears(), -rhs.getMonths(),
                                          this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                          this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                          this.getMillis() - rhs.getMillis());
    }

    /**
     * Subtracts a mixed time and datum-dependent duration.
     *
     * @param rhs the duration to subtract
     * @return the resulting duration
     */
    public TimeDatumDependentDuration minus(final TimeDatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(-rhs.getYears(), -rhs.getMonths(),
                                              this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                              this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                              this.getMillis() - rhs.getMillis());
    }

    /**
     * Converts this duration to milliseconds assuming 24-hour days.
     *
     * @return the duration in milliseconds
     */
    @Override
    public long toMilliseconds() {
        return ((((((this.getDays() * 24L) + this.getHours()) * 60 + this.getMinutes()) * 60) + this.getSeconds()) * 1000) + this.getMillis();
    }

    /**
     * Returns the date represented by this duration ago.
     *
     * @return the computed date
     */
    @Override
    public Date getAgo() {
        final Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_YEAR, -this.getDays());
        cal.add(Calendar.HOUR_OF_DAY, -this.getHours());
        cal.add(Calendar.MINUTE, -this.getMinutes());
        cal.add(Calendar.SECOND, -this.getSeconds());
        cal.add(Calendar.MILLISECOND, -this.getMillis());

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Date(cal.getTimeInMillis());
    }

    /**
     * Returns a helper for computing dates relative to now.
     *
     * @return the relative-date helper
     */
    @Override
    public From getFrom() {
        /**
         * Relative-date helper for this duration.
         */
        return new From() {
            /**
             * Returns the date obtained by adding this duration to today.
             *
             * @return the computed date
             */
            @Override
            public Date getNow() {
            final Calendar cal = Calendar.getInstance();

                cal.add(Calendar.DAY_OF_YEAR, Duration.this.getDays());

                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                return new Date(cal.getTimeInMillis());
            }
        };
    }
}
