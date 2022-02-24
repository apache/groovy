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

/**
 * Flaky on CI servers, so retry a few times. We should make most tests involving
 * Grape more isolated but having at least one end-to-end functional test might still be okay.
 */
class Groovy8060Bug extends GroovyTestCase {
    private static final int MAX_RETRIES = 3
    void testLoggingWithinClosuresThatAreMethodArgsShouldHaveGuards() {
        int retry = 0
        boolean success = false
        Exception maybeIgnore = null
        while (retry++ < MAX_RETRIES && !success) {
            try {
                assertScript '''
                    @Grab('org.slf4j:slf4j-simple:1.7.36')
                    import groovy.util.logging.Slf4j

                    @Slf4j
                    class LogMain {
                        public static int count = 0

                        static void main(args) {
                            assert !log.isTraceEnabled()
                            1.times { log.trace("${count++}") }
                            assert !count
                        }
                    }
                '''
                success = true
            } catch(Exception ex) {
                maybeIgnore = ex
                sleep 100 * retry
            }
        }
        if (!success) throw new RuntimeException("Couldn't assert script after $MAX_RETRIES retries", maybeIgnore)
    }
}
