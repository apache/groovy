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
package gls.generics

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class GenericsUsageOrderingTest {

    @Test
    void testGroovy6167() {
        assertScript '''
            class Foo<T extends List<X>, X extends Number> {
            }
            assert true
        '''
    }

    @Test
    void testIncompatibleType1() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo<T extends List<X>, X extends Number> {
                static main(args) {
                    def f = new Foo<ArrayList<String>, String>()
                }
            }
        '''
        assert err.message.contains('The type String is not a valid substitute for the bounded parameter <X extends java.lang.Number>')
    }

    @Test
    void testIncompatibleType2() {
        def err = shouldFail '''
            @groovy.transform.CompileStatic
            class Foo<T extends List<X>, X extends Number> {
                static main(args) {
                    def f = new Foo<HashSet<Integer>, Integer>()
                }
            }
        '''
        assert err.message.contains('The type HashSet is not a valid substitute for the bounded parameter <T extends java.util.List<X>>')
    }

    @Test
    void testParameter() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo<T extends List<X>, X extends Number> {
                X getFirstElement(T t) {
                    X x = t.get(0)
                    return x
                }

                static main(args) {
                    def f = new Foo<ArrayList<Integer>, Integer>()
                    def list = new ArrayList<Integer>()
                    list.add(123)
                    assert f.getFirstElement(list) == 123
                }
            }
        '''
    }

    @Test
    void testVariable() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo<T extends List<X>, X extends Number> {
                X getFirstElement() {
                    def list = new ArrayList<Integer>()
                    list.add(123)
                    T t = list
                    X x = t.get(0)
                    return x
                }

                static main(args) {
                    def f = new Foo<ArrayList<Integer>, Integer>()
                    assert f.getFirstElement() == 123
                }
            }
        '''
    }

    @Test
    void testField() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo<T extends List<X>, X extends Number> {
                T t

                {
                    def list = new ArrayList<Integer>()
                    list.add(123)
                    t = list
                }

                X getFirstElement() {
                    X x = t.get(0)
                    return x
                }

                static main(args) {
                    def f = new Foo<ArrayList<Integer>, Integer>()
                    assert f.getFirstElement() == 123
                }
            }
        '''
    }

    @Test
    void testParameter2() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo<T extends List<X>, X extends Number> {
                X getFirstElement(List<X> list) {
                    X x = list.get(0)
                    assert Number == x.getClass().getGenericSuperclass()
                    return x
                }

                Number getFirstNumber(T t) {
                    return getFirstElement(t)
                }

                static main(args) {
                    def f = new Foo<ArrayList<Integer>, Integer>()
                    def list = new ArrayList<Integer>()
                    list.add(123)
                    assert f.getFirstNumber(list) == 123
                }
            }
        '''
    }

    @Test
    void testParameterAndVariable() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo<T extends List<X>, X extends Number> {
                X getFirstElement(List<X> t) {
                    X x = t.get(0)
                    return x
                }

                static main(args) {
                    def f = new Foo<ArrayList<Integer>, Integer>()
                    def list = new ArrayList<Integer>()
                    list.add(123)
                    assert f.getFirstElement(list) == 123
                }
            }
        '''
    }
}
