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
package metaprogramming

import groovy.test.GroovyTestCase
import groovy.time.TimeCategory

class CategoryTest extends GroovyTestCase {

    void testApplyTimeCategory() {
        // tag::time_category[]
        use(TimeCategory)  {
            println 1.minute.from.now       // <1>
            println 10.hours.ago

            def someDate = new Date()       // <2>
            println someDate - 3.months
        }
        // end::time_category[]
    }

    void testCategoryAnnotation() {
        assertScript '''
            // tag::time_category_anno[]
            class Distance {
                def number
                String toString() { "${number}m" }
            }

            @Category(Number)
            class NumberCategory {
                Distance getMeters() {
                    new Distance(number: this)
                }
            }

            use (NumberCategory)  {
                assert 42.meters.toString() == '42m'
            }
            // end::time_category_anno[]
        '''
    }
}
