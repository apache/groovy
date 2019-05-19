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
    public TimeDatumDependentDuration(int years, int months, int days, int hours, int minutes, int seconds, int millis) {
        super(years, months, days, hours, minutes, seconds, millis);
    }
    
    public DatumDependentDuration plus(final Duration rhs) {
        return new TimeDatumDependentDuration(this.getYears(), this.getMonths(),
                                              this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                                              this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                                              this.getMillis() + rhs.getMillis());
    }
    
    public DatumDependentDuration plus(final DatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(this.getYears() + rhs.getYears(), this.getMonths() + rhs.getMonths(),
                                              this.getDays() + rhs.getDays(), this.getHours() + rhs.getHours(),
                                              this.getMinutes() + rhs.getMinutes(), this.getSeconds() + rhs.getSeconds(),
                                              this.getMillis() + rhs.getMillis());
    }
    
    public DatumDependentDuration minus(final Duration rhs) {
        return new TimeDatumDependentDuration(this.getYears(), this.getMonths(),
                                              this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                              this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                              this.getMillis() - rhs.getMillis());
    }
    
    public DatumDependentDuration minus(final DatumDependentDuration rhs) {
        return new TimeDatumDependentDuration(this.getYears() - rhs.getYears(), this.getMonths() - rhs.getMonths(),
                                              this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                              this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                              this.getMillis() - rhs.getMillis());
    }
    
    public From getFrom() {
        return new From() {
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
