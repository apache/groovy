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

public class Duration extends BaseDuration {
    public Duration(final long millis) {
        super(millis);
    }
    
    public Duration plus(final Duration rhs) {
        return new Duration(this.getMillis() + rhs.getMillis());
    }
    
    public ContextDependentDuration plus(final ContextDependentDuration rhs) {
        return rhs.plus(this);
    }
    
    public Duration minus(final Duration rhs) {
        return new Duration(this.getMillis() - rhs.getMillis());
    }
    
    public ContextDependentDuration minus(final ContextDependentDuration rhs) {
        return new ContextDependentDuration(-rhs.getYears(), -rhs.getMonths(), this.getMillis() - rhs.getMillis());
    }
    
    public Date getAgo() {
    final Calendar cal = Calendar.getInstance();
        
        BaseDuration.addMillisToCalendar(cal, -this.millis);
        
        return cal.getTime();
    }
    
    
    public From getFrom() {
        return new From() {
            public Date getNow() {
            final Calendar cal = Calendar.getInstance();
            
                BaseDuration.addMillisToCalendar(cal, Duration.this.millis);
                
                return cal.getTime();
            }
        };
    }
}
