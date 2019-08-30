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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy3311 {

    @Test
    void testStaticInitUsingOwnConstructor() {
        assertScript '''
            class Day extends Date {
                static Day get(_date) {
                    return new Day(new java.text.SimpleDateFormat('MM.dd.yyyy').parse(_date))
                }
                Day(Date _date) {
                    super(_date.time)
                    def time = getTime()
                    24.times { hour ->
                        hoursOfTheDay << new Date(time + hour*1000*60*60)
                    }
                }
                List<Date> hoursOfTheDay = []

                @Override String toString() {
                    this.format('MM.dd.yyyy')
                }

                static def period = (1..3).collect { get "12.3${it}.1999" }
            }

            assert Day.period*.toString() == ['12.31.1999', '01.01.2000', '01.02.2000']
        '''
    }
}
