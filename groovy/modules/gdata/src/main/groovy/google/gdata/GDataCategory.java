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

import groovy.lang.Buildable;
import groovy.lang.Closure;
import groovy.time.DatumDependentDuration;
import groovy.time.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gdata.client.GoogleService;
import com.google.gdata.client.calendar.CalendarService;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Person;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.extensions.EventEntry;
import com.google.gdata.data.extensions.EventFeed;
import com.google.gdata.data.extensions.When;
import com.google.gdata.util.AuthenticationException;
import com.google.gdata.util.ServiceException;

public class GDataCategory {
    
    //
    // Extra CalendarService methods
    //
    
    public static EventFeed getFeed(final CalendarService self, final URL url) throws IOException, ServiceException {
        return self.getFeed(url, EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final Date to, int maxEntries) throws IOException, ServiceException {
        return getFeed(self, url.toExternalForm(), from, to, maxEntries);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final Date to) throws IOException, ServiceException {
        return getFeed(self, url.toExternalForm(), from, to);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final DatumDependentDuration duration, int maxEntries) throws IOException, ServiceException {
        return getFeed(self, url.toExternalForm(), from, duration, maxEntries);
    }
    
    public static EventFeed getFeed(final CalendarService self, final URL url, final Date from, final DatumDependentDuration duration) throws IOException, ServiceException {
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

        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1+ "&max-results=" + maxEntries), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final Date to) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = new DateTime(to.getTime());
        
        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final DatumDependentDuration duration, int maxEntries) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = plus(from1, duration);

        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1 + "&max-results=" + maxEntries), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final DatumDependentDuration duration) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = plus(from1, duration);
        
        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final Duration duration, int maxEntries) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = plus(from1, duration);

        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1 + "&max-results=" + maxEntries), EventFeed.class);
    }
    
    public static EventFeed getFeed(final CalendarService self, final String url, final Date from, final Duration duration) throws IOException, ServiceException {
    final DateTime from1 = new DateTime(from.getTime());
    final DateTime to1 = plus(from1, duration);
        
        return self.getFeed(new URL(url + "?start-min=" + from1 + "&start-max=" + to1), EventFeed.class);
    }
    
    public static EventEntry insert(final CalendarService self, final String url, EventEntry entry) throws IOException, ServiceException {
        return self.insert(new URL(url), entry);
    }
    
    public static void setUserCredentials(final GoogleService self, final List<String> creds) throws AuthenticationException {
        self.setUserCredentials(creds.get(0), creds.get(1));
    }
    
    public static String toUiString(final Duration self) {
        // TODO: make this format more user friendly
        return Long.toString(self.getMillis()) + " Milliseconds";
    }
    
    public static String toUiString(final DatumDependentDuration self) {
        // TODO: make this format more user friendly
        return Integer.toString(self.getYears()) + " Years " + Integer.toString(self.getMonths()) + " Months " + Long.toString(self.getMillis()) + " Milliseconds";
    }
    
    //
    // Extra EventEntry methods
    //
    
    public static void setTitle1(final EventEntry self, final String title) {
        self.setTitle(new PlainTextConstruct(title));
    }
    
    public static void setTitle(final EventEntry self, final Closure titleBuilder) {
        // TODO: implement this
    }
    
    public static void setTitle(final EventEntry self, final Buildable titleBuilder) {
        // TODO: implement this
    }
    
    public static void setContent1(final EventEntry self, final String content) {
        self.setTitle(new PlainTextConstruct(content));
    }
    
    public static void setContent(final EventEntry self, final Closure contentBuilder) {
        // TODO: implement this
    }
    
    public static void setContent(final EventEntry self, final Buildable contentBuilder) {
        // TODO: implement this
    }
    
    public static void setAuthor(final EventEntry self, final Person author) {
        self.getAuthors().add(author);
    }
    
    public static void setAuthor(final EventEntry self, final List<Person> authors) {
        self.getAuthors().addAll(authors);
    }
    
    public static void setTime(final EventEntry self, final When when) {
        self.addTime(when);
    }
    
    public static void setTime(final EventEntry self, final List<When> whens) {
        self.getTimes().addAll(whens);
    }
    
    //
    // Extra When methods
    //
    
    public static void setStart(final When self, final Date start) {
        self.setStartTime(new DateTime(start));
    }
    
    public static void setEnd(final When self, final Date end) {
        self.setEndTime(new DateTime(end));
    }
    
    /*
     * Methods to support date and time arithmetic
     * These are in the Category to avoid putting Google related methods on Duration
     */
    
    public static DateTime plus (final DateTime self, final Duration rhs) {
    // TODO: handle TIMEZONE
    final Calendar cal = Calendar.getInstance();
    
        cal.setTimeInMillis(self.getValue());
        cal.add(Calendar.DAY_OF_YEAR, rhs.getDays());
        cal.add(Calendar.HOUR_OF_DAY, rhs.getHours());
        cal.add(Calendar.MINUTE, rhs.getMinutes());
        cal.add(Calendar.SECOND, rhs.getSeconds());
        cal.add(Calendar.MILLISECOND, rhs.getMillis());
        
        return new DateTime(cal.getTimeInMillis());
    }
    
    public static DateTime plus (final DateTime self, final DatumDependentDuration rhs) {
    // TODO: handle TIMEZONE
    final Calendar cal = Calendar.getInstance();
    
        cal.setTimeInMillis(self.getValue());
        cal.add(Calendar.YEAR, rhs.getYears());
        cal.add(Calendar.MONTH, rhs.getMonths());
        cal.add(Calendar.DAY_OF_YEAR, rhs.getDays());
        cal.add(Calendar.HOUR_OF_DAY, rhs.getHours());
        cal.add(Calendar.MINUTE, rhs.getMinutes());
        cal.add(Calendar.SECOND, rhs.getSeconds());
        cal.add(Calendar.MILLISECOND, rhs.getMillis());
        
        return new DateTime(cal.getTimeInMillis());
    }
    
    public static DateTime plus (final Duration self, final DateTime rhs) {
        return plus(rhs, self);
    }
    
    public static DateTime plus (final DatumDependentDuration self, final DateTime rhs) {
        return plus(rhs, self);
    }
    
    public static DateTime minus (final DateTime self, final Duration rhs) {
        return new DateTime(self.getValue() - rhs.getMillis());
    }
    
    public static DateTime minus (final DateTime self, final DatumDependentDuration rhs) {
        // TODO: handle TIMEZONE
        final Calendar cal = Calendar.getInstance();
        
            cal.add(Calendar.YEAR, -rhs.getYears());
            cal.add(Calendar.MONTH, -rhs.getMonths());
            cal.add(Calendar.DAY_OF_YEAR, -rhs.getDays());
            cal.add(Calendar.HOUR_OF_DAY, -rhs.getHours());
            cal.add(Calendar.MINUTE, -rhs.getMinutes());
            cal.add(Calendar.SECOND, -rhs.getSeconds());
            cal.add(Calendar.MILLISECOND, -rhs.getMillis());
           
            return new DateTime(cal.getTimeInMillis());
    }
}
