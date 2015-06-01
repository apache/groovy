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
 */
class VariableScopingBug extends TestSupport {
    
    void testBug() {
        // undeclared variable x

        shouldFail {
            def shell = new GroovyShell()
            shell.evaluate("""
                class SomeTest {
                    void run() {
                        for (z in 0..2) {
                            def x = [1, 2, 3]
                        }

                        for (t in 0..3) {
                            for (y in x) {
                                println x
                            }
                        }
                    }
               }
               new SomeTest().run()""")
           }
    }

    void testVariableReuse() {
        def shell = new GroovyShell()
        shell.evaluate("""
            for (z in 0..2) {
                def x = [1, 2, 3]
            }

            for (t in 0..3) {
                def x = 123
                println x
            }""")
    }
}