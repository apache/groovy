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
package groovy.transform

import groovy.test.GroovyTestCase

import java.util.concurrent.locks.ReentrantReadWriteLock
import java.lang.reflect.Modifier

/**
 * Unit test for WithReadLock and WithWriteLock annotations.
 */
class ReadWriteLockTest extends GroovyTestCase {

    void testLockFieldDefaultsForReadLock() {
        def tester = new GroovyClassLoader().parseClass('''
        class MyClass {
            @groovy.transform.WithReadLock
            public void readerMethod1() { }
        }
''')
        def field = tester.getDeclaredField('$reentrantlock')
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isTransient(field.modifiers)
        assert Modifier.isFinal(field.modifiers)
        assert !Modifier.isStatic(field.modifiers)

        assert field.type == ReentrantReadWriteLock
    }

    void testLockFieldDefaultsForWriteLock() {
        def tester = new GroovyClassLoader().parseClass('''
        class MyClass {
            @groovy.transform.WithWriteLock
            public void readerMethod1() { }
        }
''')
        def field = tester.getDeclaredField('$reentrantlock')
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isTransient(field.modifiers)
        assert Modifier.isFinal(field.modifiers)
        assert !Modifier.isStatic(field.modifiers)

        assert field.type == ReentrantReadWriteLock
    }

    void testLockFieldDefaultsForStaticReadLock() {
        def tester = new GroovyClassLoader().parseClass('''
        class MyClass {
            @groovy.transform.WithReadLock
            public static void readerMethod1() { }
        }
''')
        def field = tester.getDeclaredField('$REENTRANTLOCK')
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isTransient(field.modifiers)
        assert Modifier.isFinal(field.modifiers)
        assert Modifier.isStatic(field.modifiers)

        assert field.type == ReentrantReadWriteLock
    }

    void testLockFieldDefaultsForStaticWriteLock() {
        def tester = new GroovyClassLoader().parseClass('''
        class MyClass {
            @groovy.transform.WithWriteLock
            public static void readerMethod1() { }
        }
''')
        def field = tester.getDeclaredField('$REENTRANTLOCK')
        assert Modifier.isPrivate(field.modifiers)
        assert !Modifier.isTransient(field.modifiers)
        assert Modifier.isFinal(field.modifiers)
        assert Modifier.isStatic(field.modifiers)

        assert field.type == ReentrantReadWriteLock
    }

    void testLocking() {

        def tester = new MyClass()
        tester.readerMethod1()
        tester.readerMethod2()
        assert tester.readerMethod1Called
        assert tester.readerMethod2Called

        tester.writerMethod1()
        tester.writerMethod2()
        assert tester.writerMethod1Called
        assert tester.writerMethod2Called

        tester.writerMethod2()
        tester.writerMethod1()
        tester.readerMethod2()
        tester.readerMethod1()
    }

    void testStaticLocking() {

        def tester = new MyClass()
        tester.staticReaderMethod1()
        tester.staticReaderMethod2()
        assert tester.staticReaderMethod1Called
        assert tester.staticReaderMethod2Called

        tester.staticWriterMethod1()
        tester.staticWriterMethod2()
        assert tester.staticWriterMethod1Called
        assert tester.staticWriterMethod2Called

        tester.staticWriterMethod2()
        tester.staticWriterMethod1()
        tester.staticReaderMethod2()
        tester.staticReaderMethod1()
    }

    void testDeadlockingDoesNotOccur() {
        def tester = new MyClass()

        // this tests for deadlocks from not releaseing in finally block 
        shouldFail { tester.namedReaderMethod1() }
        shouldFail { tester.namedReaderMethod2() }
        shouldFail { tester.namedWriterMethod1() }
        shouldFail { tester.namedWriterMethod2() }

        shouldFail { tester.namedWriterMethod2() }
        shouldFail { tester.namedWriterMethod1() }
        shouldFail { tester.namedReaderMethod2() }
        shouldFail { tester.namedReaderMethod1() }
    }

    void testCompileError_NamingConflict() {
        shouldFail("lock field with name 'unknown' not found") {
            '''
            class MyClass {
                @groovy.transform.WithWriteLock('unknown')
                public static void readerMethod1() { }
            } '''
        }

        shouldFail("lock field with name 'myLock' should be static") {
            '''
            class MyClass {
                def myLock = new java.util.concurrent.locks.ReentrantReadWriteLock()

                @groovy.transform.WithWriteLock('myLock')
                public static void readerMethod1() { }
            } '''
        }

        shouldFail("lock field with name 'myLock' should not be static") {
            '''
            class MyClass {
                static def myLock = new java.util.concurrent.locks.ReentrantReadWriteLock()

                @groovy.transform.WithWriteLock('myLock')
                public void readerMethod1() { }
            } '''
        }
    }

    // GROOVY-8758
    void testShouldBeAllowedInInnerClassWithCompileStatic() {
        assertScript '''
            import groovy.transform.*

            @CompileStatic
            class A {
                private class B {
                    @WithReadLock
                    int getFoo() { 0 }
                }

                private B b

                A() {
                    b = new B()
                }
            }

            def a = new A()
        '''
    }

    def shouldFail(String expectedText, Closure c) {
        String script = c()
        try {
            new GroovyClassLoader().parseClass(script)
            fail('Failure Expected')
        } catch (Exception e) {
            assert e.getMessage().contains(expectedText)
        }
    }
}

class MyClass {

    def readerMethod1Called = false
    def readerMethod2Called = false
    def writerMethod1Called = false
    def writerMethod2Called = false
    def staticReaderMethod1Called = false
    def staticReaderMethod2Called = false
    def staticWriterMethod1Called = false
    def staticWriterMethod2Called = false
    def myLock = new ReentrantReadWriteLock()
    
    @WithReadLock
    void readerMethod1() {
        readerMethod1Called = true
    }
    @WithReadLock
    void readerMethod2() {
        readerMethod2Called = true
    }
    @WithWriteLock
    void writerMethod1() {
        writerMethod1Called = true
    }
    @WithWriteLock
    void writerMethod2() {
        writerMethod2Called = true
    }

    @WithReadLock('myLock')
    void namedReaderMethod1() {
        throw new Exception()
    }
    @WithReadLock('myLock')
    void namedReaderMethod2() {
        throw new Exception()
    }
    @WithWriteLock('myLock')
    void namedWriterMethod1() {
        throw new Exception()
    }
    @WithWriteLock('myLock')
    void namedWriterMethod2() {
        throw new Exception()
    }

    @WithReadLock
    void staticReaderMethod1() {
        staticReaderMethod1Called = true
    }
    @WithReadLock
    void staticReaderMethod2() {
        staticReaderMethod2Called = true
    }
    @WithWriteLock
    void staticWriterMethod1() {
        staticWriterMethod1Called = true
    }
    @WithWriteLock
    void staticWriterMethod2() {
        staticWriterMethod2Called = true
    }
}
