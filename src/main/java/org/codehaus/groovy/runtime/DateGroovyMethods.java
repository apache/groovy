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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * This class defines new groovy methods which appear on normal JDK
 * Date and Calendar classes inside the Groovy environment.
 */
public class DateGroovyMethods extends DefaultGroovyMethodsSupport {

    /**
     * Support the subscript operator for a Date.
     *
     * @param self  a Date
     * @param field a Calendar field, e.g. MONTH
     * @return the value for the given field, e.g. FEBRUARY
     * @see java.util.Calendar
     * @since 1.5.5
     */
    public static int getAt(Date self, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        return cal.get(field);
    }

    /**
     * Convert a Date to a Calendar.
     *
     * @param self a Date
     * @return a Calendar corresponding to the given Date
     * @since 1.7.6
     */
    public static Calendar toCalendar(Date self) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        return cal;
    }

    /**
     * Support the subscript operator for a Calendar.
     *
     * @param self  a Calendar
     * @param field a Calendar field, e.g. MONTH
     * @return the value for the given field, e.g. FEBRUARY
     * @see java.util.Calendar
     * @since 1.7.3
     */
    public static int getAt(Calendar self, int field) {
        return self.get(field);
    }

    /**
     * Support the subscript operator for mutating a Calendar.
     * Example usage:
     * <pre>
     * import static java.util.Calendar.*
     * def cal = Calendar.instance
     * cal[DAY_OF_WEEK] = MONDAY
     * cal[MONTH] = MARCH
     * println cal.time // A Monday in March
     * </pre>
     *
     * @param self  A Calendar
     * @param field A Calendar field, e.g. MONTH
     * @param value The value for the given field, e.g. FEBRUARY
     * @see java.util.Calendar#set(int, int)
     * @since 1.7.3
     */
    public static void putAt(Calendar self, int field, int value) {
        self.set(field, value);
    }

    /**
     * Support the subscript operator for mutating a Date.
     *
     * @param self  A Date
     * @param field A Calendar field, e.g. MONTH
     * @param value The value for the given field, e.g. FEBRUARY
     * @see #putAt(java.util.Calendar, int, int)
     * @see java.util.Calendar#set(int, int)
     * @since 1.7.3
     */
    public static void putAt(Date self, int field, int value) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        putAt(cal, field, value);
        self.setTime(cal.getTimeInMillis());
    }

    /**
     * Support mutating a Calendar with a Map.
     * <p>
     * The map values are the normal values provided as the
     * second parameter to <code>java.util.Calendar#set(int, int)</code>.
     * The keys can either be the normal fields values provided as
     * the first parameter to that method or one of the following Strings:
     * <table border="1" cellpadding="4">
     *   <caption>Calendar index values</caption>
     *   <tr><td>year</td><td>Calendar.YEAR</td></tr>
     *   <tr><td>month</td><td>Calendar.MONTH</td></tr>
     *   <tr><td>date</td><td>Calendar.DATE</td></tr>
     *   <tr><td>dayOfMonth</td><td>Calendar.DATE</td></tr>
     *   <tr><td>hourOfDay</td><td>Calendar.HOUR_OF_DAY</td></tr>
     *   <tr><td>minute</td><td>Calendar.MINUTE</td></tr>
     *   <tr><td>second</td><td>Calendar.SECOND</td></tr>
     * </table>
     * Example usage:
     * <pre>
     * import static java.util.Calendar.*
     * def cal = Calendar.instance
     * def m = [:]
     * m[YEAR] = 2010
     * m[MONTH] = DECEMBER
     * m[DATE] = 25
     * cal.set(m)
     * println cal.time // Christmas 2010
     *
     * cal.set(year:2011, month:DECEMBER, date:25)
     * println cal.time // Christmas 2010
     * </pre>
     *
     * @param self    A Calendar
     * @param updates A Map of Calendar keys and values
     * @see java.util.Calendar#set(int, int)
     * @see java.util.Calendar#set(int, int, int, int, int, int)
     * @since 1.7.3
     */
    public static void set(Calendar self, Map<Object, Integer> updates) {
        for (Map.Entry<Object, Integer> entry : updates.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof String) key = CAL_MAP.get(key);
            if (key instanceof Integer) self.set((Integer) key, entry.getValue());
        }
    }

    /**
     * Legacy alias for copyWith. Will be deprecated and removed in future versions of Groovy.
     *
     * @see #copyWith(java.util.Calendar, java.util.Map)
     * @since 1.7.3
     */
    public static Calendar updated(Calendar self, Map<Object, Integer> updates) {
        Calendar result = (Calendar) self.clone();
        set(result, updates);
        return result;
    }

    /**
     * Support creating a new Date having similar properties to
     * an existing Date (which remains unaltered) but with
     * some fields updated according to a Map of changes.
     * <p>
     * Example usage:
     * <pre>
     * import static java.util.Calendar.YEAR
     * def now = Calendar.instance
     * def nextYear = now[YEAR] + 1
     * def oneYearFromNow = now.copyWith(year: nextYear)
     * println now.time
     * println oneYearFromNow.time
     * </pre>
     *
     * @param self    A Calendar
     * @param updates A Map of Calendar keys and values
     * @return The newly created Calendar
     * @see java.util.Calendar#set(int, int)
     * @see java.util.Calendar#set(int, int, int, int, int, int)
     * @see #set(java.util.Calendar, java.util.Map)
     * @since 2.2.0
     */
    public static Calendar copyWith(Calendar self, Map<Object, Integer> updates) {
        Calendar result = (Calendar) self.clone();
        set(result, updates);
        return result;
    }

    /**
     * Support mutating a Date with a Map.
     * <p>
     * The map values are the normal values provided as the
     * second parameter to <code>java.util.Calendar#set(int, int)</code>.
     * The keys can either be the normal fields values provided as
     * the first parameter to that method or one of the following Strings:
     * <table border="1" cellpadding="4">
     *   <caption>Calendar index values</caption>
     *   <tr><td>year</td><td>Calendar.YEAR</td></tr>
     *   <tr><td>month</td><td>Calendar.MONTH</td></tr>
     *   <tr><td>date</td><td>Calendar.DATE</td></tr>
     *   <tr><td>dayOfMonth</td><td>Calendar.DATE</td></tr>
     *   <tr><td>hourOfDay</td><td>Calendar.HOUR_OF_DAY</td></tr>
     *   <tr><td>minute</td><td>Calendar.MINUTE</td></tr>
     *   <tr><td>second</td><td>Calendar.SECOND</td></tr>
     * </table>
     * Example usage:
     * <pre>
     * import static java.util.Calendar.YEAR
     * def date = new Date()
     * def nextYear = date[YEAR] + 1
     * date.set(year: nextYear)
     * println date
     * </pre>
     *
     * @param self    A Date
     * @param updates A Map of Calendar keys and values
     * @see java.util.Calendar#set(int, int)
     * @see #set(java.util.Calendar, java.util.Map)
     * @since 1.7.3
     */
    public static void set(Date self, Map<Object, Integer> updates) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        set(cal, updates);
        self.setTime(cal.getTimeInMillis());
    }

    /**
     * Legacy alias for copyWith. Will be deprecated and removed in future versions of Groovy.
     *
     * @see #copyWith(java.util.Date, java.util.Map)
     * @since 1.7.3
     */
    public static Date updated(Date self, Map<Object, Integer> updates) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        set(cal, updates);
        return cal.getTime();
    }

    /**
     * Support creating a new Date having similar properties to
     * an existing Date (which remains unaltered) but with
     * some fields updated according to a Map of changes.
     * <p>
     * Example usage:
     * <pre>
     * import static java.util.Calendar.YEAR
     * def today = new Date()
     * def nextYear = today[YEAR] + 1
     * def oneYearFromNow = today.copyWith(year: nextYear)
     * println today
     * println oneYearFromNow
     * </pre>
     *
     * @param self    A Date
     * @param updates A Map of Calendar keys and values
     * @return The newly created Date
     * @see java.util.Calendar#set(int, int)
     * @see #set(java.util.Date, java.util.Map)
     * @see #copyWith(java.util.Calendar, java.util.Map)
     * @since 2.2.0
     */
    public static Date copyWith(Date self, Map<Object, Integer> updates) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        set(cal, updates);
        return cal.getTime();
    }

    private static final Map<String, Integer> CAL_MAP = new HashMap<String, Integer>();

    static {
        CAL_MAP.put("year", Calendar.YEAR);
        CAL_MAP.put("month", Calendar.MONTH);
        CAL_MAP.put("date", Calendar.DATE);
        CAL_MAP.put("dayOfMonth", Calendar.DATE);
        CAL_MAP.put("hourOfDay", Calendar.HOUR_OF_DAY);
        CAL_MAP.put("minute", Calendar.MINUTE);
        CAL_MAP.put("second", Calendar.SECOND);
    }

    /**
     * Increment a Date by one day.
     *
     * @param self a Date
     * @return the next days date
     * @since 1.0
     */
    public static Date next(Date self) {
        return plus(self, 1);
    }

    /**
     * Increment a Calendar by one day.
     *
     * @param self a Calendar
     * @return a new Calendar set to the next day
     * @since 1.8.7
     */
    public static Calendar next(Calendar self) {
        Calendar result = (Calendar) self.clone();
        result.add(Calendar.DATE, 1);
        return result;
    }

    /**
     * Decrement a Calendar by one day.
     *
     * @param self a Calendar
     * @return a new Calendar set to the previous day
     * @since 1.8.7
     */
    public static Calendar previous(Calendar self) {
        Calendar result = (Calendar) self.clone();
        result.add(Calendar.DATE, -1);
        return result;
    }

    /**
     * Increment a java.sql.Date by one day.
     *
     * @param self a java.sql.Date
     * @return the next days date
     * @since 1.0
     */
    public static java.sql.Date next(java.sql.Date self) {
        return new java.sql.Date(next((Date) self).getTime());
    }

    /**
     * Decrement a Date by one day.
     *
     * @param self a Date
     * @return the previous days date
     * @since 1.0
     */
    public static Date previous(Date self) {
        return minus(self, 1);
    }

    /**
     * Decrement a java.sql.Date by one day.
     *
     * @param self a java.sql.Date
     * @return the previous days date
     * @since 1.0
     */
    public static java.sql.Date previous(java.sql.Date self) {
        return new java.sql.Date(previous((Date) self).getTime());
    }

    /**
     * Add a number of days to this date and returns the new date.
     *
     * @param self a Date
     * @param days the number of days to increase
     * @return the new date
     * @since 1.0
     */
    public static Date plus(Date self, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    /**
     * Add a number of days to this date and returns the new date.
     *
     * @param self a java.sql.Date
     * @param days the number of days to increase
     * @return the new date
     * @since 1.0
     */
    public static java.sql.Date plus(java.sql.Date self, int days) {
        return new java.sql.Date(plus((Date) self, days).getTime());
    }

    /**
     * Add number of days to this Timestamp and returns the new Timestamp object.
     *
     * @param self a Timestamp
     * @param days the number of days to increase
     * @return the new Timestamp
     */
    public static Timestamp plus(Timestamp self, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        calendar.add(Calendar.DATE, days);
        Timestamp ts = new Timestamp(calendar.getTime().getTime());
        ts.setNanos(self.getNanos());
        return ts;
    }

    /**
     * Subtract a number of days from this date and returns the new date.
     *
     * @param self a Date
     * @param days the number of days to subtract
     * @return the new date
     * @since 1.0
     */
    public static Date minus(Date self, int days) {
        return plus(self, -days);
    }

    /**
     * Subtract a number of days from this date and returns the new date.
     *
     * @param self a java.sql.Date
     * @param days the number of days to subtract
     * @return the new date
     * @since 1.0
     */
    public static java.sql.Date minus(java.sql.Date self, int days) {
        return new java.sql.Date(minus((Date) self, days).getTime());
    }

    /**
     * Subtract a number of days from this Timestamp and returns the new Timestamp object.
     *
     * @param self a Timestamp
     * @param days the number of days to subtract
     * @return the new Timestamp
     */
    public static Timestamp minus(Timestamp self, int days) {
        return plus(self, -days);
    }

    /**
     * Subtract another date from this one and return the number of days of the difference.
     * <p>
     * Date self = Date then + (Date self - Date then)
     * <p>
     * IOW, if self is before then the result is a negative value.
     *
     * @param self a Calendar
     * @param then another Calendar
     * @return number of days
     * @since 1.6.0
     */
    public static int minus(Calendar self, Calendar then) {
        Calendar a = self;
        Calendar b = then;

        boolean swap = a.before(b);

        if (swap) {
            Calendar t = a;
            a = b;
            b = t;
        }

        int days = 0;

        b = (Calendar) b.clone();

        while (a.get(Calendar.YEAR) > b.get(Calendar.YEAR)) {
            days += 1 + (b.getActualMaximum(Calendar.DAY_OF_YEAR) - b.get(Calendar.DAY_OF_YEAR));
            b.set(Calendar.DAY_OF_YEAR, 1);
            b.add(Calendar.YEAR, 1);
        }

        days += a.get(Calendar.DAY_OF_YEAR) - b.get(Calendar.DAY_OF_YEAR);

        if (swap) days = -days;

        return days;
    }

    /**
     * Subtract another Date from this one and return the number of days of the difference.
     * <p>
     * Date self = Date then + (Date self - Date then)
     * <p>
     * IOW, if self is before then the result is a negative value.
     *
     * @param self a Date
     * @param then another Date
     * @return number of days
     * @since 1.6.0
     */
    public static int minus(Date self, Date then) {
        Calendar a = (Calendar) Calendar.getInstance().clone();
        a.setTime(self);
        Calendar b = (Calendar) Calendar.getInstance().clone();
        b.setTime(then);
        return minus(a, b);
    }

    /**
     * <p>Create a String representation of this date according to the given
     * format pattern.
     * <p>
     * <p>For example, if the system timezone is GMT,
     * <code>new Date(0).format('MM/dd/yy')</code> would return the string
     * <code>"01/01/70"</code>. See documentation for {@link java.text.SimpleDateFormat}
     * for format pattern use.
     * <p>
     * <p>Note that a new DateFormat instance is created for every
     * invocation of this method (for thread safety).
     *
     * @param self   a Date
     * @param format the format pattern to use according to {@link java.text.SimpleDateFormat}
     * @return a string representation of this date.
     * @see java.text.SimpleDateFormat
     * @since 1.5.7
     */
    public static String format(Date self, String format) {
        return new SimpleDateFormat(format).format(self);
    }

    /**
     * <p>Create a String representation of this date according to the given
     * format pattern and timezone.
     * <p>
     * <p>For example:
     * <code>
     * def d = new Date(0)
     * def tz = TimeZone.getTimeZone('GMT')
     * println d.format('dd/MMM/yyyy', tz)
     * </code> would return the string
     * <code>"01/Jan/1970"</code>. See documentation for {@link java.text.SimpleDateFormat}
     * for format pattern use.
     * <p>
     * <p>Note that a new DateFormat instance is created for every
     * invocation of this method (for thread safety).
     *
     * @param self   a Date
     * @param format the format pattern to use according to {@link java.text.SimpleDateFormat}
     * @param tz     the TimeZone to use
     * @return a string representation of this date.
     * @see java.text.SimpleDateFormat
     * @since 1.8.3
     */
    public static String format(Date self, String format, TimeZone tz) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(tz);
        return sdf.format(self);
    }

    /**
     * <p>Return a string representation of the 'day' portion of this date
     * according to the locale-specific {@link java.text.DateFormat#SHORT} default format.
     * For an "en_UK" system locale, this would be <code>dd/MM/yy</code>.
     * <p>
     * <p>Note that a new DateFormat instance is created for every
     * invocation of this method (for thread safety).
     *
     * @param self a Date
     * @return a string representation of this date
     * @see java.text.DateFormat#getDateInstance(int)
     * @see java.text.DateFormat#SHORT
     * @since 1.5.7
     */
    public static String getDateString(Date self) {
        return DateFormat.getDateInstance(DateFormat.SHORT).format(self);
    }

    /**
     * <p>Return a string representation of the time portion of this date
     * according to the locale-specific {@link java.text.DateFormat#MEDIUM} default format.
     * For an "en_UK" system locale, this would be <code>HH:MM:ss</code>.
     * <p>
     * <p>Note that a new DateFormat instance is created for every
     * invocation of this method (for thread safety).
     *
     * @param self a Date
     * @return a string representing the time portion of this date
     * @see java.text.DateFormat#getTimeInstance(int)
     * @see java.text.DateFormat#MEDIUM
     * @since 1.5.7
     */
    public static String getTimeString(Date self) {
        return DateFormat.getTimeInstance(DateFormat.MEDIUM).format(self);
    }

    /**
     * <p>Return a string representation of the date and time time portion of
     * this Date instance, according to the locale-specific format used by
     * {@link java.text.DateFormat}.  This method uses the {@link java.text.DateFormat#SHORT}
     * preset for the day portion and {@link java.text.DateFormat#MEDIUM} for the time
     * portion of the output string.
     * <p>
     * <p>Note that a new DateFormat instance is created for every
     * invocation of this method (for thread safety).
     *
     * @param self a Date
     * @return a string representation of this date and time
     * @see java.text.DateFormat#getDateTimeInstance(int, int)
     * @since 1.5.7
     */
    public static String getDateTimeString(Date self) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(self);
    }

    /**
     * Common code for {@link #clearTime(java.util.Calendar)} and {@link #clearTime(java.util.Date)}
     * and {@link #clearTime(java.sql.Date)}
     *
     * @param self a Calendar to adjust
     */
    private static void clearTimeCommon(final Calendar self) {
        self.set(Calendar.HOUR_OF_DAY, 0);
        self.clear(Calendar.MINUTE);
        self.clear(Calendar.SECOND);
        self.clear(Calendar.MILLISECOND);
    }

    /**
     * Clears the time portion of this Date instance; useful utility where
     * it makes sense to compare month/day/year only portions of a Date.
     *
     * @param self a Date
     * @return the Date but with the time portion cleared
     * @since 1.6.7
     */
    public static Date clearTime(final Date self) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        clearTimeCommon(calendar);
        self.setTime(calendar.getTime().getTime());
        return self;
    }

    /**
     * Clears the time portion of this java.sql.Date instance; useful utility
     * where it makes sense to compare month/day/year only portions of a Date.
     *
     * @param self a java.sql.Date
     * @return the java.sql.Date but with the time portion cleared
     * @since 1.6.7
     */
    public static java.sql.Date clearTime(final java.sql.Date self) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        clearTimeCommon(calendar);
        self.setTime(calendar.getTime().getTime());
        return self;
    }

    /**
     * Clears the time portion of this Calendar instance; useful utility
     * where it makes sense to compare month/day/year only portions of a Calendar.
     *
     * @param self a Calendar
     * @return the Calendar but with the time portion cleared
     * @since 1.6.7
     */
    public static Calendar clearTime(final Calendar self) {
        clearTimeCommon(self);
        return self;
    }

    /**
     * <p>Shortcut for {@link java.text.SimpleDateFormat} to output a String representation
     * of this calendar instance.  This method respects the Calendar's assigned
     * {@link java.util.TimeZone}, whereas calling <code>cal.time.format('HH:mm:ss')</code>
     * would use the system timezone.
     * <p>Note that Calendar equivalents of <code>date.getDateString()</code>
     * and variants do not exist because those methods are Locale-dependent.
     * Although a Calendar may be assigned a {@link java.util.Locale}, that information is
     * lost and therefore cannot be used to control the default date/time formats
     * provided by these methods.  Instead, the system Locale would always be
     * used.  The alternative is to simply call
     * {@link java.text.DateFormat#getDateInstance(int, java.util.Locale)} and pass the same Locale
     * that was used for the Calendar.
     *
     * @param self    this calendar
     * @param pattern format pattern
     * @return String representation of this calendar with the given format.
     * @see java.text.DateFormat#setTimeZone(java.util.TimeZone)
     * @see java.text.SimpleDateFormat#format(java.util.Date)
     * @see #format(java.util.Date, String)
     * @since 1.6.0
     */
    public static String format(Calendar self, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(self.getTimeZone());
        return sdf.format(self.getTime());
    }

    /**
     * Iterates from this date up to the given date, inclusive,
     * incrementing by one day each time.
     *
     * @param self    a Date
     * @param to      another Date to go up to
     * @param closure the closure to call
     * @since 2.2
     */
    public static void upto(Date self, Date to, Closure closure) {
        if (self.compareTo(to) <= 0) {
            for (Date i = (Date) self.clone(); i.compareTo(to) <= 0; i = next(i)) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be earlier than the value (" + self + ") it's called on.");
    }

    /**
     * Iterates from the date represented by this calendar up to the date represented
     * by the given calendar, inclusive, incrementing by one day each time.
     *
     * @param self    a Calendar
     * @param to      another Calendar to go up to
     * @param closure the closure to call
     * @since 2.2
     */
    public static void upto(Calendar self, Calendar to, Closure closure) {
        if (self.compareTo(to) <= 0) {
            for (Calendar i = (Calendar) self.clone(); i.compareTo(to) <= 0; i = next(i)) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be earlier than the value (" + self + ") it's called on.");
    }

    /**
     * Iterates from this date down to the given date, inclusive,
     * decrementing by one day each time.
     *
     * @param self    a Date
     * @param to      another Date to go down to
     * @param closure the closure to call
     * @since 2.2
     */
    public static void downto(Date self, Date to, Closure closure) {
        if (self.compareTo(to) >= 0) {
            for (Date i = (Date) self.clone(); i.compareTo(to) >= 0; i = previous(i)) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be later than the value (" + self + ") it's called on.");
    }

    /**
     * Iterates from the date represented by this calendar up to the date represented
     * by the given calendar, inclusive, incrementing by one day each time.
     *
     * @param self    a Calendar
     * @param to      another Calendar to go down to
     * @param closure the closure to call
     * @since 2.2
     */
    public static void downto(Calendar self, Calendar to, Closure closure) {
        if (self.compareTo(to) >= 0) {
            for (Calendar i = (Calendar) self.clone(); i.compareTo(to) >= 0; i = previous(i)) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be later than the value (" + self + ") it's called on.");
    }

    /**
     * Returns the Time Zone offset of the Calendar as a {@link java.time.ZoneOffset}.
     *
     * @param self a Calendar
     * @return a ZoneOffset
     * @since 3.0
     */
    public static ZoneOffset getZoneOffset(final Calendar self) {
        int offsetMillis = self.get(Calendar.ZONE_OFFSET) + self.get(Calendar.DST_OFFSET);
        return ZoneOffset.ofTotalSeconds(offsetMillis / 1000);
    }

    /**
     * Returns the Time Zone offset of the Date as a {@link java.time.ZoneOffset},
     * which will typically be system's default offset.
     *
     * @param self a Date
     * @return a ZoneOffset
     * @since 3.0
     */
    public static ZoneOffset getZoneOffset(final Date self) {
        return getZoneOffset(toCalendar(self));
    }

    /**
     * Returns the Time Zone of the Calendar as a java.time.ZoneId.
     *
     * @param self a Calendar
     * @return a ZoneId
     * @since 3.0
     */
    public static ZoneId getZoneId(final Calendar self) {
        return self.getTimeZone().toZoneId();
    }

    /**
     * Returns the Time Zone of the Date as a {@link java.time.ZoneId}. This will
     * typically be the system's default ZoneId.
     *
     * @param self a Date
     * @return a ZoneId
     * @since 3.0
     */
    public static ZoneId getZoneId(final Date self) {
        return getZoneId(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.Year}.  If the Calendar has a different
     * time zone than the system default, the Year will be adjusted into the default time zone.
     *
     * @param self a Calendar
     * @return a Year
     * @since 3.0
     */
    public static Year toYear(final Calendar self) {
        return Year.of(self.get(Calendar.YEAR));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.Year}.
     *
     * @param self a Date
     * @return a Year
     * @since 3.0
     */
    public static Year toYear(final Date self) {
        return toYear(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.Month}. If the Calendar has a different
     * time zone than the system default, the Month will be adjusted into the default time zone.
     *
     * @param self a Calendar
     * @return a Month
     * @since 3.0
     */
    public static Month toMonth(final Calendar self) {
        return Month.of(self.get(Calendar.MONTH) + 1);
    }

    /**
     * Converts the Date to a corresponding {@link java.time.Month}.
     *
     * @param self a Date
     * @return a Month
     * @since 3.0
     */
    public static Month toMonth(final Date self) {
        return toMonth(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.MonthDay}. If the Calendar has a different
     * time zone than the system default, the MonthDay will be adjusted into the default time zone.
     *
     * @param self a Calendar
     * @return a MonthDay
     * @since 3.0
     */
    public static MonthDay toMonthDay(final Calendar self) {
        return MonthDay.of(toMonth(self), self.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.MonthDay}.
     *
     * @param self a Date
     * @return a MonthDay
     * @since 3.0
     */
    public static MonthDay toMonthDay(final Date self) {
        return toMonthDay(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.YearMonth}. If the Calendar has a different
     * time zone than the system default, the YearMonth will be adjusted into the default time zone.
     *
     * @param self a Calendar
     * @return a YearMonth
     * @since 3.0
     */
    public static YearMonth toYearMonth(final Calendar self) {
        return toYear(self).atMonth(toMonth(self));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.YearMonth}.
     *
     * @param self a Date
     * @return a YearMonth
     * @since 3.0
     */
    public static YearMonth toYearMonth(final Date self) {
        return toYearMonth(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.DayOfWeek}. If the Calendar has a different
     * time zone than the system default, the DayOfWeek will be adjusted into the default time zone.
     *
     *
     * @param self a Calendar
     * @return a DayOfWeek
     * @since 3.0
     */
    public static DayOfWeek toDayOfWeek(final Calendar self) {
        return DayOfWeek.of(self.get(Calendar.DAY_OF_WEEK)).minus(1);
    }

    /**
     * Converts the Date to a corresponding {@link java.time.DayOfWeek}.
     *
     * @param self a Date
     * @return a DayOfWeek
     * @since 3.0
     */
    public static DayOfWeek toDayOfWeek(final Date self) {
        return toDayOfWeek(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.LocalDate}. If the Calendar has a different
     * time zone than the system default, the LocalDate will be adjusted into the default time zone.
     *
     * @param self a Calendar
     * @return a LocalDate
     * @since 3.0
     */
    static LocalDate toLocalDate(final Calendar self) {
        return LocalDate.of(self.get(Calendar.YEAR), toMonth(self), self.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.LocalDate}.
     *
     * @param self a Date
     * @return a LocalDate
     * @since 3.0
     */
    public static LocalDate toLocalDate(final Date self) {
        return toLocalDate(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.LocalTime}. If the Calendar has a different
     * time zone than the system default, the LocalTime will be adjusted into the default time zone.
     *
     * @param self a Calendar
     * @return a LocalTime
     * @since 3.0
     */
    public static LocalTime toLocalTime(final Calendar self) {
        int hour = self.get(Calendar.HOUR_OF_DAY);
        int minute = self.get(Calendar.MINUTE);
        int second = self.get(Calendar.SECOND);
        int ns = self.get(Calendar.MILLISECOND) * 1_000_000;
        return LocalTime.of(hour, minute, second, ns);
    }

    /**
     * Converts the Date to a corresponding {@link java.time.LocalTime}.
     *
     * @param self a Date
     * @return a LocalTime
     * @since 3.0
     */
    public static LocalTime toLocalTime(final Date self) {
        return toLocalTime(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.LocalDateTime}. If the Calendar has a different
     * time zone than the system default, the LocalDateTime will be adjusted into the default time zone.
     *
     * @param self a Calendar
     * @return a LocalDateTime
     * @since 3.0
     */
    public static LocalDateTime toLocalDateTime(final Calendar self) {
        return LocalDateTime.of(toLocalDate(self), toLocalTime(self));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.LocalDateTime}.
     *
     * @param self a Date
     * @return a LocalDateTime
     * @since 3.0
     */
    public static LocalDateTime toLocalDateTime(final Date self) {
        return toLocalDateTime(toCalendar(self));
    }

    /**
     * <p>Converts the Calendar to a corresponding {@link java.time.ZonedDateTime}.</p><p>Note that
     * {@link java.util.GregorianCalendar} has a {@link java.util.GregorianCalendar#toZonedDateTime} method,
     * which is commonly the specific type of Calendar in use.</p>
     *
     * @param self a Calendar
     * @return a ZonedDateTime
     * @since 3.0
     */
    public static ZonedDateTime toZonedDateTime(final Calendar self) {
        if (self instanceof GregorianCalendar) { // would this branch ever be true?
            return ((GregorianCalendar) self).toZonedDateTime();
        } else {
            return ZonedDateTime.of(toLocalDateTime(self), getZoneId(self));
        }
    }

    /**
     * Converts the Date to a corresponding {@link java.time.ZonedDateTime}.
     *
     * @param self a Date
     * @return a ZonedDateTime
     * @since 3.0
     */
    public static ZonedDateTime toZonedDateTime(final Date self) {
        return toZonedDateTime(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.OffsetDateTime}.
     *
     * @param self a Calendar
     * @return an OffsetDateTime
     * @since 3.0
     */
    public static OffsetDateTime toOffsetDateTime(final Calendar self) {
        return OffsetDateTime.of(toLocalDateTime(self), getZoneOffset(self));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.OffsetDateTime}.
     *
     * @param self a Date
     * @return an OffsetDateTime
     * @since 3.0
     */
    public static OffsetDateTime toOffsetDateTime(final Date self) {
        return toOffsetDateTime(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.OffsetTime}.
     *
     * @param self a Calendar
     * @return an OffsetTime
     * @since 3.0
     */
    public static OffsetTime toOffsetTime(final Calendar self) {
        return OffsetTime.of(toLocalTime(self), getZoneOffset(self));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.OffsetTime}.
     *
     * @param self a Date
     * @return an OffsetTime
     * @since 3.0
     */
    public static OffsetTime toOffsetTime(final Date self) {
        return toOffsetTime(toCalendar(self));
    }

    /**
     * Convenience method for converting a Calendar to a corresponding {@link java.time.Instant}.
     *
     * @param self a Calendar
     * @return an Instant
     * @since 3.0
     */
    public static Instant toInstant(final Calendar self) {
        return self.getTime().toInstant();
    }

    /**
     * Converts the TimeZone to a corresponding {@link java.time.ZoneOffset}. The offset is determined
     * using the current date/time.
     *
     * @param self a TimeZone
     * @return a ZoneOffset
     * @since 3.0
     */
    public static ZoneOffset toZoneOffset(final TimeZone self) {
        return toZoneOffset(self, Instant.now());
    }

    /**
     * Converts this TimeZone to a corresponding {@link java.time.ZoneOffset}. The offset is determined
     * using the date/time of specified Instant.
     *
     * @param self a TimeZone
     * @return a ZoneOffset
     * @since 3.0
     */
    public static ZoneOffset toZoneOffset(final TimeZone self, Instant instant) {
        return self.toZoneId().getRules().getOffset(instant);
    }
}
