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

class Groovy2773Bug extends GroovyTestCase {
    void test() {
        assertScript '''
            class DateTime {
                long millis
                DateTime() { millis = new Date().getTime() }
                DateTime(long millis) { this.millis = millis }
                DateTime minusWeeks(Integer numWeeks) {
                    return new DateTime(millis - (1000 * 60 * 60 * 24 * 7 * numWeeks))
                }
            }
            
            class Utils {
                static nowUTC() { return new DateTime() }
            }
            
            import static Utils.nowUTC
            
            DateTime baseDate = Utils.nowUTC()
            Long now = new Date().getTime()
            Long lastWeekFromBaseDate = baseDate.minusWeeks(1).millis
            Long lastWeekInline = Utils.nowUTC().minusWeeks(1).millis
            assert now > lastWeekFromBaseDate, "Should have passed"
            assert now > lastWeekInline, "Here's the bug"
        '''
    }

}
