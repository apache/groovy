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
package groovy.bugs

import groovy.test.GroovyTestCase

import java.text.SimpleDateFormat

class Groovy5687Bug extends GroovyTestCase {
    void testStaticAccessToInterfaceConstant() {
        assert DateTimeUtils.convertMilitaryTimeToAmPm('20:30') == '8:30pm'
    }

    static interface DateTimeFormatConstants {
        SimpleDateFormat AM_PM_TIME_FORMAT = new SimpleDateFormat("h:mma", new Locale('en_US'))
        SimpleDateFormat MILITARY_TIME_FORMAT = new SimpleDateFormat("HH:mm")
    }

    static interface DateTimeFormatConstants2 extends DateTimeFormatConstants {}

    static class DateTimeUtils implements DateTimeFormatConstants2 {
        static String convertMilitaryTimeToAmPm(String militaryTime) {
            Date date = MILITARY_TIME_FORMAT.parse(militaryTime)
            AM_PM_TIME_FORMAT.format(date).toLowerCase()
        }
    }
}
