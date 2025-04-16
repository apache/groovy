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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

class Groovy7870Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testLineNumberOnImplicitReturnsWithLeftShift() {
        assertScript '''
            def test() {
                def thrower = new org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.Thrower()
                thrower << null
            }
            try {
                test()
            } catch (org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.DummyException e) {
                def scriptTraceElement = e.stackTrace.find { it.className.startsWith(groovy.test.GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 4 == scriptTraceElement.lineNumber
            }
        '''
    }

    void testLineNumberOnImplicitReturnsWithRightShift() {
        assertScript '''
            def test() {
                def thrower = new org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.Thrower()
                thrower >> null
            }
            try {
                test()
            } catch (org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.DummyException e) {
                def scriptTraceElement = e.stackTrace.find { it.className.startsWith(groovy.test.GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 4 == scriptTraceElement.lineNumber
            }
        '''
    }

    void testLineNumberOnImplicitReturnsWithRightShiftUnsigned() {
        assertScript '''
            def test() {
                def thrower = new org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.Thrower()
                thrower >>> null
            }
            try {
                test()
            } catch (org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.DummyException e) {
                def scriptTraceElement = e.stackTrace.find { it.className.startsWith(groovy.test.GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 4 == scriptTraceElement.lineNumber
            }
        '''
    }

    void testLineNumberOnImplicitReturnsWithCompareTo() {
        assertScript '''
            def test(org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.Thrower o) {
                def thrower = new org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.Thrower()
                thrower <=> o
            }
            try {
                test(null)
            } catch (org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.DummyException e) {
                def scriptTraceElement = e.stackTrace.find { it.className.startsWith(groovy.test.GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 4 == scriptTraceElement.lineNumber
            }
        '''
    }

    void testLineNumberOnImplicitReturnsWithIn() {
        assertScript '''
            def test() {
                def thrower = new org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.Thrower()
                'abc' in thrower
            }
            try {
                test()
            } catch (org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.DummyException e) {
                def scriptTraceElement = e.stackTrace.find { it.className.startsWith(groovy.test.GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 4 == scriptTraceElement.lineNumber
            }
        '''
    }

    void testLineNumberOnImplicitReturnsWithEquals() {
        assertScript '''
            def test(org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.Thrower o) {
                def thrower = new org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.Thrower()
                thrower == o
            }
            try {
                test(null)
            } catch (org.codehaus.groovy.classgen.asm.sc.bugs.Groovy7870Bug.DummyException e) {
                def scriptTraceElement = e.stackTrace.find { it.className.startsWith(groovy.test.GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 4 == scriptTraceElement.lineNumber
            }
        '''
    }

    static class Thrower implements Comparable<Thrower> {
        @Override
        int compareTo(Thrower o) {
            throw new DummyException()
        }

        boolean isCase(Object o) {
            throw new DummyException()
        }

        def rightShiftUnsigned(Object o) {
            throw new DummyException()
        }


        def rightShift(Object o) {
            throw new DummyException()
        }

        def leftShift(Object o) {
            throw new DummyException()
        }
    }

    static class DummyException extends Exception {}
}
