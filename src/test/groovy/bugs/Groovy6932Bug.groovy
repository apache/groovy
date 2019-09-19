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

class Groovy6932Bug extends GroovyTestCase {
    void testLoggingWithinClosuresShouldHaveGuards() {
        assertScript '''
            @Grab('org.slf4j:slf4j-simple:1.7.25')
            import groovy.util.logging.Slf4j

            new TestCode().doSomethingThatLogs()

            @Slf4j
            class TestCode {
                void doSomethingThatLogs(){
                    int info = 0
                    int trace = 0
                    log.info createLogString("${info++}")
                    log.trace createLogString("${trace++}")
                    Closure c1 = { log.info createLogString("${info++}") }
                    c1()
                    Closure c2 = { log.trace createLogString("${trace++}") }
                    c2()
                    assert info == 2
                    assert trace == 0
                }

                String createLogString(def p){
                    "called with $p"
                }
            }
        '''
    }
}
