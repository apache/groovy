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

class VariableScopingBug extends TestSupport {
    
    void testUndeclaredVariable() {
        shouldFail(MissingPropertyException) {
            def shell = new GroovyShell()
            shell.evaluate("""
                class SomeTest {
                    void run() {
                        for (z in 0..2) {
                            def x = [1, 2, 3]
                        }

                        for (t in 0..3) {
                            for (y in x) { // previous x no longer be in scope
                                println x
                            }
                        }
                    }
                }
                new SomeTest().run()
            """)
        }
    }

    void testVariableReuseAllowedInDifferentScopes() {
        def shell = new GroovyShell()
        shell.evaluate("""
            for (z in 0..2) {
                def x = [1, 2, 3]
            }

            for (t in 0..3) {
                def x = 123
                println x
            }
        """)
    }

    // GROOVY-5961
    void testVariableInAicInsideStaticMethod() {
        def shell = new GroovyShell()
        shell.evaluate("""
            static foo() {
                new LinkedList([1, 2]) {
                    int count
                    Object get(int i) { super.get(count++) }
                }
            }

            def l = foo()
            assert l.count == 0
            assert l[0] == 1
            assert l.count == 1
        """)
    }
}
