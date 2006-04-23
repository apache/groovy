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
package groovy.google.gdata.data;

import java.util.Calendar;
import java.util.Date;

public class ContextDependentDuration extends BaseDuration {
    private final int years;
    private final int months;
    public ContextDependentDuration(final int years, final int months, final long millis) {
        super(millis);
        this.years = years;
        this.months = months;
    }

    public int getMonths() {
        return this.months;
    }

    public int getYears() {
        return this.years;
    }
    
    public ContextDependentDuration plus(final ContextDependentDuration rhs) {
        return new ContextDependentDuration(this.getYears() + rhs.getYears(), this.getMonths() + rhs.getMonths(), this.getMillis() + rhs.getMillis());
    }
    
    public ContextDependentDuration plus(final Duration rhs) {
        return new ContextDependentDuration(this.getYears(), this.getMonths(), this.getMillis() + rhs.getMillis());
    }
    
    public ContextDependentDuration minus(final ContextDependentDuration rhs) {
        return new ContextDependentDuration(this.getYears() - rhs.getYears(), this.getMonths() - rhs.getMonths(), this.getMillis() - rhs.getMillis());
    }
    
    public ContextDependentDuration minus(final Duration rhs) {
        return new ContextDependentDuration(this.getYears(), this.getMonths(), this.getMillis() - rhs.getMillis());
    }
    
    public Date getAgo() {
    final Calendar cal = Calendar.getInstance();
        
        cal.add(Calendar.YEAR, -this.years);
        cal.add(Calendar.MONTH, -this.months);
        BaseDuration.addMillisToCalendar(cal, -this.millis);
        
        return cal.getTime();
    }
    
    public From getFrom() {
        return new From() {
            public Date getNow() {
            final Calendar cal = Calendar.getInstance();
                
                cal.add(Calendar.YEAR, ContextDependentDuration.this.years);
                cal.add(Calendar.MONTH, ContextDependentDuration.this.months);
                BaseDuration.addMillisToCalendar(cal, ContextDependentDuration.this.millis);
                
                return cal.getTime();
            }
        };
    }
}
