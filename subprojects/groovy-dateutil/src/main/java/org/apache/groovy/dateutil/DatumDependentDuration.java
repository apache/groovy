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
 * A duration that includes years and/or months, whose exact length depends on the datum.
 * <p>
 * DEQUIRKED: {@code toMilliseconds()} is now deterministic (see {@link BaseDuration}); it
 * no longer resolves against "now". {@code getAgo()}/{@code getFrom()} are inherited and no
 * longer zero the time-of-day.
 */
public class DatumDependentDuration extends BaseDuration {
    public DatumDependentDuration(final int years, final int months, final int days, final int hours, final int minutes, final int seconds, final int millis) {
        super(years, months, days, hours, minutes, seconds, millis);
    }

    public DatumDependentDuration plus(final DatumDependentDuration rhs) {
        return new DatumDependentDuration(getYears() + rhs.getYears(), getMonths() + rhs.getMonths(),
                getDays() + rhs.getDays(), getHours() + rhs.getHours(),
                getMinutes() + rhs.getMinutes(), getSeconds() + rhs.getSeconds(),
                getMillis() + rhs.getMillis());
    }

    public DatumDependentDuration plus(final TimeDatumDependentDuration rhs) {
        return rhs.plus(this);
    }

    public DatumDependentDuration plus(final Duration rhs) {
        return new DatumDependentDuration(getYears(), getMonths(),
                getDays() + rhs.getDays(), getHours() + rhs.getHours(),
                getMinutes() + rhs.getMinutes(), getSeconds() + rhs.getSeconds(),
                getMillis() + rhs.getMillis());
    }

    public DatumDependentDuration plus(final TimeDuration rhs) {
        return rhs.plus(this);
    }

    public DatumDependentDuration minus(final DatumDependentDuration rhs) {
        return new DatumDependentDuration(getYears() - rhs.getYears(), getMonths() - rhs.getMonths(),
                getDays() - rhs.getDays(), getHours() - rhs.getHours(),
                getMinutes() - rhs.getMinutes(), getSeconds() - rhs.getSeconds(),
                getMillis() - rhs.getMillis());
    }

    public DatumDependentDuration minus(final Duration rhs) {
        return new DatumDependentDuration(getYears(), getMonths(),
                getDays() - rhs.getDays(), getHours() - rhs.getHours(),
                getMinutes() - rhs.getMinutes(), getSeconds() - rhs.getSeconds(),
                getMillis() - rhs.getMillis());
    }
}
