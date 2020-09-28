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
package org.apache.groovy.plugin.testng

import groovy.test.GroovyShellTestCase

class TestNgRunnerTest extends GroovyShellTestCase {

    @Override
    void setUp() {
        super.setUp()
        System.setProperty('groovy.plugin.testng.output', 'build/testng-output')
    }

    @Override
    void tearDown() {
        super.tearDown()
        System.clearProperty('groovy.plugin.testng.output')
    }

    void testRunWithTestNg() {
        String test = '''
            class F {
                @org.testng.annotations.Test
                void m() {
                    org.testng.Assert.assertEquals(1,1)
                }
            }
        '''
        shell.run(test, 'F.groovy', [])
    }

    void testTestNgRunnerListedInRunnerList() {
        assert shouldFail(GroovyRuntimeException) {
            shell.run('class F {}', 'F.groovy', [])
        }.contains('* ' + TestNgRunner.class.getName())
    }

}
