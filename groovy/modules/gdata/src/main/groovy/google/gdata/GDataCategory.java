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

import groovy.time.BaseDuration;
import groovy.time.ContextDependentDuration;
import groovy.time.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.extensions.EventFeed;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GDataCategory {
    public static EventFeed getFeed(final CalendarService self, final URL url) throws IOException, ServiceException {
        return self.getFeed(url, EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final Date to, int maxEntries) throws IOException, ServiceException {
        return getFeed(self, url.toExternalForm(), from, to, maxEntries);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final Date to) throws IOException, ServiceException {
        return getFeed(self, url.toExternalForm(), from, to);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final ContextDependentDuration duration, int maxEntries) throws IOException, ServiceException {
        return getFeed(self, url.toExternalForm(), from, duration, maxEntries);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final ContextDependentDuration duration) throws IOException, ServiceException {
        return getFeed(self, url.toExternalForm(), from, duration);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final Duration duration, int maxEntries) throws IOException, ServiceException {
        return getFeed(self, url.toExternalForm(), from, duration, maxEntries);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final Duration duration) throws IOException, ServiceException {
        return getFeed(self, url.toExternalForm(), from, duration);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url) throws IOException, ServiceException {
        return self.getFeed(new URL(url), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final Date to, int maxEntries) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = new DateTime(to.getTime());
System.out.println(new URL(url + "?start-min=" + from1 + "&start-max=" + to1 + "&max-results=" + maxEntries).toExternalForm());
        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1+ "&max-results=" + maxEntries), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final Date to) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = new DateTime(to.getTime());
        
        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final ContextDependentDuration duration, int maxEntries) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = plus(from1, duration);
System.out.println(new URL(url + "?start-min=" + from1 + "&start-max=" + to1 + "&max-results=" + maxEntries).toExternalForm());
        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1 + "&max-results=" + maxEntries), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final ContextDependentDuration duration) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = plus(from1, duration);
        
        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final Duration duration, int maxEntries) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = plus(from1, duration);
System.out.println(new URL(url + "?start-min=" + from1 + "&start-max=" + to1 + "&max-results=" + maxEntries).toExternalForm());
        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1 + "&max-results=" + maxEntries), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final Duration duration) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = plus(from1, duration);
        
        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1), EventFeed.class);
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
    final Calendar cal = Calendar.getInstance();
    
        cal.setTimeInMillis(self.getValue() + rhs.getMillis());
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
        final Calendar cal = Calendar.getInstance();
        
            cal.setTimeInMillis(self.getValue() - rhs.getMillis());
            cal.add(Calendar.YEAR, -rhs.getYears());
            cal.add(Calendar.MONTH, -rhs.getMonths());
            
            return new DateTime(cal.getTimeInMillis());
    }
    
    public static Duration minus (final DateTime self, final DateTime rhs) {
        return new Duration(self.getValue() - rhs.getValue());
    }
}
