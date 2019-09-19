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

import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic
import groovy.transform.Memoized

import static org.junit.Assert.*

/**
 * Unit tests for {@link MemoizedASTTransformation}.
 */
class MemoizedASTTransformationTest extends GroovyTestCase {

    void testMethodWithoutParams() {
        [new MemoizedTestClass(), new MemoizedTestClass3()].each {
            def ins = new MemoizedTestClass3()
            assertEquals(ins.methodWithoutParams(), 123)
            assertEquals(ins.methodCallCounter, 1)
            assertEquals(ins.methodWithoutParams(), 123)
            assertEquals(ins.methodCallCounter, 1)
            assertEquals(ins.methodWithoutParams(), 123)
            assertEquals(ins.methodCallCounter, 1)
        }
    }

    void testMethodWithParams() {
        [new MemoizedTestClass(), new MemoizedTestClass3()].each {
            def ins = new MemoizedTestClass3()
            assertEquals(ins.methodWithParams(1, 2), 3)
            assertEquals(ins.methodCallCounter, 1)
            assertEquals(ins.methodWithParams(1, 2), 3)
            assertEquals(ins.methodCallCounter, 1)
            assertEquals(ins.methodWithParams(5, 4), 9)
            assertEquals(ins.methodCallCounter, 2)
            assertEquals(ins.methodWithParams(1, 2), 3)
            assertEquals(ins.methodCallCounter, 2)
            assertEquals(ins.methodWithParams(5, 4), 9)
            assertEquals(ins.methodCallCounter, 2)
        }
    }

    void testPrivateMethodWithParams() {
        [new MemoizedTestClass(), new MemoizedTestClass3()].each {
            def ins = new MemoizedTestClass3()
            assertEquals(ins.privateMethodWithParams('hello ', 'world'), 'hello world')
            assertEquals(ins.methodCallCounter, 1)
            assertEquals(ins.privateMethodWithParams('foo', 'bar'), 'foobar')
            assertEquals(ins.methodCallCounter, 2)
            assertEquals(ins.privateMethodWithParams('hello ', 'world'), 'hello world')
            assertEquals(ins.methodCallCounter, 2)
            assertEquals(ins.privateMethodWithParams('foo', 'bar'), 'foobar')
            assertEquals(ins.methodCallCounter, 2)
        }
    }

    void testMethodWithException() {
        [new MemoizedTestClass(), new MemoizedTestClass3()].each {
            def ins = new MemoizedTestClass3()
            assertEquals(ins.methodWithException('Jimmy'), 'hello Jimmy')
            assertEquals(ins.methodCallCounter, 1)
            try {
                ins.methodWithException(null)
                fail()
            } catch (IllegalArgumentException e) {
            }
            assertEquals(ins.methodCallCounter, 1)
            try {
                ins.methodWithException(null)
                fail()
            } catch (IllegalArgumentException e) {
            }
            assertEquals(ins.methodCallCounter, 1)
            assertEquals(ins.methodWithException('Jimmy'), 'hello Jimmy')
            assertEquals(ins.methodCallCounter, 1)
            assertEquals(ins.methodWithException('Tom'), 'hello Tom')
            assertEquals(ins.methodCallCounter, 2)
        }
    }

    void testConflictName() {
        [new MemoizedTestClass(), new MemoizedTestClass3()].each {
            def ins = new MemoizedTestClass3()
            assertEquals(ins.conflictName(), 'No conflict')
            assertEquals(ins.methodCallCounter, 1)
            assertEquals(ins.conflictName(), 'No conflict')
            assertEquals(ins.methodCallCounter, 1)
        }
    }

    void testMaxCacheSize() {
        MemoizedTestClass2 ins = new MemoizedTestClass2()
        assertTrue(ins.getValue('prop1') == 'prop1_value')
        assertEquals(ins.counter, 1)
        assertTrue(ins.getValue('prop1') == 'prop1_value')
        assertEquals(ins.counter, 1)
        assertTrue(ins.getValue('prop2') == 'prop2_value')
        assertEquals(ins.counter, 2)
        assertTrue(ins.getValue('prop2') == 'prop2_value')
        assertEquals(ins.counter, 2)
        assertTrue(ins.getValue('prop3') == 'prop3_value')
        assertEquals(ins.counter, 3)
        assertTrue(ins.getValue('prop3') == 'prop3_value')
        assertEquals(ins.counter, 3)
        assertTrue(ins.getValue('prop1') == 'prop1_value')
        assertEquals(ins.counter, 4)
    }

