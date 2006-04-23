/*
 * Created on Apr 22, 2006
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

public abstract class BaseDuration {
    protected final long millis;

    protected BaseDuration(final long millis) {
        this.millis = millis;
    }
    
    /**
     * @param cal
     * @param millis
     * 
     * Helper function to add a long number of Milliseconds to a Calendar
     * Calendar is brain dead and only allows an int to be added
     */
    public static void addMillisToCalendar(final Calendar cal, long millis) {
        if (millis > 0X100000000L) {
        final int days = (int)(millis / (24 * 60 * 60 * 1000));
        
            cal.add(Calendar.DATE, days);
            
            millis -= days * (24 * 60 * 60 * 1000);
        }
        
        cal.add(Calendar.MILLISECOND, (int)millis);
    }
    
    public long getMillis() {
        return this.millis;
    }
    public abstract Date getAgo();
    
    public abstract From getFrom();
    
    public static abstract class From {
        public abstract Date getNow();
    }
}
