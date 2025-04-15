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
package gls.statements

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class MultipleAssignmentDeclarationTest {

    @Test
    void testDef1() {
        assertScript '''
            def (a,b) = [1,2]
            assert a == 1
            assert b == 2
        '''
    }

    @Test
    void testDef2() {
        assertScript '''
            def list = [1,2]
            def (c,d) = list
            assert c == 1
            assert d == 2
        '''
    }

    @Test
    void testMixedTypes() {
        assertScript '''
            def x = "foo"
            def (int i, String j) = [1,x]
            assert x == "foo"
            assert i == 1
            assert i instanceof Integer
            assert j == "foo"
            assert j instanceof String
        '''
    }

    @Test
    void testMixedTypesWithConversion() {
        assertScript '''
            def x = "foo"
            def (int i, String j) = [1,"$x $x"]
            assert x == "foo"
            assert i == 1
            assert i instanceof Integer
            assert j == "foo foo"
            assert j instanceof String
        '''
    }

    @Test
    void testDeclarationOrder() {
        shouldFail MissingPropertyException, '''
            def (i,j) = [1,i]
        '''
    }

    @Test
    void testNestedScope1() {
        assertScript '''import static groovy.test.GroovyAssert.shouldFail

            def c = { ->
                def (i,j) = [1,2]
                assert i == 1
                assert j == 2
            }
            c()

            shouldFail(MissingPropertyException) {
                println i
            }

            shouldFail(MissingPropertyException) {
                println j
            }

            def (i,j) = [2,3]
            assert i == 2
            assert j == 3
            c()

            assert i == 2
            assert j == 3
        '''
    }

    @Test
    void testNestedScope2() {
        assertScript '''
            class C {
                int m() {
                    def (i,j) = [1,2]
                    assert i == 1
                    assert j == 2

                    def x = { ->
                        assert i == 1
                        assert j == 2

                        i = 3
                        assert i == 3
                    }
                    x()

                    assert i == 3
                    return j
                }
            }
            int n = new C().m()
            assert n == 2
        '''
    }

    @Test
    void testMultiAssignChain() {
        assertScript '''
            def a,b
            def (c,d) = (a,b) = [1,2]
            assert [a,b] == [1,2]
            assert [c,d] == [1,2]
        '''
    }

    @Test
    void testMultiAssignFromObject() {
        shouldFail MissingMethodException, '''
            def obj = new Object()
            def (x) = obj
        '''
    }

    @Test
    void testMultiAssignFromCalendar() {
        assertScript '''
            def (_, y, m) = Calendar.instance
            assert y >= 2022
            assert m in 0..11
        '''
    }

    @Test // GROOVY-5744
    void testMultiAssignFromIterator() {
        assertScript '''
            def list = [1,2,3]
            def iter = list.iterator()

            def (a,b,c) = list
            def (d,e,f) = iter
            assert "$a $b $c" == "$d $e $f"
        '''
    }

    @Test // GROOVY-10666
    void testMultiAssignFromIterable() {
        assertScript '''
            class MySet {
                List<String> ops = []
                @Delegate Set<String> strings = []

                String getAt(int index) {
                    ops << "getAt($index)".toString()
                    org.codehaus.groovy.runtime.DefaultGroovyMethods.getAt(this, index)
                }

                Iterator<String> iterator() {
                    ops << "iterator()"
                    def iterator = strings.iterator()
                    return new Iterator<String>() {
                        @Override
                        boolean hasNext() {
                            iterator.hasNext()
                        }
                        @Override
                        String next() {
                            ops << "next()"
                            iterator.next()
                        }
                    }
                }
            }

            Set<String> strings = new MySet()
            strings << 'foo'
            strings << 'bar'
            strings << 'baz'

            def (foo,bar,baz) = strings
            assert foo == 'foo'
            assert bar == 'bar'
            assert baz == 'baz'

            assert strings.ops == ['iterator()','next()','next()','next()']
        '''
    }

    @Test // GROOVY-10666
    void testMultiAssignFromJavaStream() {
        assertScript '''import java.util.stream.Stream

            Stream<String> strings = Stream.of('foo','bar','baz')
            def (foo,bar,baz) = strings
            assert foo == 'foo'
            assert bar == 'bar'
            assert baz == 'baz'
        '''
    }
}
