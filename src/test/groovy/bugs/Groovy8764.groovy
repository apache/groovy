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
package bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy8764 {

    @Test
    void testDgmMethodInClosureInAnonymousInnerClass() {
        assertScript '''
            import groovy.transform.*

            @CompileStatic
            class GroovyTest {
                public boolean hasMeta = false
                void test() {
                    new Runnable() {
                        void run() {
                            def c = { hasMeta = hasProperty('metaClass') /* implicit this for hasProperty DGM method */ }
                            c()
                        }
                    }.run()
                }
            }

            def gt = new GroovyTest()
            gt.test()
            assert gt.hasMeta
        '''
    }

    @Test
    void testDgmMethodInClosureInStaticInnerClass() {
        assertScript '''
            import groovy.transform.*
            import java.util.function.Function

            @CompileStatic
            class Outer {
                static class Inner {
                    List<Optional<String>> pets = [Optional.of('goldfish'), Optional.of('cat')]
                    Optional<Integer> test(int i) {
                        pets[i].flatMap({ String s ->
                            // sprintf is a DGM of Object
                            sprintf('%s', s).size() == 3 ? Optional.of(i) : Optional.<Integer>empty()
                        } as Function<String, Optional<Integer>>)
                    }
                }
            }

            def oi = new Outer.Inner()
            assert oi.test(0).isEmpty()
            assert oi.test(1).get() == 1
        '''
    }
}
