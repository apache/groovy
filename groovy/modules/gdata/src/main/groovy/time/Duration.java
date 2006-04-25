/*
 * Created on Apr 21, 2006
 *
 * Copyright 2006 John G. Wilson
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package groovy.time;

import java.util.Calendar;
import java.util.Date;

/**
 * @author John Wilson tug@wilson.co.uk
 * 
 * Duration represents time periods which have values independant of the context.
 * So, whilst we can't say how long a month is without knowing the year and the name of the month,
 * we know how long a second is independant of the date.
 * 
 * This is not 100% true for days and minutes.
 * Days can atually be 23, 24 or 25 hours long (due to daylight saving adjustments)
 * Minutes can be 59, 60 or 61 seconds long (due to leap seconds)
 * 
 * If you ask Duration to convert itself to milisaconds then it will work on the basis of 24 hours
 * in a day and 60 seconds in a minute. If you add or subtract it from a date it will take daylight
 * saving, etc. into account.
 *
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
    
    public ContextDependentDuration plus(final ContextDependentDuration rhs) {
        return rhs.plus(this);
    }
    
    public Duration minus(final Duration rhs) {
        return new Duration(this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                            this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                            this.getMillis() - rhs.getMillis());
    }
    
    public ContextDependentDuration minus(final ContextDependentDuration rhs) {
        return new ContextDependentDuration(-rhs.getYears(), -rhs.getMonths(),
                                            this.getDays() - rhs.getDays(), this.getHours() - rhs.getHours(),
                                            this.getMinutes() - rhs.getMinutes(), this.getSeconds() - rhs.getSeconds(),
                                            this.getMillis() - rhs.getMillis());
    }
    
    public long toMilliseconds() {
        return ((((((this.getDays() * 24 ) + this.getHours()) * 60 + this.getMinutes()) * 60) * this.getSeconds()) * 10000) + this.getMillis();
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

                cal.add(Calendar.DAY_OF_YEAR, Duration.this.getDays());
                cal.add(Calendar.HOUR_OF_DAY, Duration.this.getHours());
                cal.add(Calendar.MINUTE, Duration.this.getMinutes());
                cal.add(Calendar.SECOND, Duration.this.getSeconds());
                cal.add(Calendar.MILLISECOND, Duration.this.getMillis());
                
                return cal.getTime();
            }
            
            public Date getToday() {
            final Calendar cal = Calendar.getInstance();
                
                cal.add(Calendar.DAY_OF_YEAR, Duration.this.getDays());
                cal.set(Calendar.HOUR_OF_DAY, Duration.this.getHours());
                cal.set(Calendar.MINUTE, Duration.this.getMinutes());
                cal.set(Calendar.SECOND, Duration.this.getSeconds());
                cal.set(Calendar.MILLISECOND, Duration.this.getMillis());
               
                return cal.getTime();
            }
        };
    }
}
