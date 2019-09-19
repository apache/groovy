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

import groovy.test.GroovyTestCase

class LoopBreakTest extends GroovyTestCase {

    void testWhileWithBreak() {
        def x = 0
        while (true) {
            if (x == 5) {
                break
            }
            ++x

            assert x < 10 , "Should never get here"
        }
    }
    
    /**

      We currently do not support do ... while in the JSR syntax

    void testDoWhileWithBreak() {
        def x = 0
        do {
            //println "in do-while loop and x = ${x}"

            if (x == 5) {
                break
            }
            ++x

            assert x < 10 , "Should never get here"
        }
        while (true)

        println "worked: do-while completed with value ${x}"
    }
    */

    void testForWithBreak() {
        def returnValue
        for (x in 0..20) {
            if (x == 5) {
                returnValue = x
                break
            }
            assert x < 10 , "Should never get here"
        }
    }
 }
