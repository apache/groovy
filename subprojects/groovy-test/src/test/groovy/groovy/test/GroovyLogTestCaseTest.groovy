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
package groovy.test

import java.util.logging.Level
import java.util.logging.Logger

/**
 * Showing usage of the GroovyLogTestCase
 */
class GroovyLogTestCaseTest extends GroovyLogTestCase {

    static final LOG = Logger.getLogger('groovy.lang.GroovyLogTestCaseTest')

    void loggedMethod() {
        LOG.finer 'some log entry'
    }

    void testStringLog(){
        def result = stringLog(Level.FINER, 'groovy.lang.GroovyLogTestCaseTest') {
            loggedMethod()
        }
        assertTrue result, result.contains('some log entry')
    }

    void testCombinedUsageForMetaClass(){
/*
        def result = withLevel(Level.FINER, 'groovy.lang.MetaClass') {
            stringLog(Level.FINER, 'methodCalls'){
                'hi'.toString()
            }
        }
        assertTrue result, result.contains('java.lang.String toString()')
*/
    }
}