    void testProtectedCacheSize() {
        // Test the only method call
        MemoizedTestClass2 ins = new MemoizedTestClass2()
        assertTrue(ins.getValue2('prop1') == 'prop1_value')
        assertEquals(ins.counter, 1)
        assertTrue(ins.getValue2('prop2') == 'prop2_value')
        assertEquals(ins.counter, 2)
    }

    void testProtectedCacheSizeMaxCacheSize() {
        // Test the only method call
        MemoizedTestClass2 ins = new MemoizedTestClass2()
        assertTrue(ins.getValue3('prop1') == 'prop1_value')
        assertEquals(ins.counter, 1)
        assertTrue(ins.getValue3('prop2') == 'prop2_value')
        assertEquals(ins.counter, 2)
    }

    void testInheritedMemoizedMethod() {
        def ins = new SubClassOfMemoizedTestClass()
        assertEquals(ins.methodWithoutParams(), 123)
    }

    void testVarargsMemoizedMethod() {
        def ins = new MemoizedTestClassWithVarargs()
        assertEquals(ins.getValue(12, 'foo', 'boo'), 'foo')
    }

    void testMultiDimensionalArrayMemoizedMethod() {
        def ins = new MemoizedTestClassWithMultiDimensionalArray()
        assertEquals(ins.getValue(new String[2][2]), 'foo')
    }

    // -- static methods -- //

    void testStaticMethodWithoutParams() {
        assertEquals(MemoizedTestClass.staticMethodWithoutParams(), 'foo')
        assertEquals(MemoizedTestClass.staticMethodWithoutParamsCounter, 1)
        assertEquals(MemoizedTestClass.staticMethodWithoutParams(), 'foo')
        assertEquals(MemoizedTestClass.staticMethodWithoutParamsCounter, 1)
    }

    void testStaticMethodWithParams() {
        assertEquals(MemoizedTestClass.staticMethodWithParams(2, 'foo'), 'foofoo')
        assertEquals(MemoizedTestClass.staticMethodWithParamsCounter, 1)
        assertEquals(MemoizedTestClass.staticMethodWithParams(3, 'bar'), 'barbarbar')
        assertEquals(MemoizedTestClass.staticMethodWithParamsCounter, 2)
        assertEquals(MemoizedTestClass.staticMethodWithParams(3, 'bar'), 'barbarbar')
        assertEquals(MemoizedTestClass.staticMethodWithParamsCounter, 2)
        assertEquals(MemoizedTestClass.staticMethodWithParams(2, 'foo'), 'foofoo')
        assertEquals(MemoizedTestClass.staticMethodWithParamsCounter, 2)
    }

    void testPrivateStaticMethodWithParams() {
        assertEquals(MemoizedTestClass.privateStaticMethodWithParams(20, 5), 15)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParamsCounter, 1)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParams(20, 5), 15)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParamsCounter, 1)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParams(12, 4), 8)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParamsCounter, 2)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParams(12, 5), 7)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParamsCounter, 3)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParams(12, 4), 8)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParamsCounter, 3)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParams(12, 5), 7)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParamsCounter, 3)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParams(20, 5), 15)
        assertEquals(MemoizedTestClass.privateStaticMethodWithParamsCounter, 3)
    }

    void testMemoizedAIC_Groovy8043() {
        assertScript '''
            class A {}
            assert new A() {
                @groovy.transform.Memoized()
                String a() {
                    return "a"
                }
            }.a() == 'a'
        '''
    }

    // GROOVY-8326
    void testMemoizedMethodWithOverrideAnnotation() {
        assertScript '''
            class A {            
                @groovy.transform.Memoized
                @Override
                String toString() {
                    'an expensive toString operation'
                }
            }
            assert new A().toString() == 'an expensive toString operation'
        '''
    }

    // GROOVY-8367
    void testMemoizedInInnerClass() {
        assertScript '''
            import groovy.transform.Memoized
            class Outer {
                private static int count = 0
                private static class Inner {
                    @Memoized
                    static calc(int arg1, int arg2) { count++; arg1 + arg2 }
                }
            }
            assert Outer.Inner.calc(3, 4) + Outer.Inner.calc(3, 4) + Outer.count == 15
        '''
    }
}

