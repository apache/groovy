/*
 * Created on Apr 23, 2006
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
package org.codehaus.groovy.runtime;

import groovy.time.ContextDependentDuration;
import groovy.time.Duration;

public class TimeCategory {
    
    /*
     * Methods on Integer to implement 1.week, 4.days etc.
     */
    
    public static Duration getWeeks(final Integer self) {
        return new Duration(self.longValue() * (7 * 24 * 60 * 60 * 1000));
    }
    
    public static Duration getWeek(final Integer self) {
        return getWeeks(self);
    }
    
    public static Duration getDays(final Integer self) {
        return new Duration(self.longValue() * (24 * 60 * 60 * 1000));
    }
    
    public static Duration getDay(final Integer self) {
        return getDays(self);
    }
    
    public static Duration getHours(final Integer self) {
        return new Duration(self.longValue() * (60 * 60 * 1000));
    }
    
    public static Duration getHour(final Integer self) {
        return getHours(self);
    }
    
    public static Duration getMinutes(final Integer self) {
        return new Duration(self.longValue() * (60 * 1000));
    }
    
    public static Duration getMinute(final Integer self) {
        return getMinutes(self);
    }
    
    public static Duration getSeconds(final Integer self) {
        return new Duration(self.longValue() * (1000));
    }
    
    public static Duration getSecond(final Integer self) {
        return getSeconds(self);
    }
    
    /*
     * Methods on Integer to implement 1.month, 4.years etc.
     * These are Google dependant
     */
    
    public static ContextDependentDuration getMonths(final Integer self) {
        return new ContextDependentDuration(0, self.intValue(), 0l);
    }
    
    public static ContextDependentDuration getMonth(final Integer self) {
        return getMonths(self);
    }
    
    public static ContextDependentDuration getYears(final Integer self) {
        return new ContextDependentDuration(self.intValue(), 0, 0l);
    }
    
    public static ContextDependentDuration getYear(final Integer self) {
        return getYears(self);
    }

}
