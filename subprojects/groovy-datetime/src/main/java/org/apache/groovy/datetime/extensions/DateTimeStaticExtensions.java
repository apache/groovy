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
import java.time.format.DateTimeFormatter;

/**
 * This class defines new static extension methods which appear on normal JDK
 * Date/Time API (java.time) classes inside the Groovy environment.
 */
public final class DateTimeStaticExtensions {

    // Static methods only
    private DateTimeStaticExtensions() {
    }

    /**
     * Parse text into a {@link java.time.LocalDate} using the provided pattern.
     *
     * Note: the order of parameters differs from versions of this method for the legacy Date class.
     *
     * @param type    placeholder variable used by Groovy categories; ignored for default static methods
     * @param text    String to be parsed to create the date instance
     * @param pattern pattern used to parse the text
     * @return a LocalDate representing the parsed text
     * @throws java.lang.IllegalArgumentException if the pattern is invalid
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     * @see java.time.format.DateTimeFormatter
     * @see java.time.LocalDate#parse(java.lang.CharSequence, java.time.format.DateTimeFormatter)
     * @since 2.5.0
     */
    public static LocalDate parse(final LocalDate type, CharSequence text, String pattern) {
        return LocalDate.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse text into a {@link java.time.LocalDateTime} using the provided pattern.
     *
     * Note: the order of parameters differs from versions of this method for the legacy Date class.
     *
     * @param type    placeholder variable used by Groovy categories; ignored for default static methods
     * @param text    String to be parsed to create the date instance
     * @param pattern pattern used to parse the text
     * @return a LocalDateTime representing the parsed text
     * @throws java.lang.IllegalArgumentException if the pattern is invalid
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     * @see java.time.format.DateTimeFormatter
     * @see java.time.LocalDateTime#parse(java.lang.CharSequence, java.time.format.DateTimeFormatter)
     * @since 2.5.0
     */
    public static LocalDateTime parse(final LocalDateTime type, CharSequence text, String pattern) {
        return LocalDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse text into a {@link java.time.LocalTime} using the provided pattern.
     *
     * @param type    placeholder variable used by Groovy categories; ignored for default static methods
     * @param text    String to be parsed to create the date instance
     * @param pattern pattern used to parse the text
     * @return a LocalTime representing the parsed text
     * @throws java.lang.IllegalArgumentException if the pattern is invalid
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     * @see java.time.format.DateTimeFormatter
     * @see java.time.LocalTime#parse(java.lang.CharSequence, java.time.format.DateTimeFormatter)
     * @since 2.5.0
     */
    public static LocalTime parse(final LocalTime type, CharSequence text, String pattern) {
        return LocalTime.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse text into a {@link java.time.MonthDay} using the provided pattern.
     *
     * @param type    placeholder variable used by Groovy categories; ignored for default static methods
     * @param text    String to be parsed to create the date instance
     * @param pattern pattern used to parse the text
     * @return a MonthDay representing the parsed text
     * @throws java.lang.IllegalArgumentException if the pattern is invalid
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     * @see java.time.format.DateTimeFormatter
     * @see java.time.MonthDay#parse(java.lang.CharSequence, java.time.format.DateTimeFormatter)
     * @since 2.5.0
     */
    public static MonthDay parse(final MonthDay type, CharSequence text, String pattern) {
        return MonthDay.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse text into an {@link java.time.OffsetDateTime} using the provided pattern.
     *
     * @param type    placeholder variable used by Groovy categories; ignored for default static methods
     * @param text    String to be parsed to create the date instance
     * @param pattern pattern used to parse the text
     * @return an OffsetDateTime representing the parsed text
     * @throws java.lang.IllegalArgumentException if the pattern is invalid
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     * @see java.time.format.DateTimeFormatter
     * @see java.time.OffsetDateTime#parse(java.lang.CharSequence, java.time.format.DateTimeFormatter)
     * @since 2.5.0
     */
    public static OffsetDateTime parse(final OffsetDateTime type, CharSequence text, String pattern) {
        return OffsetDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse text into an {@link java.time.OffsetTime} using the provided pattern.
     *
     * @param type    placeholder variable used by Groovy categories; ignored for default static methods
     * @param text    String to be parsed to create the date instance
     * @param pattern pattern used to parse the text
     * @return an OffsetTime representing the parsed text
     * @throws java.lang.IllegalArgumentException if the pattern is invalid
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     * @see java.time.format.DateTimeFormatter
     * @see java.time.OffsetTime#parse(java.lang.CharSequence, java.time.format.DateTimeFormatter)
     * @since 2.5.0
     */
    public static OffsetTime parse(final OffsetTime type, CharSequence text, String pattern) {
        return OffsetTime.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse text into a {@link java.time.Year} using the provided pattern.
     *
     * @param type    placeholder variable used by Groovy categories; ignored for default static methods
     * @param text    String to be parsed to create the date instance
     * @param pattern pattern used to parse the text
     * @return a Year representing the parsed text
     * @throws java.lang.IllegalArgumentException if the pattern is invalid
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     * @see java.time.format.DateTimeFormatter
     * @see java.time.Year#parse(java.lang.CharSequence, java.time.format.DateTimeFormatter)
     * @since 2.5.0
     */
    public static Year parse(final Year type, CharSequence text, String pattern) {
        return Year.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse text into a {@link java.time.YearMonth} using the provided pattern.
     *
     * @param type    placeholder variable used by Groovy categories; ignored for default static methods
     * @param text    String to be parsed to create the date instance
     * @param pattern pattern used to parse the text
     * @return a YearMonth representing the parsed text
     * @throws java.lang.IllegalArgumentException if the pattern is invalid
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     * @see java.time.format.DateTimeFormatter
     * @see java.time.YearMonth#parse(java.lang.CharSequence, java.time.format.DateTimeFormatter)
     * @since 2.5.0
     */
    public static YearMonth parse(final YearMonth type, CharSequence text, String pattern) {
        return YearMonth.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parse text into a {@link java.time.ZonedDateTime} using the provided pattern.
     *
     * @param type    placeholder variable used by Groovy categories; ignored for default static methods
     * @param text    String to be parsed to create the date instance
     * @param pattern pattern used to parse the text
     * @return a ZonedDateTime representing the parsed text
     * @throws java.lang.IllegalArgumentException if the pattern is invalid
     * @throws java.time.format.DateTimeParseException if the text cannot be parsed
     * @see java.time.format.DateTimeFormatter
     * @see java.time.ZonedDateTime#parse(java.lang.CharSequence, java.time.format.DateTimeFormatter)
     * @since 2.5.0
     */
    public static ZonedDateTime parse(final ZonedDateTime type, CharSequence text, String pattern) {
        return ZonedDateTime.parse(text, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Returns the {@link java.time.ZoneOffset} currently associated with the system default {@link java.time.ZoneId}.
     *
     * @param type placeholder variable used by Groovy categories; ignored for default static methods
     * @return a ZoneOffset
     * @see java.time.ZoneId#systemDefault()
     * @since 2.5.0
     */
    public static ZoneOffset systemDefault(final ZoneOffset type) {
        return DateTimeExtensions.getOffset(ZoneId.systemDefault());
    }

    /**
     * Obtains a Period consisting of the number of years between two {@link java.time.Year} instances.
     * The months and days of the Period will be zero.
     * The result of this method can be a negative period if the end is before the start.
     *
     * @param type           placeholder variable used by Groovy categories; ignored for default static methods
     * @param startInclusive the start {@link java.time.Year}, inclusive, not null
     * @param endExclusive   the end {@link java.time.Year}, exclusive, not null
     * @return a Period between the years
     * @see java.time.Period#between(LocalDate, LocalDate)
     */
    public static Period between(final Period type, Year startInclusive, Year endExclusive) {
        MonthDay now = MonthDay.of(Month.JANUARY, 1);
        return Period.between(
                DateTimeExtensions.leftShift(startInclusive, now),
                DateTimeExtensions.leftShift(endExclusive, now))
                .withDays(0)
                .withMonths(0);
    }

    /**
     * Obtains a Period consisting of the number of years and months between two {@link java.time.YearMonth} instances.
     * The days of the Period will be zero.
     * The result of this method can be a negative period if the end is before the start.
     *
     * @param type           placeholder variable used by Groovy categories; ignored for default static methods
     * @param startInclusive the start {@link java.time.YearMonth}, inclusive, not null
     * @param endExclusive   the end {@link java.time.YearMonth}, exclusive, not null
     * @return a Period between the year/months
     * @see java.time.Period#between(LocalDate, LocalDate)
     */
    public static Period between(final Period type, YearMonth startInclusive, YearMonth endExclusive) {
        int dayOfMonth = 1;
        return Period.between(
                DateTimeExtensions.leftShift(startInclusive, dayOfMonth),
                DateTimeExtensions.leftShift(endExclusive, dayOfMonth))
                .withDays(0);
    }

}
