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

package org.apache.groovy.dateutil.extensions;

import org.junit.Test;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class DateUtilExtensionsTest {
    @Test
    public void plus() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date dec31 = sdf.parse("20171231");
        assertEquals("20180101", sdf.format(DateUtilExtensions.plus(dec31, 1)));
        assertEquals("20180101", sdf.format(DateUtilExtensions.plus(new Timestamp(dec31.getTime()), 1)));
    }

    @Test
    public void minus() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date jan01 = sdf.parse("20180101");
        assertEquals("20171231", sdf.format(DateUtilExtensions.minus(jan01, 1)));
        assertEquals("20171231", sdf.format(DateUtilExtensions.minus(new Timestamp(jan01.getTime()), 1)));
    }

    @Test
    public void next() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse("20171231"));
        assertEquals("20180101", sdf.format(DateUtilExtensions.next(calendar).getTime()));
    }

    @Test
    public void previous() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse("20180101"));
        assertEquals("20171231", sdf.format(DateUtilExtensions.previous(calendar).getTime()));
    }
}
