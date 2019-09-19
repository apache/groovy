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

import groovy.test.GroovyTestCase

class Groovy8383Bug extends GroovyTestCase {
    void testCompileStaticWithOptimizedConstants() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                private static String $const$0 = 'xyzzy'

                def method() {
                    Long wrapper1 = 8L
                    long prim1 = 8L
                    long prim2 = 8L
                    long prim3 = 9L
                    Long wrapper2 = 9L
                    wrapper1 + prim1 + prim2 + prim3 + wrapper2
                }
            }
            assert new Foo().method() == 42

            class Bar {
                private static long $const$0 = 9
                private static long $const$2 = 10
                def method2() {
                    long prim4 = 11L
                    long prim5 = 12L
                    prim4 + prim5 + $const$0 + $const$2
                }
            }
            assert new Bar().method2() == 42
        '''
    }
}
