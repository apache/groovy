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
package groovy

import groovy.test.GroovyTestCase;
import groovy.time.TimeCategory

class SqlDateTest extends GroovyTestCase {

    void testIncrement() {
        def nowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(nowMillis)
        def nowOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        sqlDate++
        def incOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        assertTrue "incrementing a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date

        // difference adjusted for daylight savings
        def diff = (sqlDate.getTime() + incOffset) - (nowMillis + nowOffset)
        assertEquals "incrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }

      void testDecrement() {
        def nowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(nowMillis)
        def nowOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        sqlDate--
        def decOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        assertTrue "decrementing a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        // difference adjusted for daylight savings
        def diff = (nowMillis + nowOffset) - (sqlDate.getTime() + decOffset)

        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
      
    void testPlusOperator() {
        def nowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(nowMillis)
        def nowOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        sqlDate += 1
        def incOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()

        assertTrue  "the plus operator applied to a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        // difference adjusted for daylight savings
        def diff = (sqlDate.getTime() + incOffset) - (nowMillis + nowOffset)
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
    
    void testMinusOperator() {
        def nowMillis = System.currentTimeMillis()
        def sqlDate = new java.sql.Date(nowMillis)
        def nowOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()
        sqlDate -= 1
        def decOffset = TimeCategory.getDaylightSavingsOffset(sqlDate).toMilliseconds()

        assertTrue  "the minus operator applied to a java.sql.Date returned an incorrect type: ${sqlDate.class}", sqlDate instanceof java.sql.Date
        
        // difference adjusted for daylight savings
        def diff = (nowMillis + nowOffset) - (sqlDate.getTime() + decOffset)
        assertEquals "decrementing a java.sql.Date did not work properly", 1000 * 60 * 60 * 24, diff
    }
}
