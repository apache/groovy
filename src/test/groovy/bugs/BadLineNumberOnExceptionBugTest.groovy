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

/**
 * Ensure that the correct line information is reported when an exception is thrown.
 * <p>
 * This test covers: <ul>
 * <li><a href="https://issues.apache.org/jira/browse/GROOVY-3067">GROOVY-3067</a></li>
 * <li><a href="https://issues.apache.org/jira/browse/GROOVY-2983">GROOVY-2983</a></li>
 */
class BadLineNumberOnExceptionBugTest extends GroovyTestCase {

    void testGroovy3067() {
        assertScript """
            class Foo {
                boolean hello() { true }
            }

            try {
                foo = new Foo()

                if(foo.hello()()) { // line 9
                    println "do"
                    println "do"
                    println "do"
                    println "do"
                }

                assert false
            } catch (MissingMethodException e) {
                def scriptTraceElement = e.stackTrace.find { it.className.startsWith(GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 9 == scriptTraceElement.lineNumber
            }
        """
    }

    void testGroovy2983() {
        assertScript """
            def foo() {
                integer.metaClass = null // line 3
                integer.metaClass = null
                integer.metaClass = null
                integer.metaClass = null
            }
            
            try {
                foo()

                assert false
            } catch (MissingPropertyException e) {
                def scriptTraceElement = e.stackTrace.find { it.className.startsWith(GroovyTestCase.TEST_SCRIPT_NAME_PREFIX) }
                assert 3 == scriptTraceElement.lineNumber
            }
        """
    }
}