/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform

import static org.junit.Assert.*
import groovy.transform.Memoized

import org.codehaus.groovy.ast.FieldNode

/**
 * Unit tests for {@link MemoizedASTTransformation}.
 * 
 * @author Andrey Bloschetsov
 */
class MemoizedASTTransformationTest extends GroovyTestCase {

    void testMethodWithoutParams() {
        MemoizedTestClass ins = new MemoizedTestClass()
        assertEquals(ins.methodWithoutParams(), 123)
        assertEquals(ins.methodCallCounter, 1)
        assertEquals(ins.methodWithoutParams(), 123)
        assertEquals(ins.methodCallCounter, 1)
        assertEquals(ins.methodWithoutParams(), 123)
        assertEquals(ins.methodCallCounter, 1)
    }

    void testMethodWithParams() {
        MemoizedTestClass ins = new MemoizedTestClass()
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

    void testPrivateMethodWithParams() {
        MemoizedTestClass ins = new MemoizedTestClass()
        assertEquals(ins.privateMethodWithParams('hello ', 'world'), 'hello world')
        assertEquals(ins.methodCallCounter, 1)
        assertEquals(ins.privateMethodWithParams('foo', 'bar'), 'foobar')
        assertEquals(ins.methodCallCounter, 2)
        assertEquals(ins.privateMethodWithParams('hello ', 'world'), 'hello world')
        assertEquals(ins.methodCallCounter, 2)
        assertEquals(ins.privateMethodWithParams('foo', 'bar'), 'foobar')
        assertEquals(ins.methodCallCounter, 2)
    }
    
    void testMethodWithException() {
        MemoizedTestClass ins = new MemoizedTestClass()
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
    
    void testConflictName() {
        MemoizedTestClass ins = new MemoizedTestClass()
        assertEquals(ins.conflictName(), 'No conflict')
        assertEquals(ins.methodCallCounter, 1)
        assertEquals(ins.conflictName(), 'No conflict')
        assertEquals(ins.methodCallCounter, 1)
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