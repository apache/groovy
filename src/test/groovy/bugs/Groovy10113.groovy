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

final class Groovy10113 {

    @Test
    void testTypeParamCycle1() {
        def err = shouldFail '''
            class C<T extends T> {
            }
        '''
        assert err =~ /Cycle detected: the type T cannot extend.implement itself or one of its own member types/
    }

    @Test
    void testTypeParamCycle2() {
        def err = shouldFail '''
            class C<T extends U, U extends T> {
            }
        '''
        // TODO:                 ^ error is here but refers to T; is there a way to move the error or improve it
        assert err =~ /Cycle detected: the type T cannot extend.implement itself or one of its own member types/
    }

    @Test // GROOVY-10125, GROOVY-10288
    void testTypeParamNoCycle() {
        assertScript '''
            class C<T, U extends T> {
            }
            new C<Number,Integer>()
        '''
    }

    @Test
    void testInnerClassCycle1() {
        def err = shouldFail '''
            class C extends C.Inner {
                class Inner {
                }
            }
        '''
        assert err =~ /Cycle detected: the type C cannot extend.implement itself or one of its own member types/
    }

    @Test
    void testInnerClassCycle2() {
        def err = shouldFail '''
            class C extends D {
                class Inner {
                }
            }
            class D extends C.Inner {
            }
        '''
        assert err =~ /Cycle detected: a cycle exists in the type hierarchy between D and C/
    }

    @Test
    void testInnerInterfaceCycle1() {
        def err = shouldFail '''
            class C implements C.I {
                interface I {
                }
            }
        '''
        assert err =~ /Cycle detected: the type C cannot extend.implement itself or one of its own member types/
    }

    @Test
    void testInnerInterfaceCycle2() {
        def err = shouldFail '''
            class C extends D {
              interface I {
              }
            }
            class D implements C.I {
            }
        '''
        assert err =~ /Cycle detected: a cycle exists in the type hierarchy between D and C/
    }

    @Test
    void testClassExtendsInterfaceCycle() {
        def err = shouldFail '''
            interface A extends B {
            }
            class B extends A {
            }
        '''
        assert err =~ /Cycle detected: a cycle exists in the type hierarchy between B and A/
    }
}
