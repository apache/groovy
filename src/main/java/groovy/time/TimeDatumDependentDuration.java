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
 * TimeDatumDuration represents a time period which results from an
 * arithmetic operation between a TimeDuration object and a DatumDuration object
 */
public class TimeDatumDependentDuration extends DatumDependentDuration {
    /**
     * Creates a mixed time and datum-dependent duration.
     *
     * @param years the year component
     * @param months the month component
     * @param days the day component
     * @param hours the hour component
     * @param minutes the minute component
     * @param seconds the second component
     * @param millis the millisecond component
     */
    public TimeDatumDependentDuration(int years, int months, int days, int hours, int minutes, int seconds, int millis) {
        super(years, months, days, hours, minutes, seconds, millis);
    }

    /**
     * Adds a fixed duration.
     *
     * @param rhs the duration to add
     * @return the combined duration
     */
    @Override
    public DatumDependentDuration plus(final Duration rhs) {
        return new TimeDatumDependentDuration(this.getYears(), this.getMonths(),
                                              this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                                              this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                                              this.getMillis() + rhs.getMillis());
    }

    /**
     * Adds a datum-dependent duration.
     *
     * @param rhs the duration to add
     * @return the combined duration
     */
    @Override
    public DatumDependentDuration plus(final DatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(this.getYears() + rhs.getYears(), this.getMonths() + rhs.getMonths(),
                                              this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                                              this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                                              this.getMillis() + rhs.getMillis());
    }

    /**
     * Subtracts a fixed duration.
     *
     * @param rhs the duration to subtract
     * @return the resulting duration
     */
    @Override
    public DatumDependentDuration minus(final Duration rhs) {
        return new TimeDatumDependentDuration(this.getYears(), this.getMonths(),
                                              this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                              this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                              this.getMillis() - rhs.getMillis());
    }

    /**
     * Subtracts a datum-dependent duration.
     *
     * @param rhs the duration to subtract
     * @return the resulting duration
     */
    @Override
    public DatumDependentDuration minus(final DatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(this.getYears() - rhs.getYears(), this.getMonths() - rhs.getMonths(),
                                              this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                              this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                              this.getMillis() - rhs.getMillis());
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

                cal.add(Calendar.YEAR, TimeDatumDependentDuration.this.getYears());
                cal.add(Calendar.MONTH, TimeDatumDependentDuration.this.getMonths());
                cal.add(Calendar.DAY_OF_YEAR, TimeDatumDependentDuration.this.getDays());
                cal.add(Calendar.HOUR_OF_DAY, TimeDatumDependentDuration.this.getHours());
                cal.add(Calendar.MINUTE, TimeDatumDependentDuration.this.getMinutes());
                cal.add(Calendar.SECOND, TimeDatumDependentDuration.this.getSeconds());
                cal.add(Calendar.MILLISECOND, TimeDatumDependentDuration.this.getMillis());

                return cal.getTime();
            }
        };
    }

}
