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
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * @deprecated use DateUtilExtensions instead
 */
@Deprecated
public class DateGroovyMethods extends DefaultGroovyMethodsSupport {

    @Deprecated
    public static int getAt(Date self, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        return cal.get(field);
    }

    @Deprecated
    public static Calendar toCalendar(Date self) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        return cal;
    }

    @Deprecated
    public static int getAt(Calendar self, int field) {
        return self.get(field);
    }

    @Deprecated
    public static void putAt(Calendar self, int field, int value) {
        self.set(field, value);
    }

    @Deprecated
    public static void putAt(Date self, int field, int value) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        putAt(cal, field, value);
        self.setTime(cal.getTimeInMillis());
    }

    @Deprecated
    public static void set(Calendar self, Map<Object, Integer> updates) {
        for (Map.Entry<Object, Integer> entry : updates.entrySet()) {
            Object key = entry.getKey();
//            if (key instanceof String) key = CAL_MAP.get(key);
            if (key instanceof Integer) self.set((Integer) key, entry.getValue());
        }
    }

    @Deprecated
    public static Calendar updated(Calendar self, Map<Object, Integer> updates) {
        Calendar result = (Calendar) self.clone();
        set(result, updates);
        return result;
    }

    @Deprecated
    public static Calendar copyWith(Calendar self, Map<Object, Integer> updates) {
        Calendar result = (Calendar) self.clone();
        set(result, updates);
        return result;
    }

    @Deprecated
    public static void set(Date self, Map<Object, Integer> updates) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        set(cal, updates);
        self.setTime(cal.getTimeInMillis());
    }

    @Deprecated
    public static Date updated(Date self, Map<Object, Integer> updates) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        set(cal, updates);
        return cal.getTime();
    }

    @Deprecated
    public static Date copyWith(Date self, Map<Object, Integer> updates) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        set(cal, updates);
        return cal.getTime();
    }

    @Deprecated
    public static Date next(Date self) {
        return plus(self, 1);
    }

    @Deprecated
    public static Calendar next(Calendar self) {
        Calendar result = (Calendar) self.clone();
        result.add(Calendar.DATE, 1);
        return result;
    }

    @Deprecated
    public static Calendar previous(Calendar self) {
        Calendar result = (Calendar) self.clone();
        result.add(Calendar.DATE, -1);
        return result;
    }

    @Deprecated
    public static java.sql.Date next(java.sql.Date self) {
        return new java.sql.Date(next((Date) self).getTime());
    }

    @Deprecated
    public static Date previous(Date self) {
        return minus(self, 1);
    }

    @Deprecated
    public static java.sql.Date previous(java.sql.Date self) {
        return new java.sql.Date(previous((Date) self).getTime());
    }

    @Deprecated
    public static Date plus(Date self, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

    @Deprecated
    public static java.sql.Date plus(java.sql.Date self, int days) {
        return new java.sql.Date(plus((Date) self, days).getTime());
    }

    @Deprecated
    public static Timestamp plus(Timestamp self, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        calendar.add(Calendar.DATE, days);
        Timestamp ts = new Timestamp(calendar.getTime().getTime());
        ts.setNanos(self.getNanos());
        return ts;
    }

    @Deprecated
    public static Date minus(Date self, int days) {
        return plus(self, -days);
    }

    @Deprecated
    public static java.sql.Date minus(java.sql.Date self, int days) {
        return new java.sql.Date(minus((Date) self, days).getTime());
    }

    @Deprecated
    public static Timestamp minus(Timestamp self, int days) {
        return plus(self, -days);
    }

    @Deprecated
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

    @Deprecated
    public static int minus(Date self, Date then) {
        Calendar a = (Calendar) Calendar.getInstance().clone();
        a.setTime(self);
        Calendar b = (Calendar) Calendar.getInstance().clone();
        b.setTime(then);
        return minus(a, b);
    }

    @Deprecated
    public static String format(Date self, String format) {
        return new SimpleDateFormat(format).format(self);
    }

    @Deprecated
    public static String format(Date self, String format, TimeZone tz) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setTimeZone(tz);
        return sdf.format(self);
    }

    @Deprecated
    public static String getDateString(Date self) {
        return DateFormat.getDateInstance(DateFormat.SHORT).format(self);
    }

    @Deprecated
    public static String getTimeString(Date self) {
        return DateFormat.getTimeInstance(DateFormat.MEDIUM).format(self);
    }

    @Deprecated
    public static String getDateTimeString(Date self) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(self);
    }

    @Deprecated
    private static void clearTimeCommon(final Calendar self) {
        self.set(Calendar.HOUR_OF_DAY, 0);
        self.clear(Calendar.MINUTE);
        self.clear(Calendar.SECOND);
        self.clear(Calendar.MILLISECOND);
    }

    @Deprecated
    public static Date clearTime(final Date self) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        clearTimeCommon(calendar);
        self.setTime(calendar.getTime().getTime());
        return self;
    }

    @Deprecated
    public static java.sql.Date clearTime(final java.sql.Date self) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(self);
        clearTimeCommon(calendar);
        self.setTime(calendar.getTime().getTime());
        return self;
    }

    @Deprecated
    public static Calendar clearTime(final Calendar self) {
        clearTimeCommon(self);
        return self;
    }

    @Deprecated
    public static String format(Calendar self, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setTimeZone(self.getTimeZone());
        return sdf.format(self.getTime());
    }

    @Deprecated
    public static void upto(Date self, Date to, Closure closure) {
        if (self.compareTo(to) <= 0) {
            for (Date i = (Date) self.clone(); i.compareTo(to) <= 0; i = next(i)) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be earlier than the value (" + self + ") it's called on.");
    }

    @Deprecated
    public static void upto(Calendar self, Calendar to, Closure closure) {
        if (self.compareTo(to) <= 0) {
            for (Calendar i = (Calendar) self.clone(); i.compareTo(to) <= 0; i = next(i)) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be earlier than the value (" + self + ") it's called on.");
    }

    @Deprecated
    public static void downto(Date self, Date to, Closure closure) {
        if (self.compareTo(to) >= 0) {
            for (Date i = (Date) self.clone(); i.compareTo(to) >= 0; i = previous(i)) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be later than the value (" + self + ") it's called on.");
    }

    @Deprecated
    public static void downto(Calendar self, Calendar to, Closure closure) {
        if (self.compareTo(to) >= 0) {
            for (Calendar i = (Calendar) self.clone(); i.compareTo(to) >= 0; i = previous(i)) {
                closure.call(i);
            }
        } else
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be later than the value (" + self + ") it's called on.");
    }
}
