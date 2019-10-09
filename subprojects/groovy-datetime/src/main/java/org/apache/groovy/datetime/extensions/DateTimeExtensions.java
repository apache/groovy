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
package org.apache.groovy.datetime.extensions;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoPeriod;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.YEARS;

/**
 * This class defines new Groovy methods which appear on normal JDK
 * Date/Time API (java.time) classes inside the Groovy environment.
 * These extensions require JDK 8 or above.
 */
public class DateTimeExtensions {

    // Static methods only
    private DateTimeExtensions() {
    }

    private static final DateTimeFormatter ZONE_SHORT_FORMATTER = DateTimeFormatter.ofPattern("z");

    /**
     * For any Temporal subtype that does not use {@link java.time.temporal.ChronoUnit#SECONDS} as the unit for
     * the upto/downto methods, should have an entry.
     */
    private static Map<Class<? extends Temporal>, TemporalUnit> DEFAULT_UNITS = new HashMap<>();

    static {
        DEFAULT_UNITS.put(ChronoLocalDate.class, DAYS);
        DEFAULT_UNITS.put(YearMonth.class, MONTHS);
        DEFAULT_UNITS.put(Year.class, YEARS);
    }

    /**
     * A number of extension methods permit a long or int to be provided as a parameter. This method determines
     * what the unit should be for this number.
     */
    private static TemporalUnit defaultUnitFor(Temporal temporal) {
        return DEFAULT_UNITS.entrySet()
                .stream()
                .filter(e -> e.getKey().isAssignableFrom(temporal.getClass()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(ChronoUnit.SECONDS);
    }

    /**
     * Truncates a nanosecond value to milliseconds. No rounding.
     */
    private static int millisFromNanos(int nanos) {
        return nanos / 1_000_000;
    }

    /* ******** java.time.temporal.Temporal extension methods ******** */

    /**
     * Iterates from this to the {@code to} {@link java.time.temporal.Temporal}, inclusive, incrementing by one
     * unit each iteration, calling the closure once per iteration. The closure may accept a single
     * {@link java.time.temporal.Temporal} argument.
     * <p>
     * The particular unit incremented by depends on the specific sub-type of {@link java.time.temporal.Temporal}.
     * Most sub-types use a unit of {@link java.time.temporal.ChronoUnit#SECONDS} except for
     * <ul>
     * <li>{@link java.time.chrono.ChronoLocalDate} and its sub-types use {@link java.time.temporal.ChronoUnit#DAYS}.
     * <li>{@link java.time.YearMonth} uses {@link java.time.temporal.ChronoUnit#MONTHS}.
     * <li>{@link java.time.Year} uses {@link java.time.temporal.ChronoUnit#YEARS}.
     * </ul>
     *
     * @param from    the starting Temporal
     * @param to      the ending Temporal
     * @param closure the zero or one-argument closure to call
     * @throws GroovyRuntimeException if this value is later than {@code to}
     * @throws GroovyRuntimeException if {@code to} is a different type than this
     * @since 2.5.0
     */
    public static void upto(Temporal from, Temporal to, Closure closure) {
        upto(from, to, defaultUnitFor(from), closure);
    }

    /**
     * Iterates from this to the {@code to} {@link java.time.temporal.Temporal}, inclusive, incrementing by one
     * {@code unit} each iteration, calling the closure once per iteration. The closure may accept a single
     * {@link java.time.temporal.Temporal} argument.
     * <p>
     * If the unit is too large to iterate to the second Temporal exactly, such as iterating from two LocalDateTimes
     * that are seconds apart using {@link java.time.temporal.ChronoUnit#DAYS} as the unit, the iteration will cease
     * as soon as the current value of the iteration is later than the second Temporal argument. The closure will
     * not be called with any value later than the {@code to} value.
     *
     * @param from    the starting Temporal
     * @param to      the ending Temporal
     * @param unit    the TemporalUnit to increment by
     * @param closure the zero or one-argument closure to call
     * @throws GroovyRuntimeException if this value is later than {@code to}
     * @throws GroovyRuntimeException if {@code to} is a different type than this
     * @since 2.5.0
     */
    public static void upto(Temporal from, Temporal to, TemporalUnit unit, Closure closure) {
        if (isUptoEligible(from, to)) {
            for (Temporal i = from; isUptoEligible(i, to); i = i.plus(1, unit)) {
                closure.call(i);
            }
        } else {
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to upto() cannot be earlier than the value (" + from + ") it's called on.");
        }
    }

    /**
     * Returns true if the {@code from} can be iterated up to {@code to}.
     */
    private static boolean isUptoEligible(Temporal from, Temporal to) {
        TemporalAmount amount = rightShift(from, to);
        if (amount instanceof Period) {
            return isNonnegative((Period) amount);
        } else if (amount instanceof Duration) {
            return isNonnegative((Duration) amount);
        } else {
            throw new GroovyRuntimeException("Temporal implementations of "
                    + from.getClass().getCanonicalName() + " are not supported by upto().");
        }
    }

    /**
     * Iterates from this to the {@code to} {@link java.time.temporal.Temporal}, inclusive, decrementing by one
     * unit each iteration, calling the closure once per iteration. The closure may accept a single
     * {@link java.time.temporal.Temporal} argument.
     * <p>
     * The particular unit decremented by depends on the specific sub-type of {@link java.time.temporal.Temporal}.
     * Most sub-types use a unit of {@link java.time.temporal.ChronoUnit#SECONDS} except for
     * <ul>
     * <li>{@link java.time.chrono.ChronoLocalDate} and its sub-types use {@link java.time.temporal.ChronoUnit#DAYS}.
     * <li>{@link java.time.YearMonth} uses {@link java.time.temporal.ChronoUnit#MONTHS}.
     * <li>{@link java.time.Year} uses {@link java.time.temporal.ChronoUnit#YEARS}.
     * </ul>
     *
     * @param from    the starting Temporal
     * @param to      the ending Temporal
     * @param closure the zero or one-argument closure to call
     * @throws GroovyRuntimeException if this value is earlier than {@code to}
     * @throws GroovyRuntimeException if {@code to} is a different type than this
     * @since 2.5.0
     */
    public static void downto(Temporal from, Temporal to, Closure closure) {
        downto(from, to, defaultUnitFor(from), closure);
    }

    /**
     * Iterates from this to the {@code to} {@link java.time.temporal.Temporal}, inclusive, decrementing by one
     * {@code unit} each iteration, calling the closure once per iteration. The closure may accept a single
     * {@link java.time.temporal.Temporal} argument.
     * <p>
     * If the unit is too large to iterate to the second Temporal exactly, such as iterating from two LocalDateTimes
     * that are seconds apart using {@link java.time.temporal.ChronoUnit#DAYS} as the unit, the iteration will cease
     * as soon as the current value of the iteration is earlier than the second Temporal argument. The closure will
     * not be called with any value earlier than the {@code to} value.
     *
     * @param from    the starting Temporal
     * @param to      the ending Temporal
     * @param unit    the TemporalUnit to increment by
     * @param closure the zero or one-argument closure to call
     * @throws GroovyRuntimeException if this value is earlier than {@code to}
     * @throws GroovyRuntimeException if {@code to} is a different type than this
     * @since 2.5.0
     */
    public static void downto(Temporal from, Temporal to, TemporalUnit unit, Closure closure) {
        if (isDowntoEligible(from, to)) {
            for (Temporal i = from; isDowntoEligible(i, to); i = i.minus(1, unit)) {
                closure.call(i);
            }
        } else {
            throw new GroovyRuntimeException("The argument (" + to +
                    ") to downto() cannot be later than the value (" + from + ") it's called on.");
        }
    }

    /**
     * Returns true if the {@code from} can be iterated down to {@code to}.
     */
    private static boolean isDowntoEligible(Temporal from, Temporal to) {
        TemporalAmount amount = rightShift(from, to);
        if (amount instanceof Period) {
            return isNonpositive((Period) amount);
        } else if (amount instanceof Duration) {
            return isNonpositive((Duration) amount);
        } else {
            throw new GroovyRuntimeException("Temporal implementations of "
                    + from.getClass().getCanonicalName() + " are not supported by downto().");
        }
    }

    /**
     * Returns a {@link java.time.Duration} or {@link java.time.Period} between this (inclusive) and the {@code other}
     * {@link java.time.temporal.Temporal} (exclusive).
     * <p>
     * A Period will be returned for types {@link java.time.Year}, {@link java.time.YearMonth}, and
     * {@link java.time.chrono.ChronoLocalDate}; otherwise, a Duration will be returned.
     * <p>
     * Note: if the Temporal is a ChronoLocalDate but not a {@link java.time.LocalDate}, a general
     * {@link java.time.chrono.ChronoPeriod} will be returned as per the return type of the method
     * {@link java.time.chrono.ChronoLocalDate#until(ChronoLocalDate)} .
     *
     * @param self  a Temporal
     * @param other another Temporal of the same type
     * @return an TemporalAmount between the two Temporals
     * @since 2.5.0
     */
    public static TemporalAmount rightShift(final Temporal self, Temporal other) {
        if (!self.getClass().equals(other.getClass())) {
            throw new GroovyRuntimeException("Temporal arguments must be of the same type.");
        }
        switch ((ChronoUnit) defaultUnitFor(self)) {
            case YEARS:
                return DateTimeStaticExtensions.between(null, (Year) self, (Year) other);
            case MONTHS:
                return DateTimeStaticExtensions.between(null, (YearMonth) self, (YearMonth) other);
            case DAYS:
                return ChronoPeriod.between((ChronoLocalDate) self, (ChronoLocalDate) other);
            default:
                return Duration.between(self, other);
        }
    }

    /* ******** java.time.temporal.TemporalAccessor extension methods ******** */

    /**
     * Supports the getAt operator; equivalent to calling the
     * {@link java.time.temporal.TemporalAccessor#getLong(java.time.temporal.TemporalField)} method.
     *
     * @param self  a TemporalAccessor
     * @param field a non-null TemporalField
     * @return the value for the field
     * @throws DateTimeException                if a value for the field cannot be obtained
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException              if numeric overflow occurs
     * @since 2.5.0
     */
    public static long getAt(final TemporalAccessor self, TemporalField field) {
        return self.getLong(field);
    }

    /* ******** java.time.temporal.TemporalAmount extension methods ******** */

    /**
     * Supports the getAt operator; equivalent to calling the
     * {@link java.time.temporal.TemporalAmount#get(TemporalUnit)} method.
     *
     * @param self a TemporalAmount
     * @param unit a non-null TemporalUnit
     * @return the value for the field
     * @throws DateTimeException                if a value for the field cannot be obtained
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException              if numeric overflow occurs
     * @since 2.5.0
     */
    public static long getAt(final TemporalAmount self, TemporalUnit unit) {
        return self.get(unit);
    }

    /* ******** java.time.Duration extension methods ******** */

    /**
     * Returns a {@link java.time.Duration} that is {@code seconds} seconds longer than this duration.
     *
     * @param self    a Duration
     * @param seconds the number of seconds to add
     * @return a Duration
     * @since 2.5.0
     */
    public static Duration plus(final Duration self, long seconds) {
        return self.plusSeconds(seconds);
    }

    /**
     * Returns a {@link java.time.Duration} that is {@code seconds} seconds shorter that this duration.
     *
     * @param self    a Duration
     * @param seconds the number of seconds to subtract
     * @return a Duration
     * @since 2.5.0
     */
    public static Duration minus(final Duration self, long seconds) {
        return self.minusSeconds(seconds);
    }

    /**
     * Returns a {@link java.time.Duration} that is one second longer than this duration.
     *
     * @param self a Duration
     * @return a Duration
     * @since 2.5.0
     */
    public static Duration next(final Duration self) {
        return self.plusSeconds(1);
    }

    /**
     * Returns a {@link java.time.Duration} that is one second shorter than this duration.
     *
     * @param self a Duration
     * @return a Duration
     * @since 2.5.0
     */
    public static Duration previous(final Duration self) {
        return self.minusSeconds(1);
    }

    /**
     * Supports the unary minus operator; equivalent to calling the {@link Duration#negated()} method.
     *
     * @param self a Duration
     * @return a Duration
     * @since 2.5.0
     */
    public static Duration negative(final Duration self) {
        return self.negated();
    }

    /**
     * Supports the unary plus operator; equivalent to calling the {@link Duration#abs()} method.
     *
     * @param self a Duration
     * @return a Duration
     * @since 2.5.0
     */
    public static Duration positive(final Duration self) {
        return self.abs();
    }

    /**
     * Supports the multiplication operator; equivalent to calling the {@link Duration#multipliedBy(long)} method.
     *
     * @param self   a Duration
     * @param scalar the value to multiply by
     * @return a Duration
     * @since 2.5.0
     */
    public static Duration multiply(final Duration self, long scalar) {
        return self.multipliedBy(scalar);
    }

    /**
     * Supports the division operator; equivalent to calling the {@link Duration#dividedBy(long)} method.
     *
     * @param self   a Duration
     * @param scalar the value to divide by
     * @return a Duration
     * @since 2.5.0
     */
    public static Duration div(final Duration self, long scalar) {
        return self.dividedBy(scalar);
    }

    /**
     * Returns true if this duration is positive, excluding zero.
     *
     * @param self a Duration
     * @return true if positive
     * @since 2.5.0
     */
    public static boolean isPositive(final Duration self) {
        return !self.isZero() && !self.isNegative();
    }

    /**
     * Returns true if this duration is zero or positive.
     *
     * @param self a Duration
     * @return true if nonnegative
     * @since 2.5.0
     */
    public static boolean isNonnegative(final Duration self) {
        return self.isZero() || !self.isNegative();
    }

    /**
     * Returns true if this duration is zero or negative.
     *
     * @param self a Duration
     * @return true if nonpositive
     * @since 2.5.0
     */
    public static boolean isNonpositive(final Duration self) {
        return self.isZero() || self.isNegative();
    }

    /* ******** java.time.Instant extension methods ******** */

    /**
     * Returns an {@link java.time.Instant} that is {@code seconds} seconds after this instant.
     *
     * @param self    an Instant
     * @param seconds the number of seconds to add
     * @return an Instant
     * @since 2.5.0
     */
    public static Instant plus(final Instant self, long seconds) {
        return self.plusSeconds(seconds);
    }

    /**
     * Returns an {@link java.time.Instant} that is {@code seconds} seconds before this instant.
     *
     * @param self    an Instant
     * @param seconds the number of seconds to subtract
     * @return an Instant
     * @since 2.5.0
     */
    public static Instant minus(final Instant self, long seconds) {
        return self.minusSeconds(seconds);
    }

    /**
     * Returns an {@link java.time.Instant} that is one second after this instant.
     *
     * @param self an Instant
     * @return an Instant one second ahead
     * @since 2.5.0
     */
    public static Instant next(final Instant self) {
        return plus(self, 1);
    }

    /**
     * Returns an {@link java.time.Instant} that one second before this instant.
     *
     * @param self an Instant
     * @return an Instant one second behind
     * @since 2.5.0
     */
    public static Instant previous(final Instant self) {
        return minus(self, 1);
    }

    /**
     * Returns a generally equivalent {@link java.util.Date} according the number of milliseconds since the epoch,
     * adjusted into the system default time zone.
     *
     * @param self an Instant
     * @return a Date
     * @since 2.5.0
     */
    public static Date toDate(final Instant self) {
        return new Date(self.toEpochMilli());
    }

    /**
     * Returns a generally equivalent {@link java.util.Calendar} in the GMT time zone, truncated to milliseconds.
     *
     * @param self an Instant
     * @return a Calendar
     * @since 2.5.0
     */
    public static Calendar toCalendar(final Instant self) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(toDate(self));
        return cal;
    }

    /* ******** java.time.LocalDate extension methods ******** */

    /**
     * Formats this date with the provided {@link java.time.format.DateTimeFormatter} pattern.
     *
     * @param self    a LocalDate
     * @param pattern the formatting pattern
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final LocalDate self, String pattern) {
        return self.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats this date in the provided, localized {@link java.time.format.FormatStyle}.
     *
     * @param self      a LocalDate
     * @param dateStyle the FormatStyle
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final LocalDate self, FormatStyle dateStyle) {
        return self.format(DateTimeFormatter.ofLocalizedDate(dateStyle));
    }

    /**
     * Formats this date with the {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE} formatter.
     *
     * @param self a LocalDate
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getDateString(final LocalDate self) {
        return self.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Returns a {@link java.time.LocalDate} that is {@code days} days after this date.
     *
     * @param self a LocalDate
     * @param days the number of days to add
     * @return a LocalDate
     * @since 2.5.0
     */
    public static LocalDate plus(final LocalDate self, long days) {
        return self.plusDays(days);
    }

    /**
     * Returns a {@link java.time.LocalDate} that is {@code days} days before this date.
     *
     * @param self a LocalDate
     * @param days the number of days to subtract
     * @return a LocalDate
     * @since 2.5.0
     */
    public static LocalDate minus(final LocalDate self, long days) {
        return self.minusDays(days);
    }

    /**
     * Calculates the number of days between two dates
     *
     * @param self a LocalDate
     * @param other the other LocalDate
     * @return the number of days
     * @since 3.0.0
     */
    public static long minus(final LocalDate self, LocalDate other) {
        return ChronoUnit.DAYS.between(other, self);
    }

    /**
     * Returns a {@link java.time.LocalDate} one day after this date.
     *
     * @param self a LocalDate
     * @return the next day
     * @since 2.5.0
     */
    public static LocalDate next(final LocalDate self) {
        return plus(self, 1);
    }

    /**
     * Returns a {@link java.time.LocalDate} one day before this date.
     *
     * @param self a LocalDate
     * @return the previous day
     * @since 2.5.0
     */
    public static LocalDate previous(final LocalDate self) {
        return minus(self, 1);
    }

    /**
     * Returns a {@link java.time.Period} equivalent to the time between this date (inclusive)
     * and the provided {@link java.time.LocalDate} (exclusive).
     *
     * @param self  a LocalDate
     * @param other another LocalDate
     * @return a Period representing the time between the two LocalDates
     * @since 2.5.0
     */
    public static Period rightShift(final LocalDate self, LocalDate other) {
        return Period.between(self, other);
    }

    /**
     * Returns a {@link java.time.LocalDateTime} from this date and the provided {@link java.time.LocalTime}.
     *
     * @param self a LocalDate
     * @param time a LocalTime
     * @return a LocalDateTime
     * @since 2.5.0
     */
    public static LocalDateTime leftShift(final LocalDate self, LocalTime time) {
        return LocalDateTime.of(self, time);
    }

    /**
     * Returns a {@link java.time.OffsetDateTime} from this date and the provided {@link java.time.OffsetTime}.
     *
     * @param self a LocalDate
     * @param time an OffsetTime
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime leftShift(final LocalDate self, OffsetTime time) {
        return time.atDate(self);
    }

    /**
     * Returns an equivalent instance of {@link java.util.Date}.
     * The time portion of the returned date is cleared.
     *
     * @param self a LocalDate
     * @return a java.util.Date
     * @since 2.5.0
     */
    public static Date toDate(final LocalDate self) {
        return toCalendar(self).getTime();
    }

    /**
     * Returns an equivalent instance of {@link java.util.Calendar}.
     * The time portion of the returned calendar is cleared and the time zone is the current system default.
     *
     * @param self a LocalDate
     * @return a java.util.Calendar
     * @since 2.5.0
     */
    public static Calendar toCalendar(final LocalDate self) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, self.getDayOfMonth());
        cal.set(Calendar.MONTH, self.getMonthValue() - 1);
        cal.set(Calendar.YEAR, self.getYear());
        clearTimeCommon(cal);
        return cal;
    }

    /* duplicated with DateUtilExtensions utility method but we don't want the modules to depend on one another */
    private static void clearTimeCommon(final Calendar self) {
        self.set(Calendar.HOUR_OF_DAY, 0);
        self.clear(Calendar.MINUTE);
        self.clear(Calendar.SECOND);
        self.clear(Calendar.MILLISECOND);
    }

    /* ******** java.time.LocalDateTime extension methods ******** */

    /**
     * Formats this date/time with the provided {@link java.time.format.DateTimeFormatter} pattern.
     *
     * @param self    a LocalDateTime
     * @param pattern the formatting pattern
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final LocalDateTime self, String pattern) {
        return self.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats this date/time in the provided, localized {@link java.time.format.FormatStyle}.
     *
     * @param self          a LocalDateTime
     * @param dateTimeStyle the FormatStyle
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final LocalDateTime self, FormatStyle dateTimeStyle) {
        return self.format(DateTimeFormatter.ofLocalizedDateTime(dateTimeStyle));
    }

    /**
     * Formats this date/time with the {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE_TIME} formatter.
     *
     * @param self a LocalDateTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getDateTimeString(final LocalDateTime self) {
        return self.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * Formats this date/time with the {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE} formatter.
     *
     * @param self a LocalDateTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getDateString(final LocalDateTime self) {
        return self.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * Formats this date/time with the {@link java.time.format.DateTimeFormatter#ISO_LOCAL_TIME} formatter.
     *
     * @param self a LocalDateTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getTimeString(final LocalDateTime self) {
        return self.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    /**
     * Returns a {@link java.time.LocalDateTime} with the time portion cleared.
     *
     * @param self a LocalDateTime
     * @return a LocalDateTime
     * @since 2.5.0
     */
    public static LocalDateTime clearTime(final LocalDateTime self) {
        return self.truncatedTo(DAYS);
    }

    /**
     * Returns a {@link java.time.LocalDateTime} that is {@code seconds} seconds after this date/time.
     *
     * @param self    a LocalDateTime
     * @param seconds the number of seconds to add
     * @return a LocalDateTime
     * @since 2.5.0
     */
    public static LocalDateTime plus(final LocalDateTime self, long seconds) {
        return self.plusSeconds(seconds);
    }

    /**
     * Returns a {@link java.time.LocalDateTime} that is {@code seconds} seconds before this date/time.
     *
     * @param self    a LocalDateTime
     * @param seconds the number of seconds to subtract
     * @return a LocalDateTime
     * @since 2.5.0
     */
    public static LocalDateTime minus(final LocalDateTime self, long seconds) {
        return self.minusSeconds(seconds);
    }

    /**
     * Returns a {@link java.time.LocalDateTime} that is one second after this date/time.
     *
     * @param self a LocalDateTime
     * @return a LocalDateTime
     * @since 2.5.0
     */
    public static LocalDateTime next(final LocalDateTime self) {
        return plus(self, 1);
    }

    /**
     * Returns a {@link java.time.LocalDateTime} that is one second before this date/time.
     *
     * @param self a LocalDateTime
     * @return a LocalDateTime
     * @since 2.5.0
     */
    public static LocalDateTime previous(final LocalDateTime self) {
        return minus(self, 1);
    }

    /**
     * Returns an {@link java.time.OffsetDateTime} of this date/time and the provided {@link java.time.ZoneOffset}.
     *
     * @param self   a LocalDateTime
     * @param offset a ZoneOffset
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime leftShift(final LocalDateTime self, ZoneOffset offset) {
        return OffsetDateTime.of(self, offset);
    }

    /**
     * Returns a {@link java.time.OffsetDateTime} of this date/time and the provided {@link java.time.ZoneId}.
     *
     * @param self a LocalDateTime
     * @param zone a ZoneId
     * @return a ZonedDateTime
     * @since 2.5.0
     */
    public static ZonedDateTime leftShift(final LocalDateTime self, ZoneId zone) {
        return ZonedDateTime.of(self, zone);
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Date}.
     * The time value of the returned date is truncated to milliseconds.
     *
     * @param self a LocalDateTime
     * @return a java.util.Date
     * @since 2.5.0
     */
    public static Date toDate(final LocalDateTime self) {
        return toCalendar(self).getTime();
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Calendar}.
     * The time value of the returned calendar is truncated to milliseconds and the
     * time zone is the current system default.
     *
     * @param self a LocalDateTime
     * @return a java.util.Calendar
     * @since 2.5.0
     */
    public static Calendar toCalendar(final LocalDateTime self) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DATE, self.getDayOfMonth());
        cal.set(Calendar.MONTH, self.getMonthValue() - 1);
        cal.set(Calendar.YEAR, self.getYear());
        cal.set(Calendar.HOUR_OF_DAY, self.getHour());
        cal.set(Calendar.MINUTE, self.getMinute());
        cal.set(Calendar.SECOND, self.getSecond());
        cal.set(Calendar.MILLISECOND, millisFromNanos(self.getNano()));
        return cal;
    }

    /* ******** java.time.LocalTime extension methods ******** */

    /**
     * Formats this time with the provided {@link java.time.format.DateTimeFormatter} pattern.
     *
     * @param self    a LocalDateTime
     * @param pattern the formatting pattern
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final LocalTime self, String pattern) {
        return self.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats this time in the provided, localized {@link java.time.format.FormatStyle}.
     *
     * @param self      a LocalTime
     * @param timeStyle the FormatStyle
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final LocalTime self, FormatStyle timeStyle) {
        return self.format(DateTimeFormatter.ofLocalizedTime(timeStyle));
    }

    /**
     * Formats this time with the {@link java.time.format.DateTimeFormatter#ISO_LOCAL_TIME} formatter.
     *
     * @param self a LocalTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getTimeString(final LocalTime self) {
        return self.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    /**
     * Returns a {@link java.time.LocalTime} that is {@code seconds} seconds after this time.
     *
     * @param self    a LocalTime
     * @param seconds the number of seconds to add
     * @return a LocalTime
     * @since 2.5.0
     */
    public static LocalTime plus(final LocalTime self, long seconds) {
        return self.plusSeconds(seconds);
    }

    /**
     * Returns a {@link java.time.LocalTime} that is {@code seconds} seconds before this time.
     *
     * @param self    a LocalTime
     * @param seconds the number of seconds to subtract
     * @return a LocalTime
     * @since 2.5.0
     */
    public static LocalTime minus(final LocalTime self, long seconds) {
        return self.minusSeconds(seconds);
    }

    /**
     * Returns a {@link java.time.LocalTime} that is one second after this time.
     *
     * @param self a LocalTime
     * @return a LocalTime
     * @since 2.5.0
     */
    public static LocalTime next(final LocalTime self) {
        return plus(self, 1);
    }

    /**
     * Returns a {@link java.time.LocalTime} that is one second before this time.
     *
     * @param self a LocalTime
     * @return a LocalTime
     * @since 2.5.0
     */
    public static LocalTime previous(final LocalTime self) {
        return minus(self, 1);
    }

    /**
     * Returns a {@link java.time.LocalDateTime} of this time and the provided {@link java.time.LocalDate}.
     *
     * @param self a LocalTime
     * @param date a LocalDate
     * @return a LocalDateTime
     * @since 2.5.0
     */
    public static LocalDateTime leftShift(final LocalTime self, LocalDate date) {
        return LocalDateTime.of(date, self);
    }

    /**
     * Returns an {@link java.time.OffsetTime} of this time and the provided {@link java.time.ZoneOffset}.
     *
     * @param self   a LocalTime
     * @param offset a ZoneOffset
     * @return an OffsetTime
     * @since 2.5.0
     */
    public static OffsetTime leftShift(final LocalTime self, ZoneOffset offset) {
        return OffsetTime.of(self, offset);
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Date}. The day-month-year value of the
     * returned date is today and the time is truncated to milliseconds.
     *
     * @param self a LocalTime
     * @return a java.util.Date
     * @since 2.5.0
     */
    public static Date toDate(final LocalTime self) {
        return toCalendar(self).getTime();
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Calendar}. The day-month-year value of the
     * returned calendar is today, the time is truncated to milliseconds, and the time zone is the current
     * system default.
     *
     * @param self a LocalTime
     * @return a java.util.Calendar
     * @since 2.5.0
     */
    public static Calendar toCalendar(final LocalTime self) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, self.getHour());
        cal.set(Calendar.MINUTE, self.getMinute());
        cal.set(Calendar.SECOND, self.getSecond());
        cal.set(Calendar.MILLISECOND, millisFromNanos(self.getNano()));
        return cal;
    }

    /* ******** java.time.MonthDay extension methods ******** */

    /**
     * Returns a {@link java.time.LocalDate} of this month/day and the provided year.
     *
     * @param self a MonthDay
     * @param year a year
     * @return a LocalDate
     * @since 2.5.0
     */
    public static LocalDate leftShift(final MonthDay self, int year) {
        return self.atYear(year);
    }

    /**
     * Returns a {@link java.time.LocalDate} of this month/day and the provided {@link java.time.Year}.
     *
     * @param self a MonthDay
     * @param year a Year
     * @return a LocalDate
     * @since 2.5.0
     */
    public static LocalDate leftShift(final MonthDay self, Year year) {
        return year.atMonthDay(self);
    }

    /* ******** java.time.OffsetDateTime extension methods ******** */

    /**
     * Formats this date/time with the provided {@link java.time.format.DateTimeFormatter} pattern.
     *
     * @param self    an OffsetDateTime
     * @param pattern the formatting pattern
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final OffsetDateTime self, String pattern) {
        return self.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats this date/time in the provided, localized {@link java.time.format.FormatStyle}.
     *
     * @param self          an OffsetDateTime
     * @param dateTimeStyle the FormatStyle
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final OffsetDateTime self, FormatStyle dateTimeStyle) {
        return self.format(DateTimeFormatter.ofLocalizedDateTime(dateTimeStyle));
    }

    /**
     * Formats this date/time with the {@link java.time.format.DateTimeFormatter#ISO_OFFSET_DATE_TIME} formatter.
     *
     * @param self an OffsetDateTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getDateTimeString(final OffsetDateTime self) {
        return self.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Formats this date/time with the {@link java.time.format.DateTimeFormatter#ISO_OFFSET_DATE} formatter.
     *
     * @param self an OffsetDateTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getDateString(final OffsetDateTime self) {
        return self.format(DateTimeFormatter.ISO_OFFSET_DATE);
    }

    /**
     * Formats this date/time with the {@link java.time.format.DateTimeFormatter#ISO_OFFSET_TIME} formatter.
     *
     * @param self an OffsetDateTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getTimeString(final OffsetDateTime self) {
        return self.format(DateTimeFormatter.ISO_OFFSET_TIME);
    }

    /**
     * Returns an {@link java.time.OffsetDateTime} with the time portion cleared.
     *
     * @param self an OffsetDateTime
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime clearTime(final OffsetDateTime self) {
        return self.truncatedTo(DAYS);
    }

    /**
     * Returns an {@link java.time.OffsetDateTime} that is {@code seconds} seconds after this date/time.
     *
     * @param self    an OffsetDateTime
     * @param seconds the number of seconds to add
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime plus(final OffsetDateTime self, long seconds) {
        return self.plusSeconds(seconds);
    }

    /**
     * Returns an {@link java.time.OffsetDateTime} that is {@code seconds} seconds before this date/time.
     *
     * @param self    an OffsetDateTime
     * @param seconds the number of seconds to subtract
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime minus(final OffsetDateTime self, long seconds) {
        return self.minusSeconds(seconds);
    }

    /**
     * Returns an {@link java.time.OffsetDateTime} one second after this date/time.
     *
     * @param self an OffsetDateTime
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime next(final OffsetDateTime self) {
        return plus(self, 1);
    }

    /**
     * Returns an {@link java.time.OffsetDateTime} one second before this date/time.
     *
     * @param self an OffsetDateTime
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime previous(final OffsetDateTime self) {
        return minus(self, 1);
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Date}.
     * The time value of the returned date is truncated to milliseconds and will be
     * adjusted to the current system default time zone.
     *
     * @param self an OffsetDateTime
     * @return a java.util.Date
     * @since 2.5.0
     */
    public static Date toDate(final OffsetDateTime self) {
        return toCalendar(self).getTime();
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Calendar}.
     * The time value of the returned calendar is truncated to milliseconds and the time zone
     * is based on the offset of this date/time.
     *
     * @param self an OffsetDateTime
     * @return a java.util.Calendar
     * @since 2.5.0
     */
    public static Calendar toCalendar(final OffsetDateTime self) {
        return toCalendar(self.toZonedDateTime());
    }

    /* ******** java.time.OffsetTime extension methods ******** */

    /**
     * Formats this time with the provided {@link java.time.format.DateTimeFormatter} pattern.
     *
     * @param self    an OffsetTime
     * @param pattern the formatting pattern
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final OffsetTime self, String pattern) {
        return self.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats this time in the provided, localized {@link java.time.format.FormatStyle}.
     *
     * @param self      an OffsetTime
     * @param timeStyle the FormatStyle
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final OffsetTime self, FormatStyle timeStyle) {
        return self.format(DateTimeFormatter.ofLocalizedTime(timeStyle));
    }

    /**
     * Formats this time with the {@link java.time.format.DateTimeFormatter#ISO_OFFSET_TIME} formatter.
     *
     * @param self an OffsetTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getTimeString(final OffsetTime self) {
        return self.format(DateTimeFormatter.ISO_OFFSET_TIME);
    }

    /**
     * Returns an {@link java.time.OffsetTime} that is {@code seconds} seconds after this time.
     *
     * @param self    an OffsetTime
     * @param seconds the number of seconds to add
     * @return an OffsetTime
     * @since 2.5.0
     */
    public static OffsetTime plus(final OffsetTime self, long seconds) {
        return self.plusSeconds(seconds);
    }

    /**
     * Returns an {@link java.time.OffsetTime} that is {@code seconds} seconds before this time.
     *
     * @param self    an OffsetTime
     * @param seconds the number of seconds to subtract
     * @return an OffsetTime
     * @since 2.5.0
     */
    public static OffsetTime minus(final OffsetTime self, long seconds) {
        return self.minusSeconds(seconds);
    }

    /**
     * Returns an {@link java.time.OffsetTime} that is one second after this time.
     *
     * @param self an OffsetTime
     * @return an OffsetTime
     * @since 2.5.0
     */
    public static OffsetTime next(final OffsetTime self) {
        return plus(self, 1);
    }

    /**
     * Returns an {@link java.time.OffsetTime} that is one second before this time.
     *
     * @param self an OffsetTime
     * @return an OffsetTime
     * @since 2.5.0
     */
    public static OffsetTime previous(final OffsetTime self) {
        return minus(self, 1);
    }

    /**
     * Returns an {@link java.time.OffsetDateTime} of this time and the provided {@link java.time.LocalDate}.
     *
     * @param self an OffsetTime
     * @param date a LocalDate
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime leftShift(final OffsetTime self, LocalDate date) {
        return OffsetDateTime.of(date, self.toLocalTime(), self.getOffset());
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Date}.
     * The time value of the returned date is truncated to milliseconds and will be
     * adjusted to the current system default time zone.
     *
     * @param self an OffsetTime
     * @return a java.util.Date
     * @since 2.5.0
     */
    public static Date toDate(final OffsetTime self) {
        return toCalendar(self).getTime();
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Calendar}.
     * The date value of the returned calendar is now, the time value is truncated to milliseconds,
     * and the time zone is based on the offset of this time.
     *
     * @param self an OffsetTime
     * @return a java.util.Calendar
     * @since 2.5.0
     */
    public static Calendar toCalendar(final OffsetTime self) {
        TimeZone timeZone = toTimeZone(self.getOffset());
        Calendar cal = Calendar.getInstance(timeZone);
        cal.set(Calendar.HOUR_OF_DAY, self.getHour());
        cal.set(Calendar.MINUTE, self.getMinute());
        cal.set(Calendar.SECOND, self.getSecond());
        cal.set(Calendar.MILLISECOND, millisFromNanos(self.getNano()));
        return cal;
    }

    /* ******** java.time.Period extension methods ******** */

    /**
     * Returns a {@link java.time.Period} that is {@code days} days longer than this period.
     * No normalization is performed.
     *
     * @param self a Period
     * @param days the number of days to increase this Period by
     * @return a Period
     * @since 2.5.0
     */
    public static Period plus(final Period self, long days) {
        return self.plusDays(days);
    }

    /**
     * Returns a {@link java.time.Period} that is {@code days} days shorter than this period.
     * No normalization is performed.
     *
     * @param self a Period
     * @param days the number of days to decrease this Period by
     * @return a Period
     * @since 2.5.0
     */
    public static Period minus(final Period self, long days) {
        return self.minusDays(days);
    }

    /**
     * Returns a {@link java.time.Period} that is one day longer than this period.
     * No normalization is performed.
     *
     * @param self a Period
     * @return a Period one day longer in length
     * @since 2.5.0
     */
    public static Period next(final Period self) {
        return plus(self, 1);
    }

    /**
     * Returns a {@link java.time.Period} that is one day shorter than this period.
     * No normalization is performed.
     *
     * @param self a Period
     * @return a Period one day shorter in length
     * @since 2.5.0
     */
    public static Period previous(final Period self) {
        return minus(self, 1);
    }

    /**
     * Supports the unary minus operator; equivalent to calling the {@link java.time.Period#negated()} method.
     *
     * @param self a Period
     * @return a negated Period
     * @since 2.5.0
     */
    public static Period negative(final Period self) {
        return self.negated();
    }

    /**
     * Supports the unary plus operator; returns a {@link java.time.Period} with all unit values positive.
     * For example, a period of "2 years, -3 months, and -4 days" would result in a period of
     * "2 years, 3 months, and 4 days." No normalization is performed.
     *
     * @param self a Period
     * @return a positive Period
     * @since 2.5.0
     */
    public static Period positive(final Period self) {
        return !self.isNegative() ? self : self.withDays(Math.abs(self.getDays()))
                .withMonths(Math.abs(self.getMonths()))
                .withYears(Math.abs(self.getYears()));
    }

    /**
     * Supports the multiply operator; equivalent to calling the {@link java.time.Period#multipliedBy(int)} method.
     *
     * @param self   a Period
     * @param scalar a scalar to multiply each unit by
     * @return a Period
     * @since 2.5.0
     */
    public static Period multiply(final Period self, int scalar) {
        return self.multipliedBy(scalar);
    }

    /**
     * Returns true if this period is positive, excluding zero.
     *
     * @param self a ChronoPeriod
     * @return true if positive
     * @since 2.5.0
     */
    public static boolean isPositive(final ChronoPeriod self) {
        return !self.isZero() && !self.isNegative();
    }

    /**
     * Returns true if this period is zero or positive.
     *
     * @param self a ChronoPeriod
     * @return true if nonnegative
     * @since 2.5.0
     */
    public static boolean isNonnegative(final ChronoPeriod self) {
        return self.isZero() || !self.isNegative();
    }

    /**
     * Returns true if this period is zero or negative.
     *
     * @param self a ChronoPeriod
     * @return true if nonpositive
     * @since 2.5.0
     */
    public static boolean isNonpositive(final ChronoPeriod self) {
        return self.isZero() || self.isNegative();
    }

    /* ******** java.time.Year extension methods ******** */

    /**
     * Returns a {@link java.time.Year} that is {@code years} years after this year.
     *
     * @param self  a Year
     * @param years the number of years to add
     * @return a Year
     * @since 2.5.0
     */
    public static Year plus(final Year self, long years) {
        return self.plusYears(years);
    }

    /**
     * Returns a {@link java.time.Year} that is {@code years} years before this year.
     *
     * @param self  a Year
     * @param years the number of years to subtract
     * @return a Year
     * @since 2.5.0
     */
    public static Year minus(final Year self, long years) {
        return self.minusYears(years);
    }

    /**
     * Returns a {@link java.time.Year} after this year.
     *
     * @param self a Year
     * @return the next Year
     * @since 2.5.0
     */
    public static Year next(final Year self) {
        return plus(self, 1);
    }

    /**
     * Returns a {@link java.time.Year} before this year.
     *
     * @param self a Year
     * @return the previous Year
     * @since 2.5.0
     */
    public static Year previous(final Year self) {
        return minus(self, 1);
    }

    /**
     * Returns a {@link java.time.Period} between the first day of this year (inclusive) and the first day of the
     * provided {@link java.time.Year} (exclusive).
     *
     * @param self a Year
     * @param year another Year
     * @return a Period between the Years
     * @since 2.5.0
     */
    public static Period rightShift(final Year self, Year year) {
        return Period.between(self.atDay(1), year.atDay(1));
    }

    /**
     * Returns a {@link java.time.YearMonth} of this year and the provided {@link java.time.Month}.
     *
     * @param self  a Year
     * @param month a Month
     * @return a YearMonth
     * @since 2.5.0
     */
    public static YearMonth leftShift(final Year self, Month month) {
        return self.atMonth(month);
    }

    /**
     * Returns a {@link java.time.LocalDate} of this year on the given {@link java.time.MonthDay}.
     *
     * @param self     a Year
     * @param monthDay a MonthDay
     * @return a LocalDate
     * @since 2.5.0
     */
    public static LocalDate leftShift(final Year self, MonthDay monthDay) {
        return self.atMonthDay(monthDay);
    }

    /**
     * Equivalent to calling the {@link java.time.Year#get(java.time.temporal.TemporalField)} method with a
     * {@link java.time.temporal.ChronoField#ERA} argument.
     * <p>
     * Returns the era of the year, which is currently either 0 (BC) or 1 (AD).
     *
     * @param self a Year
     * @return an int representing the era
     * @since 2.5.0
     */
    public static int getEra(final Year self) {
        return self.get(ChronoField.ERA);
    }

    /**
     * Equivalent to calling the {@link java.time.Year#get(java.time.temporal.TemporalField)} method with a
     * {@link java.time.temporal.ChronoField#YEAR_OF_ERA} argument.
     * <p>
     * Since Year=0 represents 1 BC, the yearOfEra value of Year=0 is 1, Year=-1 is 2, and so on.
     *
     * @param self a Year
     * @return the year value of the era
     * @since 2.5.0
     */
    public static int getYearOfEra(final Year self) {
        return self.get(ChronoField.YEAR_OF_ERA);
    }

    /* ******** java.time.YearMonth extension methods ******** */

    /**
     * Returns a {@link java.time.YearMonth} that is {@code months} months after this year/month.
     *
     * @param self   a YearMonth
     * @param months the number of months to add
     * @return a Year
     * @since 2.5.0
     */
    public static YearMonth plus(final YearMonth self, long months) {
        return self.plusMonths(months);
    }

    /**
     * Returns a {@link java.time.YearMonth} that is {@code months} months before this year/month.
     *
     * @param self   a YearMonth
     * @param months the number of months to subtract
     * @return a Year
     * @since 2.5.0
     */
    public static YearMonth minus(final YearMonth self, long months) {
        return self.minusMonths(months);
    }

    /**
     * Returns a {@link java.time.YearMonth} that is the month after this year/month.
     *
     * @param self a YearMonth
     * @return the next YearMonth
     * @since 2.5.0
     */
    public static YearMonth next(final YearMonth self) {
        return plus(self, 1);
    }

    /**
     * Returns a {@link java.time.YearMonth} that is the month before this year/month.
     *
     * @param self a YearMonth
     * @return the previous YearMonth
     * @since 2.5.0
     */
    public static YearMonth previous(final YearMonth self) {
        return minus(self, 1);
    }

    /**
     * Returns a {@link java.time.LocalDate} of this year/month and the given day of the month.
     *
     * @param self       a YearMonth
     * @param dayOfMonth a day of the month
     * @return a LocalDate
     * @since 2.5.0
     */
    public static LocalDate leftShift(final YearMonth self, int dayOfMonth) {
        return self.atDay(dayOfMonth);
    }

    /**
     * Returns a {@link java.time.Period} of time between the first day of this year/month (inclusive) and the
     * given {@link java.time.YearMonth} (exclusive).
     *
     * @param self  a YearMonth
     * @param other another YearMonth
     * @return a Period
     * @since 2.5.0
     */
    public static Period rightShift(YearMonth self, YearMonth other) {
        return Period.between(self.atDay(1), other.atDay(1));
    }

    /* ******** java.time.ZonedDateTime extension methods ******** */

    /**
     * Formats this date/time with the provided {@link java.time.format.DateTimeFormatter} pattern.
     *
     * @param self    a ZonedDateTime
     * @param pattern the formatting pattern
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final ZonedDateTime self, String pattern) {
        return self.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats this date/time in the provided, localized {@link java.time.format.FormatStyle}.
     *
     * @param self          a ZonedDateTime
     * @param dateTimeStyle the FormatStyle
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String format(final ZonedDateTime self, FormatStyle dateTimeStyle) {
        return self.format(DateTimeFormatter.ofLocalizedDateTime(dateTimeStyle));
    }

    /**
     * Formats this date/time with the {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE_TIME} formatter
     * and appends the zone's short name, e.g. {@code 2018-03-10T14:34:55.144EST}.
     *
     * @param self a ZonedDateTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getDateTimeString(final ZonedDateTime self) {
        return self.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + self.format(ZONE_SHORT_FORMATTER);
    }

    /**
     * Formats this date/time with the {@link java.time.format.DateTimeFormatter#ISO_LOCAL_DATE} formatter
     * and appends the zone's short name, e.g. {@code 2018-03-10EST}.
     *
     * @param self a ZonedDateTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getDateString(final ZonedDateTime self) {
        return self.format(DateTimeFormatter.ISO_LOCAL_DATE) + self.format(ZONE_SHORT_FORMATTER);
    }

    /**
     * Formats this date/time with the {@link java.time.format.DateTimeFormatter#ISO_LOCAL_TIME} formatter
     * and appends the zone's short name, e.g. {@code 14:34:55.144EST}.
     *
     * @param self a ZonedDateTime
     * @return a formatted String
     * @see java.time.format.DateTimeFormatter
     * @since 2.5.0
     */
    public static String getTimeString(final ZonedDateTime self) {
        return self.format(DateTimeFormatter.ISO_LOCAL_TIME) + self.format(ZONE_SHORT_FORMATTER);
    }

    /**
     * Returns an {@link java.time.ZonedDateTime} with the time portion cleared.
     *
     * @param self a ZonedDateTime
     * @return a ZonedDateTime
     * @since 2.5.0
     */
    public static ZonedDateTime clearTime(final ZonedDateTime self) {
        return self.truncatedTo(DAYS);
    }

    /**
     * Returns a {@link java.time.ZonedDateTime} that is {@code seconds} seconds after this date/time.
     *
     * @param self    an ZonedDateTime
     * @param seconds the number of seconds to add
     * @return a ZonedDateTime
     * @since 2.5.0
     */
    public static ZonedDateTime plus(final ZonedDateTime self, long seconds) {
        return self.plusSeconds(seconds);
    }

    /**
     * Returns a {@link java.time.ZonedDateTime} that is {@code seconds} seconds before this date/time.
     *
     * @param self    a ZonedDateTime
     * @param seconds the number of seconds to subtract
     * @return a ZonedDateTime
     * @since 2.5.0
     */
    public static ZonedDateTime minus(final ZonedDateTime self, long seconds) {
        return self.minusSeconds(seconds);
    }

    /**
     * Returns a {@link java.time.ZonedDateTime} that is one second after this date/time.
     *
     * @param self a ZonedDateTime
     * @return a ZonedDateTime
     * @since 2.5.0
     */
    public static ZonedDateTime next(final ZonedDateTime self) {
        return plus(self, 1);
    }

    /**
     * Returns a {@link java.time.ZonedDateTime} that is one second before this date/time.
     *
     * @param self a ZonedDateTime
     * @return a ZonedDateTime
     * @since 2.5.0
     */
    public static ZonedDateTime previous(final ZonedDateTime self) {
        return minus(self, 1);
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Date}.
     * The time value of the returned date is truncated to milliseconds and will be
     * adjusted to the current system default time zone.
     *
     * @param self a ZonedDateTime
     * @return a java.util.Date
     * @since 2.5.0
     */
    public static Date toDate(final ZonedDateTime self) {
        return toCalendar(self).getTime();
    }

    /**
     * Returns a generally equivalent instance of {@link java.util.Calendar}.
     * The time value of the returned calendar is truncated to milliseconds and the time zone
     * is determined by the zone of this date/time.
     *
     * @param self an ZonedDateTime
     * @return a java.util.Calendar
     * @since 2.5.0
     */
    public static Calendar toCalendar(final ZonedDateTime self) {
        Calendar cal = Calendar.getInstance(toTimeZone(self.getZone()));
        cal.set(Calendar.DATE, self.getDayOfMonth());
        cal.set(Calendar.MONTH, self.getMonthValue() - 1);
        cal.set(Calendar.YEAR, self.getYear());
        cal.set(Calendar.HOUR_OF_DAY, self.getHour());
        cal.set(Calendar.MINUTE, self.getMinute());
        cal.set(Calendar.SECOND, self.getSecond());
        cal.set(Calendar.MILLISECOND, millisFromNanos(self.getNano()));
        return cal;
    }

    /* ******** java.time.ZoneId extension methods ******** */

    /**
     * Returns a {@link java.util.TimeZone} equivalent to this zone.
     *
     * @param self a ZoneId
     * @return a TimeZone
     * @since 2.5.0
     */
    public static TimeZone toTimeZone(final ZoneId self) {
        return TimeZone.getTimeZone(self);
    }

    /**
     * Returns the name of this zone formatted according to the {@link java.time.format.TextStyle#FULL} text style.
     *
     * @param self a ZoneId
     * @return the full display name of the ZoneId
     * @since 2.5.0
     */
    public static String getFullName(final ZoneId self) {
        return getFullName(self, Locale.getDefault());
    }

    /**
     * Returns the name of this zone formatted according to the {@link java.time.format.TextStyle#FULL} text style
     * for the provided {@link java.util.Locale}.
     *
     * @param self   a ZoneId
     * @param locale a Locale
     * @return the full display name of the ZoneId
     * @since 2.5.0
     */
    public static String getFullName(final ZoneId self, Locale locale) {
        return self.getDisplayName(TextStyle.FULL, locale);
    }

    /**
     * Returns the name of this zone formatted according to the {@link java.time.format.TextStyle#SHORT} text style.
     *
     * @param self a ZoneId
     * @return the short display name of the ZoneId
     * @since 2.5.0
     */
    public static String getShortName(final ZoneId self) {
        return getShortName(self, Locale.getDefault());
    }

    /**
     * Returns the name of this zone formatted according to the {@link java.time.format.TextStyle#SHORT} text style
     * for the provided {@link java.util.Locale}.
     *
     * @param self   a ZoneId
     * @param locale a Locale
     * @return the short display name of the ZoneId
     * @since 2.5.0
     */
    public static String getShortName(final ZoneId self, Locale locale) {
        return self.getDisplayName(TextStyle.SHORT, locale);
    }

    /**
     * Returns a {@link java.time.ZoneOffset} for this zone as of now.
     *
     * @param self a ZoneId
     * @return a ZoneOffset
     * @since 2.5.0
     */
    public static ZoneOffset getOffset(final ZoneId self) {
        return getOffset(self, Instant.now());
    }

    /**
     * Returns a {@link java.time.ZoneOffset} for this zone as of the provided {@link java.time.Instant}.
     *
     * @param self    a ZoneId
     * @param instant an Instant
     * @return a ZoneOffset
     * @since 2.5.0
     */
    public static ZoneOffset getOffset(final ZoneId self, Instant instant) {
        return self.getRules().getOffset(instant);
    }

    /**
     * Returns a {@link java.time.ZonedDateTime} of this zone and the given {@link java.time.LocalDateTime}.
     *
     * @param self a ZoneId
     * @return a ZonedDateTime
     * @since 2.5.0
     */
    public static ZonedDateTime leftShift(final ZoneId self, LocalDateTime dateTime) {
        return ZonedDateTime.of(dateTime, self);
    }

    /* ******** java.time.ZoneOffset extension methods ******** */

    /**
     * Returns a generally  equivalent {@link java.util.TimeZone}. The offset will be truncated to minutes.
     *
     * @param self a ZoneOffset
     * @return a TimeZone
     * @since 2.5.0
     */
    public static TimeZone toTimeZone(final ZoneOffset self) {
        if (ZoneOffset.UTC.equals(self)) {
            return TimeZone.getTimeZone("GMT");
        } else if (getSeconds(self) == 0) {
            return TimeZone.getTimeZone("GMT" + self.getId());
        } else {
            // TimeZone is only hours and minutes--no seconds
            ZoneOffset noSeconds = ZoneOffset.ofHoursMinutes(getHours(self), getMinutes(self));
            return TimeZone.getTimeZone("GMT" + noSeconds.getId());
        }
    }

    /**
     * Returns the value of the provided field for the ZoneOffset as if the ZoneOffset's
     * hours/minutes/seconds were reckoned as a LocalTime.
     */
    private static int offsetFieldValue(ZoneOffset offset, TemporalField field) {
        int offsetSeconds = offset.getTotalSeconds();
        int value = LocalTime.ofSecondOfDay(Math.abs(offsetSeconds)).get(field);
        return offsetSeconds < 0 ? value * -1 : value;
    }

    /**
     * Returns the hours component of this offset. If the offset's total seconds are negative, a negative
     * value will be returned.
     *
     * @param self a ZoneOffset
     * @return the hours component value
     * @since 2.5.0
     */
    public static int getHours(final ZoneOffset self) {
        return offsetFieldValue(self, ChronoField.HOUR_OF_DAY);
    }

    /**
     * Returns the minutes component of this offset. If the offset's total seconds are negative, a negative
     * value will be returned.
     *
     * @param self a ZoneOffset
     * @return the minutes component value
     * @since 2.5.0
     */
    public static int getMinutes(final ZoneOffset self) {
        return offsetFieldValue(self, ChronoField.MINUTE_OF_HOUR);
    }

    /**
     * Returns the seconds component of this offset. This is not the same as the total seconds. For example:
     * <pre>
     *     def offset = ZoneOffset.ofHoursMinutesSeconds(0, 1, 1)
     *     assert offset.seconds == 1
     *     assert offset.totalSeconds == 61
     * </pre>
     * <p>
     * If the offset's total seconds are negative, a negative value will be returned.
     *
     * @param self a ZoneOffset
     * @return the seconds component value
     * @since 2.5.0
     */
    public static int getSeconds(final ZoneOffset self) {
        return offsetFieldValue(self, ChronoField.SECOND_OF_MINUTE);
    }

    /**
     * Supports the getAt operator; equivalent to calling the
     * {@link java.time.ZoneOffset#getLong(java.time.temporal.TemporalField)} method.
     *
     * @param self  a ZoneOffset
     * @param field a TemporalField
     * @return the ZoneOffset's field value
     * @since 2.5.0
     */
    public static long getAt(final ZoneOffset self, TemporalField field) {
        return self.getLong(field);
    }

    /**
     * Returns an {@link java.time.OffsetDateTime} of this offset and the provided {@link java.time.LocalDateTime}.
     *
     * @param self     a ZoneOffset
     * @param dateTime a LocalDateTime
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime leftShift(final ZoneOffset self, LocalDateTime dateTime) {
        return OffsetDateTime.of(dateTime, self);
    }

    /**
     * Returns an {@link java.time.OffsetDateTime} of this offset and the provided {@link java.time.LocalTime}.
     *
     * @param self a ZoneOffset
     * @param time a LocalTime
     * @return an OffsetTime
     * @since 2.5.0
     */
    public static OffsetTime leftShift(final ZoneOffset self, LocalTime time) {
        return OffsetTime.of(time, self);
    }

    /* ******** java.time.DayOfWeek extension methods ******** */

    /**
     * Returns the {@link java.time.DayOfWeek} that is {@code days} many days after this day of the week.
     *
     * @param self a DayOfWeek
     * @param days the number of days to move forward
     * @return the DayOfWeek
     * @since 2.5.0
     */
    public static DayOfWeek plus(final DayOfWeek self, int days) {
        int daysPerWeek = DayOfWeek.values().length;
        int val = ((self.getValue() + days - 1) % daysPerWeek) + 1;
        return DayOfWeek.of(val > 0 ? val : daysPerWeek + val);
    }

    /**
     * Returns the {@link java.time.DayOfWeek} that is {@code days} many days before this day of the week.
     *
     * @param self a DayOfWeek
     * @param days the number of days to move back
     * @return the DayOfWeek
     * @since 2.5.0
     */
    public static DayOfWeek minus(final DayOfWeek self, int days) {
        return plus(self, days * -1);
    }

    /**
     * Returns {@code true} if this day of the week is a weekend day (Saturday or Sunday).
     *
     * @param self a DayOfWeek
     * @return true if this DayOfWeek is Saturday or Sunday
     * @since 2.5.0
     */
    public static boolean isWeekend(final DayOfWeek self) {
        return self == DayOfWeek.SATURDAY || self == DayOfWeek.SUNDAY;
    }

    /**
     * Returns {@code true} if the DayOfWeek is a weekday.
     *
     * @return true if this DayOfWeek is Monday through Friday
     * @since 2.5.0
     */
    public static boolean isWeekday(final DayOfWeek self) {
        return !isWeekend(self);
    }

    /* ******** java.time.Month extension methods ******** */

    /**
     * Returns the {@link java.time.Month} that is {@code months} months after this month.
     *
     * @param self   a Month
     * @param months the number of months move forward
     * @return the Month
     * @since 2.5.0
     */
    public static Month plus(final Month self, int months) {
        int monthsPerYear = Month.values().length;
        int val = ((self.getValue() + months - 1) % monthsPerYear) + 1;
        return Month.of(val > 0 ? val : monthsPerYear + val);
    }

    /**
     * Returns the {@link java.time.Month} that is {@code months} months before this month.
     *
     * @param self   a Month
     * @param months the number of months to move back
     * @return the Month
     * @since 2.5.0
     */
    public static Month minus(final Month self, int months) {
        return plus(self, months * -1);
    }

    /**
     * Creates a {@link java.time.MonthDay} at the provided day of the month.
     *
     * @param self       a Month
     * @param dayOfMonth a day of the month
     * @return a MonthDay
     * @since 2.5.0
     */
    public static MonthDay leftShift(final Month self, int dayOfMonth) {
        return MonthDay.of(self, dayOfMonth);
    }

    /**
     * Creates a {@link java.time.YearMonth} at the provided {@link java.time.Year}.
     *
     * @param self a Month
     * @param year a Year
     * @return a YearMonth
     * @since 2.5.0
     */
    public static YearMonth leftShift(final Month self, Year year) {
        return YearMonth.of(year.getValue(), self);
    }

    /**
     * Returns the Time Zone offset of the Calendar as a {@link java.time.ZoneOffset}.
     *
     * @param self a Calendar
     * @return a ZoneOffset
     * @since 2.5.0
     */
    public static ZoneOffset getZoneOffset(final Calendar self) {
        int offsetMillis = self.get(Calendar.ZONE_OFFSET) + self.get(Calendar.DST_OFFSET);
        return ZoneOffset.ofTotalSeconds(offsetMillis / 1000);
    }

    /* duplicated with DateUtilExtensions.toCalendar() but we don't want modulkes to depend on one another */
    private static Calendar toCalendar(Date self) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(self);
        return cal;
    }

    /**
     * Returns the Time Zone offset of the Date as a {@link java.time.ZoneOffset},
     * which will typically be system's default offset.
     *
     * @param self a Date
     * @return a ZoneOffset
     * @since 2.5.0
     */
    public static ZoneOffset getZoneOffset(final Date self) {
        return getZoneOffset(toCalendar(self));
    }

    /**
     * Returns the Time Zone of the Calendar as a java.time.ZoneId.
     *
     * @param self a Calendar
     * @return a ZoneId
     * @since 2.5.0
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
     * @since 2.5.0
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
     * @since 2.5.0
     */
    public static Year toYear(final Calendar self) {
        return Year.of(self.get(Calendar.YEAR));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.Year}.
     *
     * @param self a Date
     * @return a Year
     * @since 2.5.0
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
     * @since 2.5.0
     */
    public static Month toMonth(final Calendar self) {
        return Month.of(self.get(Calendar.MONTH) + 1);
    }

    /**
     * Converts the Date to a corresponding {@link java.time.Month}.
     *
     * @param self a Date
     * @return a Month
     * @since 2.5.0
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
     * @since 2.5.0
     */
    public static MonthDay toMonthDay(final Calendar self) {
        return MonthDay.of(toMonth(self), self.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.MonthDay}.
     *
     * @param self a Date
     * @return a MonthDay
     * @since 2.5.0
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
     * @since 2.5.0
     */
    public static YearMonth toYearMonth(final Calendar self) {
        return toYear(self).atMonth(toMonth(self));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.YearMonth}.
     *
     * @param self a Date
     * @return a YearMonth
     * @since 2.5.0
     */
    public static YearMonth toYearMonth(final Date self) {
        return toYearMonth(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.DayOfWeek}. If the Calendar has a different
     * time zone than the system default, the DayOfWeek will be adjusted into the default time zone.
     *
     * @param self a Calendar
     * @return a DayOfWeek
     * @since 2.5.0
     */
    public static DayOfWeek toDayOfWeek(final Calendar self) {
        return DayOfWeek.of(self.get(Calendar.DAY_OF_WEEK)).minus(1);
    }

    /**
     * Converts the Date to a corresponding {@link java.time.DayOfWeek}.
     *
     * @param self a Date
     * @return a DayOfWeek
     * @since 2.5.0
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
     * @since 2.5.0
     */
    static LocalDate toLocalDate(final Calendar self) {
        return LocalDate.of(self.get(Calendar.YEAR), toMonth(self), self.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.LocalDate}.
     *
     * @param self a Date
     * @return a LocalDate
     * @since 2.5.0
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
     * @since 2.5.0
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
     * @since 2.5.0
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
     * @since 2.5.0
     */
    public static LocalDateTime toLocalDateTime(final Calendar self) {
        return LocalDateTime.of(toLocalDate(self), toLocalTime(self));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.LocalDateTime}.
     *
     * @param self a Date
     * @return a LocalDateTime
     * @since 2.5.0
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
     * @since 2.5.0
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
     * @since 2.5.0
     */
    public static ZonedDateTime toZonedDateTime(final Date self) {
        return toZonedDateTime(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.OffsetDateTime}.
     *
     * @param self a Calendar
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime toOffsetDateTime(final Calendar self) {
        return OffsetDateTime.of(toLocalDateTime(self), getZoneOffset(self));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.OffsetDateTime}.
     *
     * @param self a Date
     * @return an OffsetDateTime
     * @since 2.5.0
     */
    public static OffsetDateTime toOffsetDateTime(final Date self) {
        return toOffsetDateTime(toCalendar(self));
    }

    /**
     * Converts the Calendar to a corresponding {@link java.time.OffsetTime}.
     *
     * @param self a Calendar
     * @return an OffsetTime
     * @since 2.5.0
     */
    public static OffsetTime toOffsetTime(final Calendar self) {
        return OffsetTime.of(toLocalTime(self), getZoneOffset(self));
    }

    /**
     * Converts the Date to a corresponding {@link java.time.OffsetTime}.
     *
     * @param self a Date
     * @return an OffsetTime
     * @since 2.5.0
     */
    public static OffsetTime toOffsetTime(final Date self) {
        return toOffsetTime(toCalendar(self));
    }

    /**
     * Convenience method for converting a Calendar to a corresponding {@link java.time.Instant}.
     *
     * @param self a Calendar
     * @return an Instant
     * @since 2.5.0
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
     * @since 2.5.0
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
     * @since 2.5.0
     */
    public static ZoneOffset toZoneOffset(final TimeZone self, Instant instant) {
        return self.toZoneId().getRules().getOffset(instant);
    }
}
