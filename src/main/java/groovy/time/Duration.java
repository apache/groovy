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
    public Duration(final int days, final int hours, final int minutes, final int seconds, final int millis) {
        super(days, hours, minutes, seconds, millis);
    }
    
    public Duration plus(final Duration rhs) {
        return new Duration(this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                            this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                            this.getMillis() + rhs.getMillis());
    }

    public Duration plus(final TimeDuration rhs) {
        return rhs.plus(this);
    }
    
    public DatumDependentDuration plus(final DatumDependentDuration rhs) {
        return rhs.plus(this);
    }
    
    public Duration minus(final Duration rhs) {
        return new Duration(this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                            this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                            this.getMillis() - rhs.getMillis());
    }
    
    public TimeDuration minus(final TimeDuration rhs) {
        return new TimeDuration(this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                this.getMillis() - rhs.getMillis());
    }
    
    public DatumDependentDuration minus(final DatumDependentDuration rhs) {
        return new DatumDependentDuration(-rhs.getYears(), -rhs.getMonths(),
                                          this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                          this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                          this.getMillis() - rhs.getMillis());
    }
    
    public TimeDatumDependentDuration minus(final TimeDatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(-rhs.getYears(), -rhs.getMonths(),
                                              this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                              this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                              this.getMillis() - rhs.getMillis());
    }
    
    @Override
    public long toMilliseconds() {
        return ((((((long)(this.getDays() * 24 ) + this.getHours()) * 60 + this.getMinutes()) * 60) + this.getSeconds()) * 1000) + this.getMillis();
    }
    
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
     
    @Override
    public From getFrom() {
        return new From() {
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
