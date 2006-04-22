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

package groovy.google.gdata;

import groovy.google.gdata.data.ContextDependentDuration;
import groovy.google.gdata.data.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


import com.google.gdata.client.*;
import com.google.gdata.client.calendar.*;
import com.google.gdata.data.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;

public class GDataCategory {
    public static EventFeed getFeed(final CalendarService self, final URL url) throws IOException, ServiceException {
        return self.getFeed(url, EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url) throws IOException, ServiceException {
        return self.getFeed(new URL(url), EventFeed.class);
    }
    
    public static void setUserCredentials(final GoogleService self, final List<String> creds) throws AuthenticationException {
        self.setUserCredentials(creds.get(0), creds.get(1));
    }
    
    public static String toUiString(final Duration self) {
        // TODO: make this format more user friendly
        return Long.toString(self.getMillis()) + " Milliseconds";
    }
    
    public static String toUiString(final ContextDependentDuration self) {
        // TODO: make this format more user friendly
        return Integer.toString(self.getYears()) + " Years " + Integer.toString(self.getMonths()) + " Months " + Long.toString(self.getMillis()) + " Milliseconds";
    }
    
    /*
     * Methods to support date and time arithmetic
     * These are in the Category to avoid putting Google related methods on Duration
     */
    
    public static DateTime plus (final DateTime self, final Duration rhs) {
        return new DateTime(self.getValue() + rhs.getMillis());
    }
    
    public static DateTime plus (final DateTime self, final ContextDependentDuration rhs) {
    // TODO: handle TIMEZONE
    final Calendar cal = new GregorianCalendar();
    long diff = self.getValue() - cal.getTimeInMillis();
    
        assert diff < 0X10000;        
        cal.add(Calendar.MILLISECOND, (int)diff);
        
        diff = rhs.getMillis() - cal.getTimeInMillis();
        assert diff < 0X10000;        
        cal.add(Calendar.MILLISECOND, (int)diff);
        
        cal.add(Calendar.YEAR, rhs.getYears());
        cal.add(Calendar.MONTH, rhs.getMonths());
        
        return new DateTime(cal.getTimeInMillis());
    }
    
    public static DateTime plus (final Duration self, final DateTime rhs) {
        return plus(rhs, self);
    }
    
    public static DateTime plus (final ContextDependentDuration self, final DateTime rhs) {
        return plus(rhs, self);
    }
    
    public static DateTime minus (final DateTime self, final Duration rhs) {
        return new DateTime(self.getValue() - rhs.getMillis());
    }
    
    public static DateTime minus (final DateTime self, final ContextDependentDuration rhs) {
        // TODO: handle TIMEZONE
        final Calendar cal = new GregorianCalendar();
        long diff = self.getValue() - cal.getTimeInMillis();
        
            assert diff < 0X10000;        
            cal.add(Calendar.MILLISECOND, (int)diff);
            
            diff = rhs.getMillis() - cal.getTimeInMillis();
            assert diff < 0X10000;        
            cal.add(Calendar.MILLISECOND, -(int)diff);
            
            cal.add(Calendar.YEAR, -rhs.getYears());
            cal.add(Calendar.MONTH, -rhs.getMonths());
            
            return new DateTime(cal.getTimeInMillis());
    }
    
    public static Duration minus (final DateTime self, final DateTime rhs) {
        return new Duration(self.getValue() - rhs.getValue());
    }
    
    /*
     * Methods on Integer to implement 1.week, 4.days etc.
     * Not Google depandant
     */
    
    public static Duration getWeeks(final Integer self) {
        return new Duration(self.intValue() * (7 * 24 * 60 * 60 * 1000));
    }
    
    public static Duration getWeek(final Integer self) {
        return getWeeks(self);
    }
    
    public static Duration getDays(final Integer self) {
        return new Duration(self.intValue() * (24 * 60 * 60 * 1000));
    }
    
    public static Duration getDay(final Integer self) {
        return getDays(self);
    }
    
    public static Duration getHours(final Integer self) {
        return new Duration(self.intValue() * (60 * 60 * 1000));
    }
    
    public static Duration getHour(final Integer self) {
        return getHours(self);
    }
    
    public static Duration getMinutes(final Integer self) {
        return new Duration(self.intValue() * (60 * 1000));
    }
    
    public static Duration getMinute(final Integer self) {
        return getMinutes(self);
    }
    
    public static Duration getSeconds(final Integer self) {
        return new Duration(self.intValue() * (1000));
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
