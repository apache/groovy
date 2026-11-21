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
 * The result of mixing a {@link TimeDuration} with a {@link DatumDependentDuration}.
 */
public class TimeDatumDependentDuration extends DatumDependentDuration {
    /**
     * Creates a mixed duration from the given calendar and time fields.
     */
    public TimeDatumDependentDuration(int years, int months, int days, int hours, int minutes, int seconds, int millis) {
        super(years, months, days, hours, minutes, seconds, millis);
    }

    /**
     * Keeps this duration's years and months and adds {@code rhs}'s day and
     * time fields, returning a {@link TimeDatumDependentDuration}.
     */
    @Override
    public DatumDependentDuration plus(final Duration rhs) {
        return new TimeDatumDependentDuration(getYears(), getMonths(),
                getDays() + rhs.getDays(), getHours() + rhs.getHours(),
                getMinutes() + rhs.getMinutes(), getSeconds() + rhs.getSeconds(),
                getMillis() + rhs.getMillis());
    }

    /**
     * Adds every calendar and time field of {@code rhs}, returning a
     * {@link TimeDatumDependentDuration}.
     */
    @Override
    public DatumDependentDuration plus(final DatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(getYears() + rhs.getYears(), getMonths() + rhs.getMonths(),
                getDays() + rhs.getDays(), getHours() + rhs.getHours(),
                getMinutes() + rhs.getMinutes(), getSeconds() + rhs.getSeconds(),
                getMillis() + rhs.getMillis());
    }

    /**
     * Keeps this duration's years and months and subtracts {@code rhs}'s day
     * and time fields, returning a {@link TimeDatumDependentDuration}.
     */
    @Override
    public DatumDependentDuration minus(final Duration rhs) {
        return new TimeDatumDependentDuration(getYears(), getMonths(),
                getDays() - rhs.getDays(), getHours() - rhs.getHours(),
                getMinutes() - rhs.getMinutes(), getSeconds() - rhs.getSeconds(),
                getMillis() - rhs.getMillis());
    }

    /**
     * Subtracts every calendar and time field of {@code rhs}, returning a
     * {@link TimeDatumDependentDuration}.
     */
    @Override
    public DatumDependentDuration minus(final DatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(getYears() - rhs.getYears(), getMonths() - rhs.getMonths(),
                getDays() - rhs.getDays(), getHours() - rhs.getHours(),
                getMinutes() - rhs.getMinutes(), getSeconds() - rhs.getSeconds(),
                getMillis() - rhs.getMillis());
    }
}