class MemoizedTestClass2 {

    int counter

    @Memoized(maxCacheSize = 2)
    String getValue(String name) {
        counter++
        name + "_value"
    }

    @Memoized(protectedCacheSize = 2)
    String getValue2(String name) {
        counter++
        name + "_value"
    }

    @Memoized(protectedCacheSize = 2, maxCacheSize = 3)
    String getValue3(String name) {
        counter++
        name + "_value"
    }
}

class SubClassOfMemoizedTestClass extends MemoizedTestClass {
}

class MemoizedTestClass {

    int methodCallCounter

    @Memoized
    int methodWithoutParams() {
        methodCallCounter++
        123
    }

    @Memoized
    int methodWithParams(int n1, int n2) {
        methodCallCounter++
        n1 + n2
    }

    @Memoized
    private String privateMethodWithParams(String s1, String s2) {
        methodCallCounter++
        s1 + s2
    }

    @Memoized
    String methodWithException(String name) {
        if (!name) {
            throw new IllegalArgumentException()
        }
        methodCallCounter++
        'hello ' + name
    }

    String memoizedMethodClosure$conflictName = null // Field for check a name conflict

    @Memoized
    String conflictName() {
        methodCallCounter++
        "No conflict"
    }

    // -- static methods -- //

    static int staticMethodWithoutParamsCounter

    @Memoized
    static String staticMethodWithoutParams() {
        staticMethodWithoutParamsCounter++
        'foo'
    }

    static int staticMethodWithParamsCounter

    @Memoized
    static String staticMethodWithParams(int n, String str) {
        staticMethodWithParamsCounter++
        str * n
    }

    static int privateStaticMethodWithParamsCounter

    @Memoized
    private static long privateStaticMethodWithParams(long n, long m) {
        privateStaticMethodWithParamsCounter++
        n - m
    }
}

class MemoizedTestClassWithVarargs {
    @Memoized
    String getValue(Integer inp, ... params) {
        'foo'
    }
}

class MemoizedTestClassWithMultiDimensionalArray {
    @Memoized
    String getValue(Object[][] params) {
        'foo'
    }
}

@CompileStatic
class MemoizedTestClass3 {
    int methodCallCounter

    @Memoized
    int methodWithoutParams() {
        methodCallCounter++
        123
    }

    @Memoized
    int methodWithParams(int n1, int n2) {
        methodCallCounter++
        n1 + n2
    }

    @Memoized
    private String privateMethodWithParams(String s1, String s2) {
        methodCallCounter++
        s1 + s2
    }

    @Memoized
    String methodWithException(String name) {
        if (!name) {
            throw new IllegalArgumentException()
        }
        methodCallCounter++
        'hello ' + name
    }

    String memoizedMethodClosure$conflictName = null // Field for check a name conflict

    @Memoized
    String conflictName() {
        methodCallCounter++
        "No conflict"
    }

    // -- static methods -- //

    static int staticMethodWithoutParamsCounter

    @Memoized
    static String staticMethodWithoutParams() {
        staticMethodWithoutParamsCounter++
        'foo'
    }

    static int staticMethodWithParamsCounter

    @Memoized
    static String staticMethodWithParams(int n, String str) {
        staticMethodWithParamsCounter++
        str * n
    }

    static int privateStaticMethodWithParamsCounter

    @Memoized
    private static long privateStaticMethodWithParams(long n, long m) {
        privateStaticMethodWithParamsCounter++
        n - m
    }
}