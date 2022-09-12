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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy8310 {

    @Test
    void testClosureReturnType1() {
        def err = shouldFail '''
            def bar(Closure<Collection<Integer>> block) {
                block()
            }

            @groovy.transform.CompileStatic
            def foo() {
                bar {
                    [1]
                }
            }
        '''
        assert err =~ /Cannot find matching method \w+#bar\(groovy.lang.Closure <java.util.List>\)/
    }

    @Test
    void testClosureReturnType2() {
        def err = shouldFail '''
            def <T> T bar(Closure<Collection<Integer>> block) {
                block()
            }

            @groovy.transform.CompileStatic
            def foo() {
                bar {
                    [1]
                }
            }
        '''
        assert err =~ /Cannot call <T> \w+#bar\(groovy.lang.Closure <java.util.Collection>\) with arguments \[groovy.lang.Closure <java.util.List>\]/
    }

    @Test
    void testClosureReturnType3() {
        assertScript '''
            def <T> T bar(Closure<? extends Collection<Integer>> block) {
                block()
            }

            @groovy.transform.CompileStatic
            def foo() {
                bar {
                    [1]
                }
            }

            assert foo() == [1]
        '''
    }

    @Test
    void testClosureReturnType4() {
        assertScript '''
            def <T> T bar(Closure<Collection<Integer>> block) {
                block()
            }

            @groovy.transform.CompileStatic
            def foo() {
                bar {
                    (Collection<Integer>) [1]
                }
            }

            assert foo() == [1]
        '''
    }

    @Test
    void testClosureReturnType5() {
        assertScript '''
            def <T> T bar(Closure<Collection<Integer>> block) {
                block()
            }

            def foo() {
                bar {
                    [1] as Collection<Integer>
                }
            }

            assert foo() == [1]
        '''
    }
}
