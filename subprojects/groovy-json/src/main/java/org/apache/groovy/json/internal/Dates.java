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
package org.apache.groovy.json.internal;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Dates {

    private static TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

    public static long utc(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.setTimeZone(UTC_TIME_ZONE);
        return calendar.getTime().getTime();
    }

    private static Date internalDate(TimeZone tz, int year, int month, int day, int hour,
                                     int minute, int second, int miliseconds) {

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, miliseconds);

        calendar.setTimeZone(tz);

        return calendar.getTime();
    }

    public static Date toDate(TimeZone tz, int year, int month, int day,
                              int hour, int minute, int second) {
        return internalDate(tz, year, month, day, hour, minute, second, 0);
    }

    public static Date toDate(TimeZone tz, int year, int month, int day,
                              int hour, int minute, int second, int miliseconds) {
        return internalDate(tz, year, month, day, hour, minute, second, miliseconds);
    }

    static final int SHORT_ISO_8601_TIME_LENGTH = "1994-11-05T08:15:30Z".length();
    static final int LONG_ISO_8601_TIME_LENGTH = "1994-11-05T08:15:30-05:00".length();
    public static final int JSON_TIME_LENGTH = "2013-12-14T01:55:33.412Z".length();

    public static Date fromISO8601(char[] charArray, int from, int to) {
        try {
            if (isISO8601(charArray, from, to)) {
                int year = CharScanner.parseIntFromTo(charArray, from, from + 4);
                int month = CharScanner.parseIntFromTo(charArray, from + 5, from + 7);
                int day = CharScanner.parseIntFromTo(charArray, from + 8, from + 10);
                int hour = CharScanner.parseIntFromTo(charArray, from + 11, from + 13);

                int minute = CharScanner.parseIntFromTo(charArray, from + 14, from + 16);

                int second = CharScanner.parseIntFromTo(charArray, from + 17, from + 19);
                TimeZone tz = null;

                if (charArray[from + 19] == 'Z') {
                    tz = TimeZone.getTimeZone("GMT");
                } else {
                    String tzStr = "GMT" + String.valueOf(charArray, from + 19, 6);
                    tz = TimeZone.getTimeZone(tzStr);
                }
                return toDate(tz, year, month, day, hour, minute, second);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public static Date fromJsonDate(char[] charArray, int from, int to) {
        try {
            if (isJsonDate(charArray, from, to)) {
                int year = CharScanner.parseIntFromTo(charArray, from, from + 4);
                int month = CharScanner.parseIntFromTo(charArray, from + 5, from + 7);
                int day = CharScanner.parseIntFromTo(charArray, from + 8, from + 10);
                int hour = CharScanner.parseIntFromTo(charArray, from + 11, from + 13);

                int minute = CharScanner.parseIntFromTo(charArray, from + 14, from + 16);

                int second = CharScanner.parseIntFromTo(charArray, from + 17, from + 19);

                int miliseconds = CharScanner.parseIntFromTo(charArray, from + 20, from + 23);

                TimeZone tz = TimeZone.getTimeZone("GMT");

                return toDate(tz, year, month, day, hour, minute, second, miliseconds);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }

    public static boolean isISO8601(char[] charArray, int start, int to) {
        boolean valid = true;
        final int length = to - start;

        if (length == SHORT_ISO_8601_TIME_LENGTH) {
            valid &= (charArray[start + 19] == 'Z');
        } else if (length == LONG_ISO_8601_TIME_LENGTH) {
            valid &= (charArray[start + 19] == '-' || charArray[start + 19] == '+');
            valid &= (charArray[start + 22] == ':');
        } else {
            return false;
        }

        //  0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4
        // "1 9 9 4 - 1 1 - 0 5 T 0 8 : 1 5 : 3 0 - 0 5 : 0 0

        valid &= (charArray[start + 4] == '-') &&
                (charArray[start + 7] == '-') &&
                (charArray[start + 10] == 'T') &&
                (charArray[start + 13] == ':') &&
                (charArray[start + 16] == ':');

        return valid;
    }

    public static boolean isISO8601QuickCheck(char[] charArray, int start, int to) {
        final int length = to - start;

        try {
            return length == JSON_TIME_LENGTH || length == LONG_ISO_8601_TIME_LENGTH
                    || length == SHORT_ISO_8601_TIME_LENGTH || (length >= 17 && (charArray[start + 16] == ':'));

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean isJsonDate(char[] charArray, int start, int to) {
        boolean valid = true;
        final int length = to - start;

        if (length != JSON_TIME_LENGTH) {
            return false;
        }

        valid &= (charArray[start + 19] == '.' || charArray[start + 19] == '+');

        if (!valid) {
            return false;
        }

        valid &= (charArray[start + 4] == '-') &&
                (charArray[start + 7] == '-') &&
                (charArray[start + 10] == 'T') &&
                (charArray[start + 13] == ':') &&
                (charArray[start + 16] == ':');

        return valid;
    }
}
