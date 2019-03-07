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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.ArraysAndCollectionsSTCTest

/**
 * Unit tests for static type checking : miscellaneous tests.
 */
class ArraysAndCollectionsStaticCompileTest extends ArraysAndCollectionsSTCTest implements StaticCompilationTestSupport {

    void testListStarWithMethodReturningVoid() {
        assertScript '''
            class A { void m() {} }
            List<A> elems = [new A(), new A(), new A()]
            List result = elems*.m()
            assert result == [null,null,null]
        '''
    }

    void testListStarWithMethodWithNullInList() {
        assertScript '''
            List<String> elems = ['a',(String)null,'C']
            List<String> result = elems*.toUpperCase()
            assert result == ['A',null,'C']
        '''
    }

    void testShouldNotThrowVerifyError() {
        assertScript '''
            def al = new ArrayList<Double>()
            al.add(2.0d)
            assert al.get(0) + 1 == 3.0d
        '''
    }

    // GROOVY-5654
    void testShouldNotThrowForbiddenAccessWithMapProperty() {
        assertScript '''
            Map<String, Integer> m = ['abcd': 1234]
            assert m['abcd'] == 1234
            assert m.abcd == 1234
        '''
    }

    // GROOVY-5988
    void testMapArraySetPropertyAssignment() {
        assertScript '''
            Map<String, Object> props(Object p) {
                Map<String, Object> props = [:]

                for(String property in p.properties.keySet()){
                    props[property] = 'TEST'
                    // I need to use calling put directy to make it work
                    // props.put property, 'TEST'
                }
                props
            }
            def map = props('SOME RANDOM STRING')
            assert map['class'] == 'TEST'
            assert map['bytes'] == 'TEST'
        '''
    }

    // GROOVY-7656
    void testSpreadSafeMethodCallsOnListLiteralShouldNotCreateListTwice() {
        try {
            assertScript '''
                class Foo {
                    static void test() {
                        def list = [1, 2]
                        def lengths = [list << 3]*.size()
                        assert lengths == [3]
                        assert list == [1, 2, 3]
                    }
                }
                Foo.test()
            '''
        } finally {
            assert astTrees['Foo'][1].count('ScriptBytecodeAdapter.createList') == 4
        }
    }

    //GROOVY-7442
    void testSpreadDotOperatorWithinAssert() {
        assertScript '''
            def myMethod(String a, String b) {
                assert [a, b]*.size() == [5, 5]
            }

            myMethod('hello', 'world')
        '''
    }

    //GROOVY-7688
    void testSpreadSafeMethodCallReceiversWithSideEffectsShouldNotBeVisitedTwice() {
        try {
            assertScript '''
                class Foo {
                    static void test() {
                        def list = ['a', 'b']
                        def lengths = list.toList()*.length()
                        assert lengths == [1, 1]
                    }
                }
                Foo.test()
            '''
        } finally {
            assert astTrees['Foo'][1].count('DefaultGroovyMethods.toList') == 1
        }
    }

    //GROOVY-8074
    void testMapSubclassPropertyStyleAccess() {
        assertScript '''
            class MyMap extends LinkedHashMap {
                def foo = 1
            }
        
            def map = new MyMap()
            map.put('foo', 42)
            assert map.foo == 42               
        '''
    }

    @Override
    void testForInLoop() {
        try {
            super.testForInLoop()
        } finally {
            println astTrees
        }
    }
}

