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
 * DatumDependentDuration represents durations whose length in milliseconds 
 * cannot be determined without knowing the datum point.
 * <p>
 * I don't know how many days in a year unless I know if it's a leap year or not.
 * <p>
 * I don't know how many days in a month unless I know the name of the month 
 * (and if it's a leap year if the month is February)
 */
public class DatumDependentDuration extends BaseDuration {
    /**
     * Creates a datum-dependent duration.
     *
     * @param years the year component
     * @param months the month component
     * @param days the day component
     * @param hours the hour component
     * @param minutes the minute component
     * @param seconds the second component
     * @param millis the millisecond component
     */
    public DatumDependentDuration(final int years, final int months, final int days, final int hours, final int minutes, final int seconds, final int millis) {
        super(years, months, days, hours, minutes, seconds, millis);
    }

    /** {@inheritDoc} */
    @Override
    public int getMonths() {
        return this.months;
    }

    /** {@inheritDoc} */
    @Override
    public int getYears() {
        return this.years;
    }

    /**
     * Adds another datum-dependent duration.
     *
     * @param rhs the duration to add
     * @return the combined duration
     */
    public DatumDependentDuration plus(final DatumDependentDuration rhs) {
        return new DatumDependentDuration(this.getYears() + rhs.getYears(), this.getMonths() + rhs.getMonths(),
                this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                this.getMillis() + rhs.getMillis());
    }

    /**
     * Adds a mixed time and datum-dependent duration.
     *
     * @param rhs the duration to add
     * @return the combined duration
     */
    public DatumDependentDuration plus(final TimeDatumDependentDuration rhs) {
        return rhs.plus(this);
    }

    /**
     * Adds a fixed duration.
     *
     * @param rhs the duration to add
     * @return the combined duration
     */
    public DatumDependentDuration plus(final Duration rhs) {
        return new DatumDependentDuration(this.getYears(), this.getMonths(),
                this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                this.getMillis() + rhs.getMillis());

    }

    /**
     * Adds a time duration.
     *
     * @param rhs the duration to add
     * @return the combined duration
     */
    public DatumDependentDuration plus(final TimeDuration rhs) {
        return rhs.plus(this);

    }

    /**
     * Subtracts another datum-dependent duration.
     *
     * @param rhs the duration to subtract
     * @return the resulting duration
     */
    public DatumDependentDuration minus(final DatumDependentDuration rhs) {
        return new DatumDependentDuration(this.getYears() - rhs.getYears(), this.getMonths() - rhs.getMonths(),
                this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                this.getMillis() - rhs.getMillis());

    }

    /**
     * Subtracts a fixed duration.
     *
     * @param rhs the duration to subtract
     * @return the resulting duration
     */
    public DatumDependentDuration minus(final Duration rhs) {
        return new DatumDependentDuration(this.getYears(), this.getMonths(),
                this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                this.getMillis() - rhs.getMillis());

    }

    /**
     * @see groovy.time.BaseDuration#toMilliseconds()
     *
     * Change the duration into milliseconds, relative to 'now.'  Therefore
     * things like timezone and time of year will affect how this conversion 
     * occurs.
     */
    @Override
    public long toMilliseconds() {
        final Date now = new Date();
        return TimeCategory.minus(plus(now), now).toMilliseconds();
    }

    /**
     * Returns the date represented by this duration ago.
     *
     * @return the computed date
     */
    @Override
    public Date getAgo() {
        final Calendar cal = Calendar.getInstance();

        cal.add(Calendar.YEAR, -this.getYears());
        cal.add(Calendar.MONTH, -this.getMonths());
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
             * Returns the date obtained by adding this duration to now.
             *
             * @return the computed date
             */
            @Override
            public Date getNow() {
                final Calendar cal = Calendar.getInstance();

                cal.add(Calendar.YEAR, DatumDependentDuration.this.getYears());
                cal.add(Calendar.MONTH, DatumDependentDuration.this.getMonths());
                cal.add(Calendar.DAY_OF_YEAR, DatumDependentDuration.this.getDays());
                cal.add(Calendar.HOUR_OF_DAY, DatumDependentDuration.this.getHours());
                cal.add(Calendar.MINUTE, DatumDependentDuration.this.getMinutes());
                cal.add(Calendar.SECOND, DatumDependentDuration.this.getSeconds());
                cal.add(Calendar.MILLISECOND, DatumDependentDuration.this.getMillis());

                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                return new Date(cal.getTimeInMillis());
            }
        };
    }
}
