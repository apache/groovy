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

final class Groovy9892 extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {

    void testIncrementPropertyWithinClosure1() {
        assertScript '''
            class C {
                int prefix
                int postfix

                def test() {
                    { ->
                        "pre:${++prefix} post:${postfix++}"
                    }.call()
                }
            }
            new C().with {
                assert test() == 'pre:1 post:0'
                assert prefix == 1
                assert postfix == 1
            }
        '''
    }

    // GROOVY-9978
    void testIncrementPropertyWithinClosure2() {
        assertScript '''
            import groovyx.gpars.dataflow.Dataflow

            class C {
                boolean stopProcessing = true
                int processedEvents = 0

                void test() {
                    def promise = Dataflow.task {
                        while (!stopProcessing) {
                            processedEvents++
                        }
                    }
                    // ...
                }
            }

            new C().test()
        '''
    }
}
