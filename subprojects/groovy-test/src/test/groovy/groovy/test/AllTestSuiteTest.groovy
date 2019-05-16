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

/**
 * Testing groovy.util.AllTestSuite.
 * The suite() method must properly collect Test files under the given dir and pattern,
 * add found files to the log,
 * produce a proper TestSuite,
 * and wrap Scripts into TestCases.
 */
class AllTestSuiteTest extends GroovyLogTestCase {

    def suite

    void setUp() {
        suite = null
    }

    void testSuiteForThisFileOnly() {
        def result = stringLog(Level.FINEST, 'groovy.test.AllTestSuite') {
            withProps('src/test/groovy/groovy/test', 'AllTestSuiteTest.groovy') {
                suite = AllTestSuite.suite()
            }
        }
        assertTrue result, result.contains('AllTestSuiteTest.groovy')
        assertEquals 1 + 1, result.count("\n")   // only one entry in the log
        assert suite, 'Resulting suite should not be null'
        assertEquals 2, suite.countTestCases() // the 2 test methods in this file
    }

    void testAddingScriptsThatDoNotInheritFromTestCase() {
        withProps('src/test/groovy/groovy/test', 'suite/*.groovy') {
            suite = AllTestSuite.suite()
        }
        assert suite
        assertEquals 1, suite.countTestCases()
        suite.testAt(0) // call the contained Script to makes sure it is testable
    }

    /** store old System property values for not overriding them accidentally */
    void withProps(dir, pattern, yield) {
        String olddir = System.properties.'groovy.test.dir'
        String oldpat = System.properties.'groovy.test.pattern'
        System.properties.'groovy.test.dir' = dir
        System.properties.'groovy.test.pattern' = pattern
        yield()
        if (olddir) System.properties.'groovy.test.dir' = olddir
        if (oldpat) System.properties.'groovy.test.pattern' = oldpat
    }
}