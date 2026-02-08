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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@link WithReadLock} and {@link WithWriteLock} AST transforms.
 */
final class ReadWriteLockTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            staticStar 'java.lang.reflect.Modifier'
            normal 'java.util.concurrent.locks.ReentrantReadWriteLock'
        }
    }

    @Test
    void testLockFieldDefaultsForReadLock() {
        assertScript shell, '''
            class C {
                @WithReadLock void m() { }
            }

            def field = C.getDeclaredField('$reentrantlock')
            assert field.type == ReentrantReadWriteLock
            assert !isTransient(field.modifiers)
            assert isPrivate(field.modifiers)
            assert !isStatic(field.modifiers)
            assert isFinal(field.modifiers)
        '''
    }

    @Test
    void testLockFieldDefaultsForWriteLock() {
        assertScript shell, '''
            class C {
                @WithWriteLock void m() { }
            }

            def field = C.getDeclaredField('$reentrantlock')
            assert field.type == ReentrantReadWriteLock
            assert !isTransient(field.modifiers)
            assert isPrivate(field.modifiers)
            assert !isStatic(field.modifiers)
            assert isFinal(field.modifiers)
        '''
    }

    @Test
    void testLockFieldDefaultsForStaticReadLock() {
        assertScript shell, '''
            class C {
                @WithReadLock static void m() { }
            }

            def field = C.getDeclaredField('$REENTRANTLOCK')
            assert field.type == ReentrantReadWriteLock
            assert !isTransient(field.modifiers)
            assert isPrivate(field.modifiers)
            assert isStatic(field.modifiers)
            assert isFinal(field.modifiers)
        '''
    }

    @Test
    void testLockFieldDefaultsForStaticWriteLock() {
        assertScript shell, '''
            class C {
                @WithWriteLock static void m() { }
            }

            def field = C.getDeclaredField('$REENTRANTLOCK')
            assert field.type == ReentrantReadWriteLock
            assert !isTransient(field.modifiers)
            assert isPrivate(field.modifiers)
            assert isStatic(field.modifiers)
            assert isFinal(field.modifiers)
        '''
    }

    @Test
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

    @Test
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

    @Test
    void testDeadlockingDoesNotOccur() {
        def tester = new MyClass()

        // this tests for deadlocks from not releasing in finally block
        shouldFail { tester.namedReaderMethod1() }
        shouldFail { tester.namedReaderMethod2() }
        shouldFail { tester.namedWriterMethod1() }
        shouldFail { tester.namedWriterMethod2() }

        shouldFail { tester.namedWriterMethod2() }
        shouldFail { tester.namedWriterMethod1() }
        shouldFail { tester.namedReaderMethod2() }
        shouldFail { tester.namedReaderMethod1() }
    }

    @Test
    void testCompileError_NamingConflict() {
        def err = shouldFail shell, '''
            class MyClass {
                @groovy.transform.WithWriteLock('unknown')
                public static void readerMethod1() { }
            }
        '''
        assert err =~ /lock field with name 'unknown' not found/

        err = shouldFail shell, '''
            class MyClass {
                def myLock = new ReentrantReadWriteLock()

                @groovy.transform.WithWriteLock('myLock')
                public static void readerMethod1() { }
            }
        '''
        assert err =~ /lock field with name 'myLock' should be static/

        err = shouldFail shell, '''
            class MyClass {
                static def myLock = new ReentrantReadWriteLock()

                @groovy.transform.WithWriteLock('myLock')
                public void readerMethod1() { }
            }
        '''
        assert err =~ /lock field with name 'myLock' should not be static/
    }

    @Test // GROOVY-8758
    void testShouldBeAllowedInInnerClassWithCompileStatic() {
        assertScript shell, '''
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

    //--------------------------------------------------------------------------

    static class MyClass {
        def readerMethod1Called = false
        def readerMethod2Called = false
        def writerMethod1Called = false
        def writerMethod2Called = false
        def staticReaderMethod1Called = false
        def staticReaderMethod2Called = false
        def staticWriterMethod1Called = false
        def staticWriterMethod2Called = false
        def myLock = new java.util.concurrent.locks.ReentrantReadWriteLock()

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
}
