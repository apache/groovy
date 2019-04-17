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

import groovy.mock.interceptor.StubFor
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.transform.ThreadInterruptibleASTTransformation
import org.junit.After
import org.junit.Before
import org.junit.Test

import java.lang.reflect.Modifier

import static groovy.test.GroovyAssert.isAtLeastJdk
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assume.assumeTrue

/**
 * Test for @ThreadInterrupt.
 */
class ThreadInterruptTest {
    private static final boolean jdk12plus = isAtLeastJdk('12.0')
    private Map<String, MethodNode> oldValues = [:]

    @After
    void tearDown() {
        if (jdk12plus) return
        Thread.metaClass = null
        ['CURRENTTHREAD_METHOD', 'ISINTERRUPTED_METHOD'].each {
            def ov = ThreadInterruptibleASTTransformation.getDeclaredField(it)
            def modifiersField = ov.class.getDeclaredField("modifiers")
            modifiersField.accessible = true
            modifiersField.setInt(ov, ov.modifiers & ~Modifier.FINAL)
            ov.accessible = true
            ov.set(ThreadInterruptibleASTTransformation, oldValues[it])
        }
    }

    @Before
    void setUp() throws Exception {
        // JDK12+ doesn't allow adjusting static final fields even via reflection, so
        // skip all tests on such JDK versions - it is only test code that's affected
        // and currently we have coverage from builds with lower JDK versions.
        assumeTrue !jdk12plus

        ['CURRENTTHREAD_METHOD', 'ISINTERRUPTED_METHOD'].each {
            def ov = ThreadInterruptibleASTTransformation.getDeclaredField(it)
            def modifiersField = ov.class.getDeclaredField("modifiers")
            modifiersField.accessible = true
            modifiersField.setInt(ov, ov.modifiers & ~Modifier.FINAL)
            ov.accessible = true
            oldValues[it] = ov.get(ThreadInterruptibleASTTransformation)
            ov.set(ThreadInterruptibleASTTransformation, null)
        }
    }

    @Test
    void testDefaultParameters_Method() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() { }
            }
        """)

        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            c.newInstance().myMethod()
        }
        assert 1 == counter.interruptedCheckCount
    }

    @Test
    void testNoMethodCheck_Method() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt(checkOnMethodStart = false)
            class MyClass {
              def myMethod() { }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            c.newInstance().myMethod()
        }
        // no exception means success
    }

    @Test
    void testDefaultParameters_ForLoop() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  for (int i in (1..99)) {
                      // do something
                  }
              }
            }
        """)

        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            c.newInstance().myMethod()
        }
        assert 100 == counter.interruptedCheckCount
    }

    @Test
    void testDefaultParameters_WhileLoop() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  int x = 99
                  while (x > 0) {
                      x--
                  }
              }
            }
        """)

        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            c.newInstance().myMethod()
        }
        assert 100 == counter.interruptedCheckCount
    }

    @Test
    void testDefaultParameters_Closure() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  99.times {
                    // do something
                  }
              }
            }
        """)

        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            c.newInstance().myMethod()
        }
        assert 100 == counter.interruptedCheckCount
    }

    @Test
    void testInterrupt_Method_AndTestExceptionMessage() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() { }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            def ex = shouldFail(InterruptedException) { c.newInstance().myMethod() }
            assert ex.message == 'Execution interrupted. The current thread has been interrupted.'
        }
    }

    @Test
    void testInterrupt_ForLoop() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  for (int i in (1..99)) {
                      // do something
                  }
              }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(InterruptedException) { c.newInstance().myMethod() }
        }
    }

    @Test
    void testInterrupt_WhileLoop() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  int x = 99
                  while (x > 0) {
                      x--
                  }
              }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(InterruptedException) { c.newInstance().myMethod() }
        }
    }

    @Test
    void testInterrupt_Closure() {

        def c = new GroovyClassLoader().parseClass("""
            @groovy.transform.ThreadInterrupt
            class MyClass {
              def myMethod() {
                  99.times {
                    // do something
                  }
              }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(InterruptedException) { c.newInstance().myMethod() }
        }
    }

    @Test
    void testInterrupt_ClosureWithCustomExceptionType() {

        def c = new GroovyClassLoader(this.class.classLoader).parseClass("""
            @groovy.transform.ThreadInterrupt(thrown=groovy.transform.CustomException)
            class MyClass {
              def myMethod() {
                  99.times {
                    // do something
                  }
              }
            }
        """)

        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(CustomException) { c.newInstance().myMethod() }
        }
    }

    @Test
    void testEntireCompileUnitIsAffected() {

        def script = '''
            def scriptMethod() {
                // this method should inherit the checks from the annotation defined later
            }

            @groovy.transform.ThreadInterrupt
            class MyClass {

              def myMethod() {
                // this method should also be guarded
              }
            }
            scriptMethod()
            new MyClass().myMethod()
            '''
        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            new GroovyShell().evaluate(script)
        }
        // 3 is once for run(), once for scriptMethod() and once for myMethod()
        assert 3 == counter.interruptedCheckCount
    }

    @Test
    void testOnlyScriptAffected() {

        def script = '''
            @groovy.transform.ThreadInterrupt(applyToAllClasses = false)
            def scriptMethod() {
                // should be affected
            }

            class MyClass {
              def myMethod() {
                // should not be affected
              }
            }
            scriptMethod()
            new MyClass().myMethod()
            '''
        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            new GroovyShell().evaluate(script)
        }
        // 2 is once for run() and once for scriptMethod()
        assert 2 == counter.interruptedCheckCount
    }

    @Test
    void testAnnotationOnImport() {

        def script = '''
            @groovy.transform.ThreadInterrupt
            import java.lang.String

            3.times {
                // should be affected
            }
            '''
        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            new GroovyShell().evaluate(script)
        }
        // 4 is once for run() plus 3 for times loop
        assert 4 == counter.interruptedCheckCount
    }

    @Test
    void testOnlyClassAffected() {

        def script = '''
            def scriptMethod() {
                // this should not be affected
            }

            @groovy.transform.ThreadInterrupt(applyToAllClasses = false)
            class MyClass {
              def myMethod() {
                // this should be affected
              }
            }
            scriptMethod()
            new MyClass().myMethod()
            '''
        def counter = new CountingThread()
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { counter }
        mocker.use {
            new GroovyShell(ThreadInterruptibleASTTransformation.getClassLoader()).evaluate(script)
        }
        // 1 is once for myMethod()
        assert 1 == counter.interruptedCheckCount
    }

    @Test
    void testThreadInterruptOnAbstractClass() {
        def script = '''
            @groovy.transform.ThreadInterrupt
            abstract class MyAbstractClass {
                abstract void myMethod()
            }

            class Concrete extends MyAbstractClass {
                void myMethod() {
                    99.times {
                        // do something
                    }
                }
            }

            new Concrete().myMethod()
        '''
        def mocker = new StubFor(Thread.class)
        mocker.demand.currentThread(1..Integer.MAX_VALUE) { new InterruptingThread() }
        mocker.use {
            shouldFail(InterruptedException) {
                new GroovyShell(ThreadInterruptibleASTTransformation.getClassLoader()).evaluate(script)
            }
        }

    }
}

class InterruptingThread extends Thread {
    @Override
    boolean isInterrupted() {
        true
    }
}

class CountingThread extends Thread {
    def interruptedCheckCount = 0

    @Override
    boolean isInterrupted() {
        interruptedCheckCount++
        false
    }
}

class CustomException extends Exception {
    CustomException(final String message) {
        super(message)
    }
}