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

/**
 * A context-independent duration: days and below, whose length is known without a datum.
 * <p>
 * DEQUIRKED: {@code toMilliseconds()}, {@code getAgo()} and {@code getFrom()} are now
 * inherited from {@link BaseDuration} (uniform, deterministic, time-preserving); only the
 * type-preserving arithmetic remains here.
 */
public class Duration extends BaseDuration {
    /**
     * Stores the day and time components. Years and months stay zero.
     *
     * @param days whole days
     * @param hours hours
     * @param minutes minutes
     * @param seconds seconds
     * @param millis milliseconds
     */
    public Duration(final int days, final int hours, final int minutes, final int seconds, final int millis) {
        super(days, hours, minutes, seconds, millis);
    }

    /**
     * Adds each day and time component of {@code rhs}. The components are not normalized.
     *
     * @param rhs duration to add
     * @return the component-wise sum
     */
    public Duration plus(final Duration rhs) {
        return new Duration(getDays() + rhs.getDays(), getHours() + rhs.getHours(),
                getMinutes() + rhs.getMinutes(), getSeconds() + rhs.getSeconds(),
                getMillis() + rhs.getMillis());
    }

    /**
     * Adds this duration to {@code rhs} by calling {@code rhs.plus(this)},
     * so the result is a {@code TimeDuration}.
     *
     * @param rhs duration to add
     * @return the sum, typed as {@code rhs} computes it
     */
    public Duration plus(final TimeDuration rhs) {
        return rhs.plus(this);
    }

    /**
     * Adds this duration to {@code rhs} by calling {@code rhs.plus(this)}.
     * Years and months stay those of {@code rhs}; the day and time components are added.
     *
     * @param rhs duration to add
     * @return the sum, typed as {@code rhs} computes it
     */
    public DatumDependentDuration plus(final DatumDependentDuration rhs) {
        return rhs.plus(this);
    }

    /**
     * Subtracts each day and time component of {@code rhs}. The components are not normalized.
     *
     * @param rhs duration to subtract
     * @return the component-wise difference
     */
    public Duration minus(final Duration rhs) {
        return new Duration(getDays() - rhs.getDays(), getHours() - rhs.getHours(),
                getMinutes() - rhs.getMinutes(), getSeconds() - rhs.getSeconds(),
                getMillis() - rhs.getMillis());
    }

    /**
     * Subtracts each day and time component of {@code rhs} and returns a {@code TimeDuration}.
     * The components are not normalized.
     *
     * @param rhs duration to subtract
     * @return the component-wise difference
     */
    public TimeDuration minus(final TimeDuration rhs) {
        return new TimeDuration(getDays() - rhs.getDays(), getHours() - rhs.getHours(),
                getMinutes() - rhs.getMinutes(), getSeconds() - rhs.getSeconds(),
                getMillis() - rhs.getMillis());
    }

    /**
     * Subtracts {@code rhs}. Years and months of {@code rhs} are negated, and the
     * day and time components are subtracted. The result is a {@code DatumDependentDuration}.
     *
     * @param rhs duration to subtract
     * @return the component-wise difference
     */
    public DatumDependentDuration minus(final DatumDependentDuration rhs) {
        return new DatumDependentDuration(-rhs.getYears(), -rhs.getMonths(),
                getDays() - rhs.getDays(), getHours() - rhs.getHours(),
                getMinutes() - rhs.getMinutes(), getSeconds() - rhs.getSeconds(),
                getMillis() - rhs.getMillis());
    }

    /**
     * Subtracts {@code rhs}. Years and months of {@code rhs} are negated, and the
     * day and time components are subtracted. The result is a {@code TimeDatumDependentDuration}.
     *
     * @param rhs duration to subtract
     * @return the component-wise difference
     */
    public TimeDatumDependentDuration minus(final TimeDatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(-rhs.getYears(), -rhs.getMonths(),
                getDays() - rhs.getDays(), getHours() - rhs.getHours(),
                getMinutes() - rhs.getMinutes(), getSeconds() - rhs.getSeconds(),
                getMillis() - rhs.getMillis());
    }
}
