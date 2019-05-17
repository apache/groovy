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
package gdk

import groovy.test.GroovyTestCase

class WorkingWithDateUtilTypesTest extends GroovyTestCase {

    void testGetAt() {
        assertScript '''
        // tag::calendar_getAt[]
        import static java.util.Calendar.*    // <1>

        def cal = Calendar.instance
        cal[YEAR] = 2000                      // <2>
        cal[MONTH] = JANUARY                  // <2>
        cal[DAY_OF_MONTH] = 1                 // <2>
        assert cal[DAY_OF_WEEK] == SATURDAY   // <3>
        // end::calendar_getAt[]
        '''
    }

    void testDateArithmetic() {
        // tag::date_arithmetic[]
        def utc = TimeZone.getTimeZone('UTC')
        Date date = Date.parse("yyyy-MM-dd HH:mm", "2010-05-23 09:01", utc)

        def prev = date - 1
        def next = date + 1

        def diffInDays = next - prev
        assert diffInDays == 2

        int count = 0
        prev.upto(next) { count++ }
        assert count == 3
        // end::date_arithmetic[]
    }

    void testDateParsing() {
        assertScript '''
        import static java.util.Calendar.*

        // tag::date_parsing[]
        def orig = '2000-01-01'
        def newYear = Date.parse('yyyy-MM-dd', orig)
        assert newYear[DAY_OF_WEEK] == SATURDAY
        assert newYear.format('yyyy-MM-dd') == orig
        assert newYear.format('dd/MM/yyyy') == '01/01/2000'
        // end::date_parsing[]
        '''
    }

    void testCopyWith() {
        assertScript '''
        import static java.util.Calendar.*

        // tag::date_copyWith[]
        def newYear = Date.parse('yyyy-MM-dd', '2000-01-01')
        def newYearsEve = newYear.copyWith(
            year: 1999,
            month: DECEMBER,
            dayOfMonth: 31
        )
        assert newYearsEve[DAY_OF_WEEK] == FRIDAY
        // end::date_copyWith[]
        '''
    }

}
