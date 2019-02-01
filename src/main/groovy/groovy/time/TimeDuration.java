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
 * TimeDuration represents time periods expressed in units of hours, minutes,
 * seconds and milliseconds.
 * <p>
 * Whilst we can't say how long a month is without knowing the year and the name of the month,
 * we know how long a second is independent of the date.
 * <p>
 * This is not 100% true for minutes.
 * Minutes can be 59, 60 or 61 seconds long (due to leap seconds.)
 * <p>
 * If you ask Duration to convert itself to milliseconds then it will work on the basis of 60 seconds in a minute.
 * If you add or subtract it from a date it will take leap seconds into account.
 */

public class TimeDuration extends Duration {
    public TimeDuration(final int hours, final int minutes, final int seconds, final int millis) {
        super(0, hours, minutes, seconds, millis);
     }
    
    public TimeDuration(final int days, final int hours, final int minutes, final int seconds, final int millis) {
        super(days, hours, minutes, seconds, millis);
     }
    
    public Duration plus(final Duration rhs) {
        return new TimeDuration(this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                                this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                                this.getMillis() + rhs.getMillis());
    }
    
    public DatumDependentDuration plus(final DatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(rhs.getYears(), rhs.getMonths(),
                                              this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                                              this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                                              this.getMillis() + rhs.getMillis());
    }
    
    public Duration minus(final Duration rhs) {
        return new TimeDuration(this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                this.getMillis() - rhs.getMillis());
    }
    
    public DatumDependentDuration minus(final DatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(-rhs.getYears(), -rhs.getMonths(),
                                              this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                              this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                              this.getMillis() - rhs.getMillis());
    }
    
    public Date getAgo() {
        final Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DAY_OF_YEAR, -this.getDays());
        cal.add(Calendar.HOUR_OF_DAY, -this.getHours());
        cal.add(Calendar.MINUTE, -this.getMinutes());
        cal.add(Calendar.SECOND, -this.getSeconds());
        cal.add(Calendar.MILLISECOND, -this.getMillis());
        
        return cal.getTime();
    }        

    public From getFrom() {
        return new From() {
            public Date getNow() {
                final Calendar cal = Calendar.getInstance();

                cal.add(Calendar.DAY_OF_YEAR, TimeDuration.this.getDays());
                cal.add(Calendar.HOUR_OF_DAY, TimeDuration.this.getHours());
                cal.add(Calendar.MINUTE, TimeDuration.this.getMinutes());
                cal.add(Calendar.SECOND, TimeDuration.this.getSeconds());
                cal.add(Calendar.MILLISECOND, TimeDuration.this.getMillis());
                
                return cal.getTime();
            }
        };
    }
}
