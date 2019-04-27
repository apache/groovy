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
package org.codehaus.groovy.transform

import java.lang.ref.SoftReference
import java.lang.reflect.Modifier

/**
 * Tests for the {@code @Lazy} transform.
 */
class LazyTransformTest extends GroovyShellTestCase {

    void testProp () {
        def res = evaluate("""
              class X {
                private List list = []

                List getList () {
                  [1,2,3]
                }

                List getInternalList () {
                  list
                }
              }

              new X ()
        """)

        assertEquals([1,2,3], res.list)
        assertEquals([], res.internalList)
    }

    void testNoInit() {
        def res = evaluate("""
              class X {
                @Lazy private ArrayList list

                void op () {
                  list << 1 << 2 << 3
                }
              }

              new X ()
        """)

        assertNull res.@'$list'
        res.op ()
        assertEquals([1,2,3], res.list)
    }

    void testInit() {
        def res = shell.evaluate("""
              class X {
                @Lazy private ArrayList list = [1,2,3]

                void op () {
                  list
                }
              }

              new X ()
        """)

        assertNull res.@'$list'
        res.op ()
        assertEquals([1,2,3], res.list)
    }

    void testInitCompileStatic() {
        def res = shell.evaluate("""
              @groovy.transform.CompileStatic
              class X {
                @Lazy private ArrayList list = [2,3,4]

                void op () {
                  list
                }
              }

              new X ()
        """)
        assertNull res.@'$list'
        res.op ()
        assertEquals([2,3,4], res.list)
    }

    void testInitWithClosure() {
        def res = evaluate("""
              class X {
                @Lazy private ArrayList list = { [1,2,3] } ()

                void op () {
                  list
                }
              }

              new X ()
        """)

        assertNull res.@'$list'
        res.op ()
        assertEquals([1,2,3], res.list)
    }

    void testStatic() {
        def res = evaluate("""
              class X {
                @Lazy static List list = { [1,2,3] } ()
              }
              new X ()
        """)

        assertNull res.@'$list'
        assert res.list == [1, 2, 3]
    }

    void testLazyPrimitivePromotedToWrapper() {
        def res = evaluate("""
              class X {
                @Lazy int val1 = 1
                @Lazy volatile int val2 = 2
              }
              new X ()
        """)

        assertNull res.@'$val1'
        assertNull res.@'$val2'
        assert res.val1 == 1
        assert res.val2 == 2
        assert res.class.getDeclaredField('$val1').type == Integer
        assert Modifier.isVolatile(res.class.getDeclaredField('$val2').modifiers)
    }

    void testAbstractClassShouldNotCompile() {
        def message = shouldFail {
            new GroovyShell().run('''
                abstract class Foo {}
                class Demo {
                    @Lazy Foo foo
                }

                new Demo().foo
            ''', 'dummyFileName', [])
        }
        assert message.contains("You cannot lazily initialize 'foo' from the abstract class 'Foo'")
    }

    void testSoft() {
        def res = evaluate("""
              class X {
                @Lazy(soft=true) private ArrayList list = { [1,2,3] } ()

                void op () {
                  list
                }
              }

              new X ()
        """)

        assertNull res.@'$list'
        res.op ()
        assertTrue res.@'$list' instanceof SoftReference
        assertEquals([1,2,3], res.list)
    }

    void testNestedLazyCalls() {
        def res = evaluate("""
            class X {
              @Lazy def smallSet = [1, 2, 3]
              @Lazy def biggerSet = (smallSet + [4, 5, 6])
            }
            new X().biggerSet
        """)
        assertEquals([1,2,3,4,5,6], res)
    }

    void testNestedStaticLazyCalls() {
        def res = evaluate("""
            class X {
              @Lazy static final SMALL_SET = [10, 20, 30]
              @Lazy static final BIGGER_SET = (SMALL_SET + [40, 50, 60])
            }
            X.BIGGER_SET
        """)
        assertEquals([10,20,30,40,50,60], res)
    }

    // GROOVY-7940
    void testGeneratesIsAndGetAccessorsForBooleanPrimitives() {
        assertScript '''
            class Super {
                boolean aBoolean = true
            }

            class Testing extends Super {
                @Lazy
                boolean aBoolean = {-> false }()
            }

            assert !new Testing().isaBoolean()
            assert !new Testing().getaBoolean()
            assert !new Testing().aBoolean
        '''
    }
}
